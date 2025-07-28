package com.beyond.jellyorder.domain.table.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.table.dto.ZoneCreateReqDTO;
import com.beyond.jellyorder.domain.table.dto.ZoneResDTO;
import com.beyond.jellyorder.domain.table.dto.ZoneUpdateReqDTO;
import com.beyond.jellyorder.domain.table.service.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/zone")
public class ZoneController {

    private final ZoneService zoneService;

    @PostMapping("/create/{storeLoginId}")
    public ResponseEntity<?> createZone(
            @RequestBody ZoneCreateReqDTO reqDTO,
            @PathVariable String storeLoginId
    ) {
        ZoneResDTO resDTO = zoneService.createZone(reqDTO, storeLoginId);
        return ApiResponse.created(resDTO, "구역이 생성되었습니다.");
    }

    @PutMapping("/update/{zoneId}/{storeLoginId}")
    public ResponseEntity<?> updateZone(
            @RequestBody ZoneUpdateReqDTO reqDTO,
            @PathVariable UUID zoneId,
            @PathVariable String storeLoginId
    ) {
        ZoneResDTO resDTO = zoneService.updateZone(reqDTO, zoneId, storeLoginId);
        return ApiResponse.ok(resDTO, "구역이 수정되었습니다.");
    }



}
