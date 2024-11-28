package org.example.server.DTO.Admin.Order;

import lombok.Data;

@Data
public class OrderDTO {
    private int id;
    private String data;
    private String phone;
    private boolean status;
    private boolean payment;
    private double total;
}
