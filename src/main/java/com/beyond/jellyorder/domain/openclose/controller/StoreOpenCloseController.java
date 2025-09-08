package com.beyond.jellyorder.domain.openclose.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.openclose.dto.CloseSummaryDTO;
import com.beyond.jellyorder.domain.openclose.dto.OpenCloseStatusDTO;
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

    @PostMapping("/open")
    public ResponseEntity<?> open() {
        return ApiResponse.ok(storeOpenCloseService.open(), "오픈 완료");
    }

    @PostMapping("/close")
    public ResponseEntity<?> close() {

        return ApiResponse.ok(storeOpenCloseService.close(), "마감 완료");
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        OpenCloseStatusDTO openCloseStatusDTO = storeOpenCloseService.status();
        return ApiResponse.ok(openCloseStatusDTO, "상태 조회 완료");
    }

    @GetMapping("/summary")
    public ResponseEntity<?> summary() {
        CloseSummaryDTO dto = storeOpenCloseService.summary();
        return ApiResponse.ok(dto, "요약 조회 완료");
    }
}
