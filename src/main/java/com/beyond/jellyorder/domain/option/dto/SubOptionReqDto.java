package com.beyond.jellyorder.domain.option.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/** 주문의 서브옵션 전달용 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubOptionReqDto {
    @NotNull(message = "옵션 ID는 필수값입니다.")
    private String subOptionId;
    /// 현재 피그마 로직에서는 수량이 필요없어보임. 추후, 옵션에도 수량 조절 기능 추가 시 주석 해제.
//    private Integer quantity;
//    private Integer price;
}
