package org.example.server.DTO.Adress;

import lombok.Data;
import org.example.server.models.adress.Cities;

@Data
public class StreetsDTO {
    private String streetId;
    private Cities city;
    private String nameStreet;
}
