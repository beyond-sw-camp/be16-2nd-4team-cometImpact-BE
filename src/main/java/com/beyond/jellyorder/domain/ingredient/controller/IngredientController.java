package com.beyond.jellyorder.domain.ingredient.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.ingredient.dto.*;
import com.beyond.jellyorder.domain.ingredient.service.IngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 식자재 관련 요청을 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('STORE')")
    public ResponseEntity<?> createIngredient(@RequestBody @Valid IngredientCreateReqDto reqDto) {
        IngredientCreateResDto resDto = ingredientService.create(reqDto);
        return ApiResponse.created(resDto, resDto.getName() + " 식자재가 정상적으로 저장되었습니다.");
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('STORE') or hasRole('STORE_TABLE')")
    public ResponseEntity<?> getIngredients() {
        IngredientListResDto res = ingredientService.getIngredientsByStoreId();
        return ResponseEntity.ok(ApiResponse.ok(res, "식자재 목록이 정상적으로 조회되었습니다."));
    }

    @DeleteMapping("/delete/{ingredientId}")
    @PreAuthorize("hasRole('STORE')")
    public ResponseEntity<?> deleteIngredient(@PathVariable String ingredientId) {
        IngredientDeleteResDto res = ingredientService.delete(ingredientId);
        return ResponseEntity.ok(ApiResponse.ok(res, "식자재가 정상적으로 삭제되었습니다."));
    }

    @PatchMapping("/modify")
    @PreAuthorize("hasRole('STORE')")
    public ResponseEntity<?> modifyIngredient(@RequestBody @Valid IngredientModifyReqDto req) {
        IngredientModifyResDto res = ingredientService.modify(req);
        return ResponseEntity.ok(res);
    }
}
