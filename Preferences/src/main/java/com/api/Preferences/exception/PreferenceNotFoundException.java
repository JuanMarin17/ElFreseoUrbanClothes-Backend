package com.api.Preferences.exception;

public class PreferenceNotFoundException extends RuntimeException {
    public PreferenceNotFoundException(String message) {
        super(message);
    }
}