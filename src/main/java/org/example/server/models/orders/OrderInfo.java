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
@Table(name = "order_info") // Приводим имя таблицы к общепринятому формату
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_response")
    private String idResponse;

    @Column(name = "pos_id")
    private String posId;

    @Column(name = "external_number")
    private String externalNumber;

    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "timestamp")
    private long timestamp;

    @Column(name = "creation_status")
    private String creationStatus;

    @Column(name = "error_info")
    private String errorInfo;

    @Lob
    @Column(name = "order_data", columnDefinition = "TEXT") // Указываем, что это JSON
    private String orderData;

    @OneToOne(mappedBy = "orderInfo")
    private Response response;
}
