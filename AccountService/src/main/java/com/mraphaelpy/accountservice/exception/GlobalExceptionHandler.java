package com.mraphaelpy.accountservice.exception;

import com.mraphaelpy.accountservice.utils.GlobalVariables;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAccountNotFoundException(AccountNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(GlobalVariables.TIMESTAMP, Instant.now());
        body.put(GlobalVariables.STATUS, HttpStatus.NOT_FOUND.value());
        body.put(GlobalVariables.ERROR, HttpStatus.NOT_FOUND.getReasonPhrase());
        body.put(GlobalVariables.MESSAGE, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleNoBalanceException(NoBalanceException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(GlobalVariables.TIMESTAMP, Instant.now());
        body.put(GlobalVariables.STATUS, HttpStatus.BAD_REQUEST.value());
        body.put(GlobalVariables.ERROR, HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put(GlobalVariables.MESSAGE, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequestExceptions(RuntimeException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(GlobalVariables.TIMESTAMP, Instant.now());
        body.put(GlobalVariables.STATUS, HttpStatus.BAD_REQUEST.value());
        body.put(GlobalVariables.ERROR, HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put(GlobalVariables.MESSAGE, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(UserNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(GlobalVariables.TIMESTAMP, Instant.now());
        body.put(GlobalVariables.STATUS, HttpStatus.NOT_FOUND.value());
        body.put(GlobalVariables.ERROR, HttpStatus.NOT_FOUND.getReasonPhrase());
        body.put(GlobalVariables.MESSAGE, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
}

