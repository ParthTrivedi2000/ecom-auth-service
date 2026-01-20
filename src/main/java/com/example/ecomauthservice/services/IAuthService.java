package com.example.EcomAuthService.services;

import com.example.EcomAuthService.exceptions.UserNotFoundException;

public interface IAuthService {
    Boolean signUp(String email, String password) throws Exception;
    String login(String email, String password) throws Exception;
    boolean validate(String token);
}
