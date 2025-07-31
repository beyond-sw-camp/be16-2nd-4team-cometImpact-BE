package com.beyond.jellyorder.domain.test.testexception;

import com.beyond.jellyorder.common.exception.CommonErrorDTO;
import com.beyond.jellyorder.domain.test.testexception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 기본적인 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonErrorDTO> handleCustomException(BusinessException e) {
        log.error("[BusinessException] code={}, message={}", e.getErrorCode(), e.getMessage());

        return ResponseEntity.status(e.getErrorCode().getStatus())
                .body(CommonErrorDTO.builder()
                        .status_message(e.getMessage())
                        .status_code(e.getErrorCode().getStatus().value())
                        .result(e.getData())
                        .build());
    }








}
