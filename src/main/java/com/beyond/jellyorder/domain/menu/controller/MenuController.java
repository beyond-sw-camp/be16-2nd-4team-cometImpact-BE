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

import java.util.List;

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

    @GetMapping("/store/{storeId}/name/{menuName}") // 추후 Authentication 도입 시 storeId 필요 X, menuName 대신 UUID를 body를 통해 전달 예정
    public ResponseEntity<?> getMenuByStoreIdAndName(
            @PathVariable String storeId,
            @PathVariable String menuName) {

        MenuCreateResDto resDto = menuService.getMenuByStoreIdAndName(storeId, menuName);
        return ApiResponse.ok(resDto, "단일 메뉴 조회가 정상적으로 조회되었습니다.");
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<?> getMenusByStoreId(@PathVariable String storeId) {
        List<MenuCreateResDto> resDtos = menuService.getMenusByStoreId(storeId);
        return ApiResponse.ok(resDtos, "메뉴 목록이 정상적으로 조회되었습니다.");
    }
}
