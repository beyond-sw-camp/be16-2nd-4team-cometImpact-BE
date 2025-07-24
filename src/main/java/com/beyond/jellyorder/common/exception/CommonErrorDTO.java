package com.beyond.jellyorder.common.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CommonErrorDTO {
    private String status_message;
    private int status_code;
}
