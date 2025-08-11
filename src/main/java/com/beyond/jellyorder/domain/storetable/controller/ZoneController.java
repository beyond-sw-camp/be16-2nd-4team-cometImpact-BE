package com.beyond.jellyorder.domain.storetable.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.storetable.dto.zone.ZoneCreateReqDTO;
import com.beyond.jellyorder.domain.storetable.dto.zone.ZoneListResDTO;
import com.beyond.jellyorder.domain.storetable.dto.zone.ZoneResDTO;
import com.beyond.jellyorder.domain.storetable.dto.zone.ZoneUpdateReqDTO;
import com.beyond.jellyorder.domain.storetable.service.ZoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/zone")
@PreAuthorize("hasRole('STORE')")
public class ZoneController {

    private final ZoneService zoneService;

    @PostMapping("/create")
    public ResponseEntity<?> createZone(
            @RequestBody @Valid ZoneCreateReqDTO reqDTO
    ) {
        ZoneResDTO resDTO = zoneService.createZone(reqDTO);
        return ApiResponse.created(resDTO, "구역이 생성되었습니다.");
    }

    @GetMapping("/list")
    public ResponseEntity<?> getZoneList(
    ) {
        List<ZoneListResDTO> resDTO = zoneService.getZoneList();
        return ApiResponse.ok(resDTO);
    }

    @PutMapping("/update/{zoneId}")
    public ResponseEntity<?> updateZone(
            @RequestBody @Valid ZoneUpdateReqDTO reqDTO,
            @PathVariable UUID zoneId
    ) {
        ZoneResDTO resDTO = zoneService.updateZone(reqDTO, zoneId);
        return ApiResponse.ok(resDTO, "구역이 수정되었습니다.");
    }



}
