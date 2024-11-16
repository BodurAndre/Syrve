package org.example.server.models.adress;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "STREETS")
public class Streets {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "STREETID")
    private String streetId;

    @ManyToOne
    @JoinColumn(name = "CITYID")
    private Cities city;

    @Column(name = "NAME")
    private String name;

    @Column(name = "isDELETED")
    private boolean isDeleted;

    public Streets(String streetId, String name, boolean isDeleted, Cities city) {
        this.streetId = streetId;
        this.name = name;
        this.isDeleted = isDeleted;
        this.city = city;
    }
}
