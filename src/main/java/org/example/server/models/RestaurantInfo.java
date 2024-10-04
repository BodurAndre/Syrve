package org.example.server.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Restouran")
public class RestaurantInfo {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "API_LOGIN")
    private String apiLogin;

    @Column(name = "name_Restaurant")
    private String nameRestaurant;

    @Column(name = "id_Restaurant")
    private String idRestaurant;
}
