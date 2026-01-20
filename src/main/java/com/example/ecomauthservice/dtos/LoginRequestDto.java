package com.example.EcomAuthService.dtos;

import lombok.Data;

@Data
public class LoginRequestDto {

    private String email;
    private String password;

}
