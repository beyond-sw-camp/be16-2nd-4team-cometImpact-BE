package com.beyond.jellyorder.common.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.naming.AuthenticationException;
import java.util.NoSuchElementException;

@ControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegalException(IllegalArgumentException e) {
        return new ResponseEntity<>(new CommonErrorDTO(
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> entityException(EntityNotFoundException e) {
        return new ResponseEntity<>(new CommonErrorDTO(
                e.getMessage(),
                HttpStatus.NOT_FOUND.value()
        ), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> NoSuchElementException(NoSuchElementException e) {
        return new ResponseEntity<>(new CommonErrorDTO(
                e.getMessage(),
                HttpStatus.NOT_FOUND.value()
        ), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> MethodNotValidException(MethodArgumentNotValidException e) {
        String defaultMessage = e.getBindingResult().getFieldError().getDefaultMessage();

        return new ResponseEntity<>(new CommonErrorDTO(
                defaultMessage,
                HttpStatus.BAD_REQUEST.value()
        ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> authenticationException(AuthenticationException e) {
        return new ResponseEntity<>(new CommonErrorDTO(
                e.getMessage(),
                HttpStatus.FORBIDDEN.value()
        ), HttpStatus.FORBIDDEN);
    }


}
