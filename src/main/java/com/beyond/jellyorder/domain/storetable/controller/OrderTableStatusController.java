package com.beyond.jellyorder.domain.storetable.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.storetable.dto.ZoneListResDTO;
import com.beyond.jellyorder.domain.storetable.dto.orderTableStatus.OrderTableResDTO;
import com.beyond.jellyorder.domain.storetable.service.OrderTableStatusService;
import com.beyond.jellyorder.domain.storetable.service.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/order-table-status")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STORE')")
public class OrderTableStatusController {

    private final OrderTableStatusService orderTableStatusService;
    private final ZoneService zoneService;

    @GetMapping("/zone")
    public ResponseEntity<?> getZonesAndTables() {
        List<ZoneListResDTO> resDTOs = zoneService.getZoneList();
        return ApiResponse.ok(resDTOs);
    }
}
