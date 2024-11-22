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
@Table(name = "OrderPayment")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "payment_type_kind", nullable = false)
    private String paymentTypeKind;

    @Column(name = "sum", nullable = false)
    private double sum;

    @Column(name = "is_processed_externally", nullable = false)
    private boolean isProcessedExternally;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}
