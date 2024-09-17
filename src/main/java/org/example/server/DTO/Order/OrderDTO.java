package org.example.server.DTO.Order;

import lombok.Data;

import java.util.List;

@Data
public class OrderDTO {
    private List<OrderItemDishDTO> items;
}
