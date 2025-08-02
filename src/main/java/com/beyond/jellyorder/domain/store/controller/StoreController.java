package com.beyond.jellyorder.domain.store.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.common.auth.AuthService;
import com.beyond.jellyorder.common.auth.JwtTokenProvider;
import com.beyond.jellyorder.common.auth.RefreshTokenDto;
import com.beyond.jellyorder.domain.store.dto.LoginRequestDto;
import com.beyond.jellyorder.domain.store.dto.LoginResponseDto;
import com.beyond.jellyorder.domain.store.dto.StoreCreateDto;
import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/store")
@RequiredArgsConstructor

public class StoreController {
    private final StoreService storeService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    /* Store 회원가입 Controller */
    @PostMapping("/create")
    public ResponseEntity<?> save(@RequestBody @Valid StoreCreateDto storeCreateDto) {
        UUID loginId = storeService.save(storeCreateDto);
        return ApiResponse.created(loginId, "회원가입 완료되었습니다."); /* 리턴값 UUID로 수정 완료, 주석 삭제 하고 사용하세요! */
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        Store store = storeService.doLogin(loginRequestDto);
        String storeAccessToken = jwtTokenProvider.createStoreAtToken(store);
        String storeRefreshToken = jwtTokenProvider.createStoreRtToken(store);

        LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                .accessToken(storeAccessToken)
                .refreshToken(storeRefreshToken)
                .build();

        return ApiResponse.ok(loginResponseDto, "로그인 완료");
    }

    @PostMapping("/refresh-at")
    public ResponseEntity<?> generateNewAt(@RequestBody RefreshTokenDto refreshTokenDto) {
        Store store = authService.validateStoreRt(refreshTokenDto.getRefreshToken());

        String accessToken = jwtTokenProvider.createStoreAtToken(store);
        LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                .accessToken(accessToken)
                .build();

        return ApiResponse.ok("StoreAccessToken 발급 성공"); /* 프론트 개발 후 리턴 값 변경 예정*/
    }



}
