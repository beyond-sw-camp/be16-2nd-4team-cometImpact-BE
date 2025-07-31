package com.beyond.jellyorder.domain.test.testexception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Store

    // Menu

    // Category

    // Ingredient

    // StoreTable
    DUPLICATE_STORE_TABLE_NAME("이미 존재하는 테이블 이름입니다.", HttpStatus.BAD_REQUEST),
    STORE_TABLE_NOT_FOUND("해당 테이블이 존재하지 않습니다.", HttpStatus.NOT_FOUND),

    // Zone
    DUPLICATE_ZONE_NAME("해당 테이블이 존재하지 않습니다.", HttpStatus.BAD_REQUEST),
    ZONE_NOT_FOUND("해당 구역이 존재하지 않습니다.", HttpStatus.NOT_FOUND),

    // system_error
    INTERNAL_SERVER_ERROR("예상하지 못한 서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT("잘못된 입력입니다.", HttpStatus.BAD_REQUEST);


    private final String message;
    private final HttpStatus status;
}
