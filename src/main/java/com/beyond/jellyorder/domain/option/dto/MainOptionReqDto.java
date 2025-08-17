package com.beyond.jellyorder.domain.option.dto;

import lombok.*;

import java.util.List;

/** 주문의 메인옵션 전달용 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainOptionReqDto {
    private String mainOptionId;
    private String name;
    private List<SubOptionReqDto> subOptions;
}
