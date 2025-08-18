package com.beyond.jellyorder.domain.ingredient.dto;

import com.beyond.jellyorder.domain.ingredient.domain.IngredientStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientModifyReqDto {
    @NotNull(message = "ingredientId는 필수입니다.")
    private UUID ingredientId;

    @Size(max = 20, message = "식자재명은 최대 20자까지 허용됩니다.")
    private String name; // 선택

    private IngredientStatus status; // 선택
}
