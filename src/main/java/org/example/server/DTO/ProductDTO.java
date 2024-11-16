package org.example.server.DTO;

import lombok.Data;


@Data
public class ProductDTO {
    private String id;
    private String name;
    private Double price;
    private String groupName;
    private Boolean isIncludedMenu;
    private String weight;
    private String imageLinks;
    private String code;
    private int amount;
}
