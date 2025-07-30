package com.beyond.jellyorder.domain.storetable.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.storetable.dto.StoreTableCreateReqDTO;
import com.beyond.jellyorder.domain.storetable.dto.StoreTableResDTO;
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

}
