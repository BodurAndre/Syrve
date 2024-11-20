package org.example.server.models.orders;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Item")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;

    @Column(name="productId")
    private String productId;

    @Column(name="amount")
    private int amount;

    @ManyToMany
    @JoinColumn(name = "modifiers")
    private List<OrderModifier> modifiers;
}
