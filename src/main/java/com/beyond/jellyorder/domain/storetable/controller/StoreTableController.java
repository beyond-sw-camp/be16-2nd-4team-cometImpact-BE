package com.beyond.jellyorder.domain.storetable.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.common.auth.StoreTableJwtTokenProvider;
import com.beyond.jellyorder.domain.storetable.dto.*;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
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
//    @PreAuthorize("hasRole('STORE')") 토큰 발급 시 추가 예정
public class StoreTableController {

    private final StoreTableService storeTableService;
    private final StoreTableJwtTokenProvider jwtTokenProvider;

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

    @PostMapping("/do-login")
    public ResponseEntity<?> storeTableLogin(@Valid @RequestBody StoreTableLoginReqDTO storeTableLoginReqDTO) {

        StoreTable storeTable = storeTableService.doLogin(storeTableLoginReqDTO);
        String accessToken = jwtTokenProvider.createStoreTableAtToken(storeTable);
        String refreshToken = jwtTokenProvider.createStoreTableRtToken(storeTable);

        StoreTableLoginResDTO loginResDTO = StoreTableLoginResDTO
                .builder()
                .storeTableAccessToken(accessToken)
                .storeTableRefreshToken(refreshToken)
                .build();

        return ApiResponse.ok(loginResDTO, "테이블 로그인 완료!");
    }
}
