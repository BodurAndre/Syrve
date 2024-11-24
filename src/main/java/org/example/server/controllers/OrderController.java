package org.example.server.controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.example.server.DTO.Adress.CitiesDTO;
import org.example.server.DTO.Adress.StreetsDTO;
import org.example.server.DTO.ProductDTO;
import org.example.server.models.OrderRequest;
import org.example.server.models.orders.Order;
import org.example.server.repositories.CitiesRepository;
import org.example.server.repositories.ProductRepository;
import org.example.server.repositories.StreetsRepository;
import org.example.server.repositories.orders.*;
import org.example.server.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class OrderController {

    private final CitiesRepository citiesRepository;
    private final StreetsRepository streetsRepository;

    private final OrderService orderService;

    public OrderController(CitiesRepository citiesRepository, StreetsRepository streetsRepository, OrderService orderService) {
        this.citiesRepository = citiesRepository;
        this.streetsRepository = streetsRepository;
        this.orderService = orderService;
    }


    @GetMapping("/order")
    public String showOrder() {
        return "Web/order";
    }

    @RequestMapping(value = "/viewProducts", method = RequestMethod.GET)
    public String getMenu(){
        return "test/test";
    }

    @GetMapping("/cities")
    public ResponseEntity<List<CitiesDTO>> getCities(){
        List<CitiesDTO> citiesDTOList = citiesRepository.findAll().stream()
                .map(cities -> {
                    CitiesDTO citiesDTO = new CitiesDTO();
                    citiesDTO.setCity(cities.getCity());
                    citiesDTO.setCityId(cities.getCityId());
                    return citiesDTO;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(citiesDTOList);
    }

    @GetMapping("/streets")
    public ResponseEntity<List<StreetsDTO>> getStreets(@RequestParam String cityId) {
        List<StreetsDTO> streetsDTOList = streetsRepository.findStreetsById(cityId).stream()
                .map(streets -> {
                    StreetsDTO streetsDTO = new StreetsDTO();
                    streetsDTO.setStreetId(streets.getStreetId());
                    streetsDTO.setNameStreet(streets.getName());
                    streetsDTO.setCity(streets.getCity());
                    return streetsDTO;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(streetsDTOList);
    }

    @PostMapping("/saveOrder")
    public ResponseEntity<?> saveOrder(@RequestBody JsonNode json, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        try {
            orderService.processOrder(json, ipAddress);
            return ResponseEntity.ok(new SuccessResponse("Order processed successfully"));
        } catch (RuntimeException ex) {
            // Возвращаем ошибку в виде объекта JSON
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(ex.getMessage()));
        }
    }

    // Класс для успешного ответа
    public class SuccessResponse {
        private String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    // Класс для ошибки
    public class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}