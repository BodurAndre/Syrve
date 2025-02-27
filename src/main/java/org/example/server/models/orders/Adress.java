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
@Table(name = "OrderAdress")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Adress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "street_ID", nullable = false)
    private String streetID;

    @Column(name = "street_name", nullable = false)
    private String streetName;

    @Column(name = "city_name", nullable = false)
    private String cityName;

    @Column(name = "house", nullable = false)
    private String house;

    @Column(name = "type")
    private String type;

    @Column(name = "entrance")
    private String entrance;

    @Column(name = "flat")
    private String flat;

    @Column(name = "floor")
    private String floor;

    @Column(name = "intercom")
    private String intercom;
}
