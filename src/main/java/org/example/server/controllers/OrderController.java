package org.example.server.controllers;


import jakarta.servlet.http.HttpSession;
import org.example.server.DTO.DishDTO;
import org.example.server.models.RestaurantInfo;
import org.example.server.service.RestaurantService;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class OrderController {
    @PostMapping("/ordering")
    public ResponseEntity<Void> processOrder(@RequestBody String json, HttpSession session) {
        session.setAttribute("order", json);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/order")
    public String showOrder(Model model, HttpSession session) {
        String orderJson = (String) session.getAttribute("order");
        System.out.println(orderJson);
        session.removeAttribute("order");
        model.addAttribute("order", orderJson);
        return "test/order";
    }

    @RequestMapping(value = "/viewProducts", method = RequestMethod.GET)
    public String getMenu(){
        return "test/test";
    }

}