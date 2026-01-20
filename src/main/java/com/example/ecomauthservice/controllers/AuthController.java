package com.example.EcomAuthService.controllers;

import com.example.EcomAuthService.dtos.*;
import com.example.EcomAuthService.exceptions.UserNotFoundException;
import com.example.EcomAuthService.services.IAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final IAuthService authService;

    AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/sign_up")
    public ResponseEntity<SignUpResponseDto> signUp(@RequestBody SignUpRequestDto request) throws Exception {
        HttpStatus httpStatus;
        RequestStatus requestStatus;
        SignUpResponseDto response;

        if(authService.signUp(request.getEmail(), request.getPassword())) {
            httpStatus = HttpStatus.CREATED;
            requestStatus = RequestStatus.SUCCESS;
        }
        else {
            httpStatus = HttpStatus.BAD_REQUEST;
            requestStatus = RequestStatus.FAILURE;
        }

        response = SignUpResponseDto.builder().requestStatus(requestStatus).build();

        return new ResponseEntity<>(response, httpStatus);

        // Above implementation is to show main core logic, please try to handle edge cases by yourself.
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(LoginRequestDto request) throws Exception {
        HttpStatus httpStatus;
        RequestStatus requestStatus;
        LoginResponseDto response;

        // Generate Token
        String token = authService.login(request.getEmail(), request.getPassword());

        // Creating Headers to set token in the returned response
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

        // Logic based on token value (if it's invalid we will return Null from Service layer)
        if(token != null){
            httpStatus = HttpStatus.OK;
            requestStatus = RequestStatus.SUCCESS;
            headers.add("AUTH_TOKEN", "Bearer " + token);
        }
        else{
            httpStatus = HttpStatus.UNAUTHORIZED;
            requestStatus = RequestStatus.FAILURE;
        }

        response = LoginResponseDto.builder().requestStatus(requestStatus).build();

        return new ResponseEntity<>(response,headers, httpStatus);

        // Currently for both the methods throwing exceptions in the method signature.
        // Later please handle it gracefully.

    }

    public boolean validate(@RequestParam("token") String token) {
        return authService.validate(token);
    }

}
