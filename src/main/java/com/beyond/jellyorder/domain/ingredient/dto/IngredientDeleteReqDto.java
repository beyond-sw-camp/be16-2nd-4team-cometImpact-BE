package com.beyond.jellyorder.domain.ingredient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IngredientDeleteReqDto {
    @NotBlank
    private String storeId;     // 소속 검증용
    @NotNull
    private UUID ingredientId;  // 삭제 대상
}