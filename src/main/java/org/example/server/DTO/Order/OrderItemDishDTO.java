package org.example.server.DTO.Order;

import lombok.Data;

import java.util.List;

@Data
public class OrderItemDishDTO {
    private String id;
    private String name;
    private int amount;
    private String code;
    private List<OrderItemModifierDTO> modifiers;
}
