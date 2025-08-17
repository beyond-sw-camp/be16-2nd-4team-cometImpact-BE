package com.beyond.jellyorder.domain.order.controller;

import com.beyond.jellyorder.domain.order.dto.TotalOrderByTableResDto;
import com.beyond.jellyorder.domain.order.dto.TotalOrderDetailResDto;
import com.beyond.jellyorder.domain.order.service.TotalOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@PreAuthorize("hasRole('STORE')")
public class TotalOrderController {

    private final TotalOrderService totalOrderService;

    /** 주문내역(열려있는 최신 TotalOrder) – 테이블 ID 기반 */
    @GetMapping("/total/by-table")
    public ResponseEntity<?> getOpenTotalByTable(@RequestParam UUID storeTableId) {
        TotalOrderByTableResDto res = totalOrderService.getOpenTotalOrderByTable(storeTableId);
        return ResponseEntity.ok(res);
    }

    /** 주문내역 – 특정 totalOrderId로 조회(히스토리 포함) */
    @GetMapping("/total/{totalOrderId}")
    public ResponseEntity<?> getTotalById(@PathVariable UUID totalOrderId) {
        TotalOrderByTableResDto res = totalOrderService.getTotalOrder(totalOrderId);
        return ResponseEntity.ok(res);
    }
}