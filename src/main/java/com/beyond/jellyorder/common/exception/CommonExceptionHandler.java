package com.beyond.jellyorder.common.exception;

import com.beyond.jellyorder.domain.category.service.CategoryService;
import com.beyond.jellyorder.domain.ingredient.service.IngredientService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
@Hidden
public class CommonExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegalException(IllegalArgumentException e) {
        log.error("[IllegalArgumentException] code = {}, message = {}", HttpStatus.BAD_REQUEST, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonErrorDTO.builder()
                        .status_message(e.getMessage())
                        .status_code(HttpStatus.BAD_REQUEST.value())
                        .build()

                );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> entityException(EntityNotFoundException e) {
        log.error("[EntityNotFoundException] code = {}, message = {}", HttpStatus.NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CommonErrorDTO.builder()
                        .status_message(e.getMessage())
                        .status_code(HttpStatus.NOT_FOUND.value())
                        .build()
                );
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> NoSuchElementException(NoSuchElementException e) {
        log.error("[NoSuchElementException] code = {}, message = {}", HttpStatus.NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CommonErrorDTO.builder()
                        .status_message(e.getMessage())
                        .status_code(HttpStatus.NOT_FOUND.value())
                        .build()
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> MethodNotValidException(MethodArgumentNotValidException e) {
        log.error("[MethodArgumentNotValidException] code = {}, message = {}", HttpStatus.BAD_REQUEST, e.getMessage());
        String defaultMessage = e.getBindingResult().getFieldError().getDefaultMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonErrorDTO.builder()
                        .status_message(defaultMessage)
                        .status_code(HttpStatus.BAD_REQUEST.value())
                        .build()
                );

    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> authenticationException(AuthenticationException e) {
        log.error("[AuthenticationException] code = {}, message = {}", HttpStatus.FORBIDDEN, e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(CommonErrorDTO.builder()
                        .status_message(e.getMessage())
                        .status_code(HttpStatus.FORBIDDEN.value())
                        .build()
                );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<?> duplicateResourceException(DuplicateResourceException e) {
        log.error("[DuplicateResourceException] code = {}, message = {}", HttpStatus.BAD_REQUEST, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonErrorDTO.builder()
                        .status_message(e.getMessage())
                        .status_code(HttpStatus.BAD_REQUEST.value())
                        .result(e.getData())
                        .build()
                );
    }

}
