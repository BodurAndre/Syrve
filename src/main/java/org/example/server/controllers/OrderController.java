package org.example.server.controllers;


import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class OrderController {
    @PostMapping("/ordering")
    public ResponseEntity<Void> processOrder(@RequestBody String json, HttpSession session) {
        session.setAttribute("order", json);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/order")
    public String showOrder() {
        return "Web/order";
    }

    @RequestMapping(value = "/viewProducts", method = RequestMethod.GET)
    public String getMenu(){
        return "test/test";
    }

}