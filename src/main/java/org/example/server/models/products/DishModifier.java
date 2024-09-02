package org.example.server.models.products;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DISHMODIFIER")
public class DishModifier {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "dish_id")
    private Dish dish;

    @ManyToOne
    @JoinColumn(name = "modifier_id")
    private Modifier modifier;

    @ManyToOne
    @JoinColumn(name = "modifier_dish_id")
    private Dish modifierDishID;

    @Column(name = "default_Quantity")
    private int defaultQuantity;

    @Column(name = "min_quantity")
    private int minQuantity;

    @Column(name = "max_quantity")
    private int maxQuantity;

    @Column(name = "is_Group_Modifier")
    private boolean isGroupModifier;

    @Column(name = "free_Of_Charge_Amount")
    private int freeOfChargeAmount;

    @Column(name = "free_Of_Charge_Amount_All")
    private int freeOfChargeAmountAll;
}

