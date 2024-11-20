package org.example.server.models.orders;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Order")
public class Order {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "orderTypeId")
    private String orderTypeId;

    @ManyToOne
    @JoinColumn(name = "adress")
    private Adress adress;

    @ManyToOne
    @JoinColumn(name = "customer")
    private Customer customer;

    @ManyToMany
    @JoinColumn(name = "items")
    private List<Item> items;

    @Column(name = "comment")
    private String comment;

    @ManyToOne
    @JoinColumn(name = "payments")
    private Payment payments;

    @Column(name = "PRODUCTID")
    private String Jsom;
}
