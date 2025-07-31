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
     * 재료를 등록할 대상 매장의 고유 식별자.
     * 빈 문자열은 허용되지 않으며, 일반적으로 UUID 형식의 문자열이다.
     * (2025-07-31 기준) 테스트 편의를 위해 현재는 String 타입으로 임시 지정되어 있음.
     * 추후 로그인 및 인증 기능이 구현되면, 인증된 사용자 정보(Authentication 객체)로부터
     * storeId(UUID)를 추출할 수 있으므로 본 필드는 제거될 예정임.
     */
    @NotBlank
    private String storeId;

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
