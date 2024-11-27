package org.example.server.DTO.Admin;

import lombok.Data;

import java.util.List;

@Data
public class CityWithStreetsDTO {
    private String cityName;
    private String cityId;
    private boolean isDeleted;
    private List<StreetDTO> streets;

}
