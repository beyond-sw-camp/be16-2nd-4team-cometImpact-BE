package com.beyond.jellyorder.domain.ingredient.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.ingredient.dto.*;
import com.beyond.jellyorder.domain.ingredient.service.IngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 식자재 관련 요청을 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/ingredient")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    /**
     * 새로운 식자재 등록
     */
    @PostMapping("/create")
    public ResponseEntity<?> createIngredient(
            @RequestBody @Valid IngredientCreateReqDto reqDto) {

        IngredientCreateResDto resDto = ingredientService.create(reqDto);
        return ApiResponse.created(resDto, resDto.getName() + " 식자재가 정상적으로 저장되었습니다.");
    }

    @GetMapping("/{storeId}/ingredients")
    public ResponseEntity<?> getIngredients(@PathVariable String storeId) {
        IngredientListResDto res = ingredientService.getIngredientsByStoreId(storeId);
        return ResponseEntity.ok(ApiResponse.ok(res, "원재료 목록이 정상적으로 조회되었습니다."));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteIngredient(@RequestBody @Valid IngredientDeleteReqDto reqDto) {
        IngredientDeleteResDto res = ingredientService.delete(reqDto);
        return ResponseEntity.ok(ApiResponse.ok(res, "식자재가 정상적으로 삭제되었습니다."));
    }
}
