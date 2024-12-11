package org.example.server.DTO.Admin.Order;

import lombok.Data;

import java.util.List;

@Data
public class OrderAdminDishDTO {
    private String nameDish;
    private int amount;
    private List<OrderAdminModifierDTO> modifiers;
}
