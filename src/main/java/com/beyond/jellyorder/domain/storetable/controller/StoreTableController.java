package com.beyond.jellyorder.domain.storetable.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.storetable.dto.*;
import com.beyond.jellyorder.domain.storetable.service.StoreTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/store-table")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STORE')")
public class StoreTableController {

    private final StoreTableService storeTableService;

    @PostMapping("/create")
    public ResponseEntity<?> createStoreTable(
            @RequestBody @Valid StoreTableCreateReqDTO reqDTO
    ) {
        reqDTO.validate();
        List<StoreTableResDTO> resDTO = storeTableService.createTables(reqDTO);
        return ApiResponse.created(resDTO, "테이블이 생성되었습니다.");
    }

    @GetMapping("/list")
    public ResponseEntity<?> getStoreTableList(
    ) {
        List<StoreTableListResDTO> resDTOs = storeTableService.getStoreTableList();
        return ApiResponse.ok(resDTOs);
    }

    @PutMapping("/update/{storeTableId}")
    public ResponseEntity<?> updateStoreTable(
            @RequestBody @Valid StoreTableUpdateReqDTO reqDTO,
            @PathVariable UUID storeTableId
    ) {
        StoreTableResDTO resDTO = storeTableService.updateStoreTable(reqDTO, storeTableId);
        return ApiResponse.ok(resDTO, "구역이 수정되었습니다.");
    }

}
