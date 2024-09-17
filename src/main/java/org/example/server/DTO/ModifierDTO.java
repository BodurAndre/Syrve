package org.example.server.DTO;


import lombok.Data;

@Data
public class ModifierDTO {
    private String id;
    private String name;
    private int defaultQuantity;
    private int minQuantity;
    private int maxQuantity;
    private int freeOfChargeAmount;
    private int freeOfChargeAmountAll;
    private Double currentPrice;
    private boolean isGroupModifier;
    private String nameGroup;
    private String idGroup;
}
