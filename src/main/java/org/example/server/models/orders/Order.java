package org.example.server.models.orders;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OrderOrders")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "order_type_id", nullable = false)
    private String orderTypeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true) // Разрешаем null
    @JoinColumn(name = "address_id", nullable = true)
    private Adress address;

    @ManyToOne(fetch = FetchType.LAZY, optional = true) // Разрешаем null
    @JoinColumn(name = "customer_id", nullable = true)
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @Column(name = "comment")
    private String comment;

    @Column(name = "status")
    private boolean status;

    @Column(name = "JSON", columnDefinition = "TEXT")
    private String json;
}
