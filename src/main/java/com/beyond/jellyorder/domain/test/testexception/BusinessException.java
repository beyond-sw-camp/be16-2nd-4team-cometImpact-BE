package com.beyond.jellyorder.domain.test.testexception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{

    private final ErrorCode errorCode;
    private final Object data;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = null;
    }

    public BusinessException(ErrorCode errorCode, Object data) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = data;
    }

}
