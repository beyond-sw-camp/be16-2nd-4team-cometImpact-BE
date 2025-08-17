package com.beyond.jellyorder.domain.option.dto;

import lombok.*;

/** 주문의 서브옵션 전달용 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubOptionReqDto {
    private String subOptionId;
    private Integer quantity;
    private Integer price;
}
