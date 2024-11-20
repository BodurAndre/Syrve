package org.example.server.models.orders;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Payment")
public class Payment {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;

    @Column(name = "Type")
    private String paymentTypeKind;

    @Column(name = "sum")
    private double sum;

    @Column(name = "isProcessedExternally")
    private boolean isProcessedExternally;
}
