package com.beyond.jellyorder.common.exception;

import com.beyond.jellyorder.domain.category.service.CategoryService;
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
public class CommonExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegalException(IllegalArgumentException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new CommonErrorDTO(
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> entityException(EntityNotFoundException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new CommonErrorDTO(
                e.getMessage(),
                HttpStatus.NOT_FOUND.value()
        ), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> NoSuchElementException(NoSuchElementException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new CommonErrorDTO(
                e.getMessage(),
                HttpStatus.NOT_FOUND.value()
        ), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> MethodNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage());
        String defaultMessage = e.getBindingResult().getFieldError().getDefaultMessage();

        return new ResponseEntity<>(new CommonErrorDTO(
                defaultMessage,
                HttpStatus.BAD_REQUEST.value()
        ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> authenticationException(AuthenticationException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new CommonErrorDTO(
                e.getMessage(),
                HttpStatus.FORBIDDEN.value()
        ), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> DataViolationException(DataIntegrityViolationException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new CommonErrorDTO(
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CategoryService.DuplicateCategoryNameException.class)
    public ResponseEntity<?> handleDuplicateCategoryNameException(CategoryService.DuplicateCategoryNameException e) {
        log.warn("중복 카테고리 예외 발생: {}", e.getMessage());

        return new ResponseEntity<>(new CommonErrorDTO(
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        ), HttpStatus.BAD_REQUEST);
    }
}
