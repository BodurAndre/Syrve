package org.example.server.DTO;

import lombok.Data;
import java.util.List;

@Data
public class DishDTO {
    private String id;
    private String name;
    private Double price;
    private Boolean isIncludedMenu;
    private Double weight;
    private List<ModifierGroupDTO> modifierGroups;
    private List<ModifierDTO> modifiers;
    private String imageLinks;
    private String code;
}