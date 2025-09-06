package com.beyond.jellyorder.domain.openclose.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.openclose.dto.CloseSummaryDTO;
import com.beyond.jellyorder.domain.openclose.dto.OpenSummaryDTO;
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
public class StoreOpenCloseController {
    private final StoreOpenCloseService storeOpenCloseService;
    @PostMapping("/close")
    public ResponseEntity<?> close(@RequestBody(required = false) Map<String, String> body) {
        LocalDateTime closedAt = (body != null && body.get("closedAt") != null)
                ? LocalDateTime.parse(body.get("closedAt"))
                : null;
        CloseSummaryDTO dto = storeOpenCloseService.close(closedAt);
        return ApiResponse.ok(dto, "마감완료");
    }

    @PostMapping("/open")
    public ResponseEntity<?> open(@RequestBody(required = false) Map<String, String> body) {
        LocalDateTime openedAt = (body != null && body.get("openedAt") != null)
                ? LocalDateTime.parse(body.get("openedAt"))
                : null;

        OpenSummaryDTO dto = storeOpenCloseService.open(openedAt);
        return ApiResponse.ok(dto, "오픈완료");
    }
}
