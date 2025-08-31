package com.beyond.jellyorder.domain.settlement.controller;

import com.beyond.jellyorder.domain.settlement.dto.SettlementDashboardDTO;
import com.beyond.jellyorder.domain.settlement.dto.SettlementUnitDetailDTO;
import com.beyond.jellyorder.domain.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/settlements")
@RequiredArgsConstructor
public class SettlementController {
    private final SettlementService settlementService;

    /**
     * 단 한 번의 요청으로 화면 전체 데이터 반환
     *
     * 기본 동작:
     *  - 일별: 오늘 기준 최근 5일
     *  - 주별: 최근 8주(월요일 시작)
     *  - 월별: 최근 6개월(각 월 1일)
     *
     * 선택 파라미터로 특정 기간을 오버라이드 가능(각 탭별 별도 지정)
     */
    @GetMapping("/dashboard")
    public SettlementDashboardDTO dashboard(
            @RequestParam UUID storeId,
            // 일별
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dailyFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dailyTo,
            // 주별
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime weeklyFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime weeklyTo,
            // 월별
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime monthlyFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime monthlyTo
    ) {
        return settlementService.dashboard(storeId, dailyFrom, dailyTo, weeklyFrom, weeklyTo, monthlyFrom, monthlyTo);
    }

    @GetMapping("/details")
    public Page<SettlementUnitDetailDTO> details(
            @RequestParam UUID storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String status, // "COMPLETED" | "CANCELLED" | null
            @PageableDefault(size = 20)
            Pageable pageable
    ) {
        return settlementService.detailPage(storeId, from, to, status, pageable);
    }
}
