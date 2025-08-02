package com.beyond.jellyorder.domain.menu.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.menu.dto.MenuCreateReqDto;
import com.beyond.jellyorder.domain.menu.dto.MenuCreateResDto;
import com.beyond.jellyorder.domain.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 메뉴 관련 요청을 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 새로운 메뉴 등록
     */
    @PostMapping("/create")
    public ResponseEntity<?> createMenu(
            @ModelAttribute @Valid MenuCreateReqDto reqDto) {

        MenuCreateResDto resDto = menuService.create(reqDto);
        return ApiResponse.created(resDto, resDto.getName() + " 메뉴가 정상적으로 저장되었습니다.");
    }
}
