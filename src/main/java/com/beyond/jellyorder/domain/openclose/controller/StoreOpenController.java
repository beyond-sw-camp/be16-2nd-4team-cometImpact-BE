package com.beyond.jellyorder.domain.openclose.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.openclose.service.StoreOpenCloseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/open-close")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STORE')")
public class StoreOpenController {
    private final StoreOpenCloseService storeOpenCloseService;
    @PostMapping("/close")
    public ResponseEntity<Void> close(@RequestBody(required = false) Map<String, String> body) {
        LocalDateTime closedAt = (body != null && body.get("closedAt") != null)
                ? LocalDateTime.parse(body.get("closedAt"))
                : null;
        storeOpenCloseService.close(closedAt);
        return ApiResponse.ok("마감완료");
    }
}
