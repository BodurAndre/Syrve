package org.example.server.DTO.Admin;

import lombok.Data;

@Data
public class StreetDTO {
    private String streetName;
    private String streetId;
    private boolean isDeleted;
}
