package org.example.server.models.orders;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Order_Response")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "orderInfo_id", nullable = true) // Ссылаемся на OrderInfo
    private OrderInfo orderInfo;

    @Column(name = "correlationId", nullable = true)
    private String correlationId;
}
