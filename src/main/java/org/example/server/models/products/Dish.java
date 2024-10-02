package org.example.server.models.products;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DISHES")
public class Dish {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PRODUCTID")
    private String productId;

    @Column(name = "NAME")
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_Group")
    private Groups groupId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "MEASUREUNIT")
    private String measureUnit;

    @Column(name = "PRICE")
    private Double price;

    @Column(name = "ISINCLUDEDMENU")
    private Boolean isIncludedMenu;

    @Column(name = "WEIGHT")
    private Double weight;

    @Column(name = "imageLinks")
    private String imageLinks;

    public Dish(String productId, String name, Groups groupId, String code, Double currentPrice, Boolean isIncludedMenu, String measureUnit, Double weight, String imageLinks) {
        this.productId = productId;
        this.name = name;
        this.groupId = groupId;
        this.code = code;
        this.price = currentPrice;
        this.isIncludedMenu = isIncludedMenu;
        this.measureUnit = measureUnit;
        this.weight = weight;
        this.imageLinks = imageLinks;
    }

}
