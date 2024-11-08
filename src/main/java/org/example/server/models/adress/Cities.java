package org.example.server.models.adress;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CITIES")
public class Cities {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CITYID")
    private String cityId;

    @Column(name = "NAME")
    private String city;

    @Column(name = "isDELETED")
    private boolean isDeleted;

    public Cities(String cityId, String city, boolean isDeleted) {
        this.cityId = cityId;
        this.city = city;
        this.isDeleted = isDeleted;
    }
}
