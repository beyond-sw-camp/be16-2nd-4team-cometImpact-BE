package com.beyond.jellyorder.domain.storetable.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.common.auth.AuthService;
import com.beyond.jellyorder.common.auth.RefreshTokenDto;
import com.beyond.jellyorder.common.auth.StoreTableJwtTokenProvider;
import com.beyond.jellyorder.domain.storetable.dto.storeTable.*;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.service.StoreTableService;
import jakarta.annotation.security.PermitAll;
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
    private final StoreTableJwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

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

    @DeleteMapping("/{storeTableId}")
    public ResponseEntity<?> deleteStoreTable(
            @PathVariable UUID storeTableId
    ) {
        String deleteStoreTableName = storeTableService.deleteStoreTable(storeTableId);
        return ApiResponse.ok(deleteStoreTableName, "삭제가 완료되었습니다.");
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/do-login")
    public ResponseEntity<?> storeTableLogin(@Valid @RequestBody StoreTableLoginReqDTO storeTableLoginReqDTO) {

        StoreTable storeTable = storeTableService.doLogin(storeTableLoginReqDTO);
        String accessToken = jwtTokenProvider.createStoreTableAtToken(storeTable);
        String refreshToken = jwtTokenProvider.createStoreTableRtToken(storeTable);

        StoreTableLoginResDTO loginResDTO = StoreTableLoginResDTO
                .builder()
                .storeTableAccessToken(accessToken)
                .storeTableRefreshToken(refreshToken)
                .storeTableId(storeTable.getId())
                .storeId(storeTable.getStore().getId())
                .build();

        return ApiResponse.ok(loginResDTO, storeTableLoginReqDTO.getTableName() + " 테이블 로그인 완료!");
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/refresh-at")
    public ResponseEntity<?> storeTableNewAt(@RequestBody RefreshTokenDto refreshTokenDto) {
        StoreTable storeTable = authService.validateStoreTableRt(refreshTokenDto.getRefreshToken());

        String storeTableAccessToken = jwtTokenProvider.createStoreTableAtToken(storeTable);
        StoreTableLoginResDTO loginResponseDto = StoreTableLoginResDTO.builder()
                .storeTableAccessToken(storeTableAccessToken)
                .build();

        return ApiResponse.ok(loginResponseDto, "테이블 토큰 재발급 완료!"); /* 프론트 개발 후 리턴 값 변경 예정*/
    }
}
