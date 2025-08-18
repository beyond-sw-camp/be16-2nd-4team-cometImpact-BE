package com.beyond.jellyorder.domain.menu.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.menu.dto.*;
import com.beyond.jellyorder.domain.menu.service.MenuService;
import com.beyond.jellyorder.domain.option.dto.OptionAddReqDto;
import com.beyond.jellyorder.domain.option.dto.OptionAddResDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 메뉴 관련 요청을 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('STORE')")
    public ResponseEntity<?> createMenu(@ModelAttribute @Valid MenuCreateReqDto reqDto) {
        MenuCreateResDto resDto = menuService.create(reqDto);
        return ApiResponse.created(resDto, resDto.getName() + " 메뉴가 정상적으로 저장되었습니다.");
    }

    @PostMapping("/option/add")
    @PreAuthorize("hasRole('STORE')")
    public ResponseEntity<?> addOptionsToMenu(@RequestBody @Valid OptionAddReqDto reqDto) {
        OptionAddResDto resDto = menuService.addOptionsToMenu(reqDto);
        return ApiResponse.ok(resDto, "옵션이 정상적으로 추가되었습니다.");
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('STORE_TABLE')")
    public ResponseEntity<?> listMenusForUser() {
        List<MenuUserResDto> res = menuService.getMenusForUserByStoreId();
        return ApiResponse.ok(res,
                res.isEmpty() ? "해당 매장의 메뉴가 없습니다." : "메뉴 목록(유저용)이 정상적으로 조회되었습니다.");
    }

    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('STORE')")
    public ResponseEntity<?> listMenusForAdmin() {
        List<MenuAdminResDto> res = menuService.getMenusForAdminByStoreId();
        return ApiResponse.ok(res,
                res.isEmpty() ? "해당 매장의 메뉴가 없습니다." : "메뉴 목록(관리자용)이 정상적으로 조회되었습니다.");
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('STORE')")
    public ResponseEntity<?> deleteMenu(@RequestBody @Valid MenuDeleteReqDto reqDto) {
        menuService.deleteMenuById(reqDto.getMenuId());
        return ApiResponse.ok(null, "메뉴가 정상적으로 삭제되었습니다.");
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STORE')")
    public ResponseEntity<?> updateMenu(
            @ModelAttribute @Valid MenuUpdateReqDto reqDto
    ) {
        // PathVariable → DTO로 주입 (클라이언트 body에 menuId 보낼 필요 없음)
        reqDto.setMenuId(reqDto.getMenuId());

        MenuAdminResDto res = menuService.update(reqDto);
        return ApiResponse.ok(res, "메뉴가 수정되었습니다.");
    }
}
