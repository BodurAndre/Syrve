package org.example.server.models.products;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "PRODUCTS")
public class Product {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="ID_PRODUCTS")
    private String idProducts;

    @Column(name = "NAME")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
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

    public Product(String id, String name, Groups groupId, String code, String measureUnit, Double price, Boolean isIncludedMenu) {
        this.idProducts = id;
        this.name = name;
        this.groupId = groupId;
        this.code = code;
        this.measureUnit = measureUnit;
        this.price = price;
        this.isIncludedMenu = isIncludedMenu;
    }

}
