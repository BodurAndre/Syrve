package org.example.server.DTO.Admin.Order;

import jakarta.persistence.Column;
import lombok.Data;

import java.util.List;

@Data
public class OrderAdminDishDTO {
    private int amount;
    private String name;
    private String imageLinks;
    private String price;
    private String discount;
    private String subtotal;

    private List<OrderAdminModifierDTO> modifiers;
}
