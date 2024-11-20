package org.example.server.models.orders;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Adress")
public class Adress {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;

    @Column(name="street")
    private String street;

    @Column (name="house")
    private String house;

    @Column (name="type")
    private String type;

    @Column (name="entrance")
    private String entrance;

    @Column (name="flat")
    private String flat;

    @Column (name="floor")
    private String floor;

    @Column (name="intercom")
    private String intercom;
}
