package org.example.server.controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.example.server.DTO.Adress.CitiesDTO;
import org.example.server.DTO.Adress.StreetsDTO;
import org.example.server.DTO.ProductDTO;
import org.example.server.models.OrderRequest;
import org.example.server.repositories.CitiesRepository;
import org.example.server.repositories.ProductRepository;
import org.example.server.repositories.StreetsRepository;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class OrderController {
    private final CitiesRepository citiesRepository;
    private final StreetsRepository streetsRepository;

    public OrderController(CitiesRepository citiesRepository, StreetsRepository streetsRepository) {
        this.citiesRepository = citiesRepository;
        this.streetsRepository = streetsRepository;
    }



    @GetMapping("/orderStatus")
    public String orderStatus() {

        return "test/OrderStatus";
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

}