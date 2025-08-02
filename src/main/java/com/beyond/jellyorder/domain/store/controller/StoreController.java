package com.beyond.jellyorder.domain.store.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.store.dto.StoreCreateDto;
import com.beyond.jellyorder.domain.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/store")
@RequiredArgsConstructor

public class StoreController {
    private final StoreService storeService;

    @PostMapping("/create")
    public ResponseEntity<?> save(@RequestBody @Valid StoreCreateDto storeCreateDto) {
        String loginId = storeService.save(storeCreateDto);
        return ApiResponse.created(loginId, "회원가입 완료되었습니다.");
    }


}
