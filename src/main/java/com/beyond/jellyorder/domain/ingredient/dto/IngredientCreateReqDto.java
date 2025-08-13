package com.beyond.jellyorder.domain.ingredient.dto;

import com.beyond.jellyorder.domain.ingredient.domain.IngredientStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 식자재 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class IngredientCreateReqDto {
    /**
     * 식자재명 (예: 양상추, 체다치즈)
     */
    @NotBlank(message = "식자재명은 필수입니다.")
    @Size(max = 20, message = "식자재명은 20자 이하로 입력해야 합니다.")
    private String name;

    /**
     * 재고 상태 (예: SUFFICIENT, INSUFFICIENT, EXHAUSTED)
     */
    @NotNull(message = "상태는 필수 선택입니다.")
    private IngredientStatus status;
}
