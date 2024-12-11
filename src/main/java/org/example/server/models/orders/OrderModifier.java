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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private String price;

    @Column(name = "subtotal")
    private String subtotal;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
}
