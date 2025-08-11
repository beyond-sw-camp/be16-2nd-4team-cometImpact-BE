package com.beyond.jellyorder.domain.ingredient.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDto {
    private UUID id;
    private String name;
}