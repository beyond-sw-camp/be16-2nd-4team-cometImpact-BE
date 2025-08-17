package com.beyond.jellyorder.domain.order.dto;

import com.beyond.jellyorder.domain.option.dto.MainOptionReqDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/** 단위 주문 메뉴 DTO */
@Getter
@Setter
@NoArgsConstructor
public class UnitOrderMenuReqDto {
    private UUID menuId;
    private Integer quantity;
    private List<MainOptionReqDto> mainOptions;
}
