package com.example.treksathi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.SimpleTimeZone;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedException(UnauthorizedException ex){
        Map<String, Object> body = new HashMap<>();
        body.put("Status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized to access");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialException(InvalidCredentialsException ex){
        Map<String, Object> body = new HashMap<>();
        body.put("Status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Invalid Credentials");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExistException(UserAlreadyExistException ex){
        Map<String, Object> body = new HashMap<>();
        body.put("Status", HttpStatus.CONFLICT.value());
        body.put("error", "User Already Exist");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<Map<String, Object>> handleInternalServerErrorException(InternalServerErrorException ex){
        Map<String, Object> body = new HashMap<>();
        body.put("Status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TokenExpireException.class)
    public ResponseEntity<Map<String, Object>> handleTokenExpireException(TokenExpireException ex){
        Map<String, Object> body = new HashMap<>();
        body.put("Status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Token is Expired");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEventNotFoundException(EventNotFoundException ex){
        Map<String, Object> body = new HashMap<>();
        body.put("Status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Event Not Found");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
}
