package org.example.server.DTO.Order;

import lombok.Data;

@Data
public class OrderItemModifierDTO {
    private String id;
    private String name;
    private int amount;
    private String groupId;
    private String groupName;
}
