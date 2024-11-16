package org.example.server.DTO;

import lombok.Data;
import java.util.List;

@Data
public class ModifierGroupDTO {
    private String name;
    private int maxQuantity; // MaxQuantity для группы
    private int freeOfChargeAmount;
    private List<ModifierDTO> modifiers;
}
