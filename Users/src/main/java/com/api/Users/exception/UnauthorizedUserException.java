package com.api.Users.exception;

public class UnauthorizedUserException extends RuntimeException{
    public  UnauthorizedUserException(String message){
        super(message);
    }
}
