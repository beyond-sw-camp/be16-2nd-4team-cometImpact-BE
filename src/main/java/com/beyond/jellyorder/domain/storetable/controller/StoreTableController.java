package com.beyond.jellyorder.domain.storetable.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.storetable.dto.*;
import com.beyond.jellyorder.domain.storetable.service.StoreTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/store-table")
@RequiredArgsConstructor
//    @PreAuthorize("hasRole('STORE')") 토큰 발급 시 추가 예정
public class StoreTableController {

    private final StoreTableService storeTableService;

    @PostMapping("/create/{storeLoginId}")
    public ResponseEntity<?> createStoreTable(
            @PathVariable String storeLoginId,
            @RequestBody @Valid StoreTableCreateReqDTO reqDTO
    ) {
        reqDTO.validate();
        List<StoreTableResDTO> resDTO = storeTableService.createTables(reqDTO, storeLoginId);
        return ApiResponse.created(resDTO, "테이블이 생성되었습니다.");
    }

    @GetMapping("/list/{storeLoginId}")
    public ResponseEntity<?> getStoreTableList(
            @PathVariable String storeLoginId
    ) {
        List<StoreTableListResDTO> resDTOs = storeTableService.getStoreTableList(storeLoginId);
        return ApiResponse.ok(resDTOs);
    }

    @PutMapping("/update/{storeTableId}/{storeLoginId}")
    public ResponseEntity<?> updateStoreTable(
            @RequestBody @Valid StoreTableUpdateReqDTO reqDTO,
            @PathVariable UUID storeTableId,
            @PathVariable String storeLoginId
    ) {
        StoreTableResDTO resDTO = storeTableService.updateStoreTable(reqDTO, storeTableId, storeLoginId);
        return ApiResponse.ok(resDTO, "구역이 수정되었습니다.");
    }

}
