package com.beyond.jellyorder.common;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/health")
public class HealthCheckController {

    @GetMapping
    public String healthCheck() {
        return "health check OK, Time: " + String.valueOf(LocalDateTime.now());
    }


}
