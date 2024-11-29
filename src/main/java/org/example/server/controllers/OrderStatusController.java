package org.example.server.controllers;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class OrderStatusController {

    private final SimpMessagingTemplate messagingTemplate;

    public OrderStatusController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendOrderStatus(String sessionId, String status) {
        messagingTemplate.convertAndSend("/topic/order-status/" + sessionId, status);
    }
}

