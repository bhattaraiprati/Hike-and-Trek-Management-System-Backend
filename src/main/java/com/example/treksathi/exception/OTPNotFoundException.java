package com.example.treksathi.exception;

public class OTPNotFoundException extends RuntimeException {
    public OTPNotFoundException(String message) {
        super(message);
    }
}
