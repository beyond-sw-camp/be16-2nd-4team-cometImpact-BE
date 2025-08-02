package com.beyond.jellyorder.domain.store.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.store.dto.LoginRequestDto;
import com.beyond.jellyorder.domain.store.dto.StoreCreateDto;
import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    /* Store 회원가입 Controller */
    @PostMapping("/create")
    public ResponseEntity<?> save(@RequestBody @Valid StoreCreateDto storeCreateDto) {
        UUID loginId = storeService.save(storeCreateDto);
        return ApiResponse.created(loginId, "회원가입 완료되었습니다."); /* 리턴값 UUID로 수정 완료, 주석 삭제 하고 사용하세요! */
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        Store store = storeService.doLogin(loginRequestDto);
        return ApiResponse.ok("로그인 완료, 추후 토큰 발급 시 토큰 리턴 예정입니다.");
    }


}
