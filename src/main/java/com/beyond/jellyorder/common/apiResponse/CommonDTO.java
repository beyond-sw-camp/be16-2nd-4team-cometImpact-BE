package com.beyond.jellyorder.common.apiResponse;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@AllArgsConstructor
@Getter
@Builder
@Data
public class CommonDTO {
    private String status_message;
    private Integer status_code;
    private Object result;
}
