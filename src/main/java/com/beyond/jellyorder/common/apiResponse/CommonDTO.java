package com.beyond.jellyorder.common.apiResponse;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@AllArgsConstructor
@Getter
public class CommonDTO {
    private String status_message;
    private int status_code;
    private Object result;
}
