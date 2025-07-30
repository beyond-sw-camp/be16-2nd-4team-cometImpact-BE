package com.beyond.jellyorder.domain.ingredient.dto;

import com.beyond.jellyorder.domain.ingredient.domain.IngredientStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 식자재 생성 응답 DTO
 */
@Getter
@Builder
public class IngredientCreateResDto {
    private UUID id;
    private String name;
    private IngredientStatus status;
}
