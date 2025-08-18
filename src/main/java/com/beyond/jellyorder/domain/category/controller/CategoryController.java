package com.beyond.jellyorder.domain.category.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.category.dto.*;
import com.beyond.jellyorder.domain.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 카테고리 관련 요청을 처리하는 REST 컨트롤러 클래스.
 * 클라이언트로부터 전달된 카테고리 생성 요청을 받아 서비스 계층에 위임하고,
 * 결과를 표준 응답 형식으로 반환한다.
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('STORE')")
    public ResponseEntity<?> createCategory(@RequestBody @Valid CategoryCreateReqDto reqDto) {
        CategoryCreateResDto resDto = categoryService.create(reqDto);
        return ApiResponse.created(resDto, reqDto.getName() + " 카테고리가 정상적으로 저장되었습니다.");
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('STORE') or hasRole('STORE_TABLE')")
    public ResponseEntity<?> getCategoriesByStore() {
        List<GetCategoryResDto> resDtoList = categoryService.getCategoriesByStore();
        return ApiResponse.ok(resDtoList, "카테고리 목록이 정상적으로 조회되었습니다.");
    }

    @PutMapping("/modify")
    @PreAuthorize("hasRole('STORE')")
    public ResponseEntity<?> modifyCategory(@RequestBody @Valid CategoryModifyReqDto reqDto) {
        CategoryModifyResDto resDto = categoryService.modifyCategory(reqDto);
        return ApiResponse.ok(resDto, "카테고리가 정상적으로 수정되었습니다.");
    }

    @DeleteMapping("/delete/{categoryName}")
    @PreAuthorize("hasRole('STORE')")
    public ResponseEntity<?> deleteCategory (@PathVariable String categoryName){
        categoryService.deleteCategory(categoryName);
        return ApiResponse.ok(null, "카테고리가 정상적으로 삭제되었습니다.");
    }
}