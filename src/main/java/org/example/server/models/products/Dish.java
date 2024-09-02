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

    @Column(name = "GROUPID")
    private String groupId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "MEASUREUNIT")
    private String measureUnit;

    @Column(name = "PRICE")
    private Double price;

    @Column(name = "ISINCLUDEDMENU")
    private Boolean isIncludedMenu;

    public Dish(String productId, String name, String groupId, String code, Double currentPrice, Boolean isIncludedMenu, String measureUnit) {
        this.productId = productId;
        this.name = name;
        this.groupId = groupId;
        this.code = code;
        this.price = currentPrice;
        this.isIncludedMenu = isIncludedMenu;
        this.measureUnit = measureUnit;
    }

}
