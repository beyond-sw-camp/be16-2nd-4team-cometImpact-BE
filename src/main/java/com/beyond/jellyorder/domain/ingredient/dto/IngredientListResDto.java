package com.beyond.jellyorder.domain.ingredient.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientListResDto {
    private List<IngredientResDto> ingredients;
}