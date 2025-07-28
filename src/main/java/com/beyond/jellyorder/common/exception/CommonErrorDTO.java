package com.beyond.jellyorder.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CommonErrorDTO {
    private String status_message;
    private int status_code;
}
