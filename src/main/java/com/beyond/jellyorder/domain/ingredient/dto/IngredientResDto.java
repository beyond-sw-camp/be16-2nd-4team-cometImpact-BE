package com.beyond.jellyorder.domain.ingredient.dto;

import com.beyond.jellyorder.domain.ingredient.domain.IngredientStatus;
import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientResDto {
    private UUID id;
    private String name;
    private IngredientStatus status;
}