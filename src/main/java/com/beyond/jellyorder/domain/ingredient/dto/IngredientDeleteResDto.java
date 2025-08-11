package com.beyond.jellyorder.domain.ingredient.dto;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IngredientDeleteResDto {
    private UUID ingredientId;
    private String ingredientName;
    private List<AffectedMenuDto> affectedMenus;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AffectedMenuDto {
        private UUID id;
        private String name;
    }
}
