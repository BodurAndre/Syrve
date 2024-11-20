package org.example.server.models.orders;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.server.models.products.Dish;
import org.example.server.models.products.Modifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OrderModifier")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderModifier {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "modifier")
    private Modifier productId;

    @ManyToOne
    @JoinColumn(name = "modifierDish")
    private Dish dishId;

    @Column(name = "amount")
    private int amount;

    @Column(name = "productGroupId")
    private String productGroupId;
}
