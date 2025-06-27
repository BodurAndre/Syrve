package org.example.server.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Terminal_Group")
public class TerminalGroup {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Terminal_Id")
    private String terminalId;

    @ManyToOne
    @JoinColumn(name = "Organization_ID")
    private RestaurantInfo idRestaurant;

    @Column(name = "name_Terminal")
    private String nameRestaurant;

    @Column(name = "address")
    private String address;

    @Column(name = "time_Zone")
    private String timeZone;

    @Column(name = "active")
    private boolean isActive;

    public TerminalGroup(RestaurantInfo idRestaurant, String terminalId, String nameRestaurant, String address, String timeZone, boolean sleep) {
        this.idRestaurant = idRestaurant;
        this.terminalId = terminalId;
        this.nameRestaurant = nameRestaurant;
        this.address = address;
        this.timeZone = timeZone;
        this.isActive = sleep;
    }
}
