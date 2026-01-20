package com.example.EcomAuthService.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
    private RequestStatus requestStatus;
}
