package org.example.server.DTO.Admin;

import lombok.Data;

@Data
public class UserDTO {
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String email;
    private String gender;
    private String role;
    private String phone;
}
