package com.beyond.jellyorder.domain.menu.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.menu.dto.MenuCreateReqDto;
import com.beyond.jellyorder.domain.menu.dto.MenuCreateResDto;
import com.beyond.jellyorder.domain.menu.dto.MenuDeleteReqDto;
import com.beyond.jellyorder.domain.menu.dto.MenuListResDto;
import com.beyond.jellyorder.domain.menu.service.MenuService;
import com.beyond.jellyorder.domain.option.dto.OptionAddReqDto;
import com.beyond.jellyorder.domain.option.dto.OptionAddResDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/store/name/{menuName}")
    @PreAuthorize("hasRole('STORE') or hasRole('STORE_TABLE')")
    public ResponseEntity<?> getMenuByStoreIdAndName(@PathVariable String menuName) {
        MenuCreateResDto resDto = menuService.getMenuByStoreIdAndName(menuName);
        return ApiResponse.ok(resDto, "단일 메뉴 조회가 정상적으로 조회되었습니다.");
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('STORE','STORE_TABLE')")
    public ResponseEntity<?> listMenus() {
        List<MenuListResDto> res = menuService.getMenusByStoreId();
        return ApiResponse.ok(res,
                res.isEmpty() ? "해당 매장의 메뉴가 없습니다." : "메뉴 목록이 정상적으로 조회되었습니다.");
    }

    @PostMapping("/option/add")
    @PreAuthorize("hasRole('STORE')")
    public ResponseEntity<?> addOptionsToMenu(@RequestBody @Valid OptionAddReqDto reqDto) {
        OptionAddResDto resDto = menuService.addOptionsToMenu(reqDto);
        return ApiResponse.ok(resDto, "옵션이 정상적으로 추가되었습니다.");
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('STORE')")
    public ResponseEntity<?> deleteMenu(@RequestBody @Valid MenuDeleteReqDto reqDto) {
        menuService.deleteMenuById(reqDto.getMenuId());
        return ApiResponse.ok(null, "메뉴가 정상적으로 삭제되었습니다.");
    }
}
