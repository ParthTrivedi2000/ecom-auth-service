package com.example.EcomAuthService.exceptions;

public class IncorrectPasswordException extends Exception{
    public IncorrectPasswordException(String message){
        super(message);
    }
}
