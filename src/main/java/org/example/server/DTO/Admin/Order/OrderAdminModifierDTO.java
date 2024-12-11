package org.example.server.DTO.Admin.Order;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class OrderAdminModifierDTO {
    private int amount;
    private String name;
    private String price;
    private String subtotal;
}
