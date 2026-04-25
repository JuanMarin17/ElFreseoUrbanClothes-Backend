package com.user.api.user.exception;

public class InvalidOtpException extends RuntimeException{
    public InvalidOtpException(String message){
        super(message);
    }
}
