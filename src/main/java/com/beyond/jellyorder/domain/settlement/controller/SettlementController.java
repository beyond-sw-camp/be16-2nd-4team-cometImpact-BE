package com.beyond.jellyorder.domain.settlement.controller;

import com.beyond.jellyorder.domain.settlement.dto.SettlementDetailDTO;
import com.beyond.jellyorder.domain.settlement.dto.SettlementSummaryDTO;
import com.beyond.jellyorder.domain.settlement.entity.Bucket;
import com.beyond.jellyorder.domain.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/settlements")
@RequiredArgsConstructor
public class SettlementController {
    private final SettlementService settlementService;

    @GetMapping("/summary")
    public List<SettlementSummaryDTO> summary(
            @RequestParam Bucket bucket,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return settlementService.summary(bucket, from, to);
    }

    @GetMapping("/detail")
    public Page<SettlementDetailDTO> lines(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        return settlementService.lines(from, to, pageable);
    }
}
