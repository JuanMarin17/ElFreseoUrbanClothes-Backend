package com.user.api.user.exception;

public class IncorrectCredentialsException extends RuntimeException{
    public IncorrectCredentialsException(String message){
        super(message);
    }
}
