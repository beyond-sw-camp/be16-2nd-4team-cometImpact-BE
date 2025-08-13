package com.beyond.jellyorder.domain.storetable.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.storetable.dto.orderTableStatus.OrderTableDetailResDTO;
import com.beyond.jellyorder.domain.storetable.dto.orderTableStatus.OrderTableUpdateReqDTO;
import com.beyond.jellyorder.domain.storetable.dto.zone.ZoneListResDTO;
import com.beyond.jellyorder.domain.storetable.dto.orderTableStatus.OrderTableResDTO;
import com.beyond.jellyorder.domain.storetable.service.OrderTableStatusService;
import com.beyond.jellyorder.domain.storetable.service.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/order-table-status")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STORE')")
public class OrderTableStatusController {

    private final OrderTableStatusService orderTableStatusService;
    private final ZoneService zoneService;

    // zone리스트 받아오기.
    @GetMapping("/zoneList")
    public ResponseEntity<?> getZonesAndTables() {
        List<ZoneListResDTO> resDTOs = zoneService.getZoneList();
        return ApiResponse.ok(resDTOs);
    }

    // 테이블별 주문 현황들 조회
    @GetMapping("/zoneList/{zoneId}/table-orders")
    public ResponseEntity<?> getTablesByZone(
            @PathVariable UUID zoneId
            ) {
        List<OrderTableResDTO> resDTOs = orderTableStatusService.getTablesByZone(zoneId);
        return ApiResponse.ok(resDTOs);
    }

    // 주문 테이블 상세 조회
    @GetMapping("/{totalOrderId}")
    public ResponseEntity<?> getTableOrderDetail(
            @PathVariable UUID totalOrderId
    ) {
        List<OrderTableDetailResDTO> resDTOs = orderTableStatusService.getTableOrderDetail(totalOrderId);
        return ApiResponse.ok(resDTOs);
    }

    // 주문 테이블 주문 수정
    @PatchMapping("/update")
    public ResponseEntity<?> updateOrderTable(
            @RequestBody List<OrderTableUpdateReqDTO> reqDTOs
    ) {
        orderTableStatusService.updateOrderTable(reqDTOs);

        return ApiResponse.ok(null, "주문 수정 완료");
    }


}
