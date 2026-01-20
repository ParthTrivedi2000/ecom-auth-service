package com.example.EcomAuthService.dtos;

import lombok.Data;

@Data
public class SignUpRequestDto {

    private String firstName; // modify later with firstName, lastName or some other attributes.
    private String lastName;
    private String email;
    private String password;

}
