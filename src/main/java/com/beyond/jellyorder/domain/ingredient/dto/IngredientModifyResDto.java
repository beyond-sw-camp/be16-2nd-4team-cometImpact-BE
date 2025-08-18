// 응답
package com.beyond.jellyorder.domain.ingredient.dto;

import com.beyond.jellyorder.domain.ingredient.domain.IngredientStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class IngredientModifyResDto {
    private UUID id;
    private String name;
    private IngredientStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}