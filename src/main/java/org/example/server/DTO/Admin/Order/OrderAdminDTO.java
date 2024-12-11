package org.example.server.DTO.Admin.Order;

import lombok.Data;

import java.util.List;

@Data
public class OrderAdminDTO {
    private int id;
    private String data;
    private String phone;
    private String status;
    private boolean payment;
    private double total;
    private List<OrderAdminDishDTO> dishes;
}
