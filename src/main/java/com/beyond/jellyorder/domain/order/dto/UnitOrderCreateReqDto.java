package com.beyond.jellyorder.domain.order.dto;

import com.beyond.jellyorder.domain.option.dto.SubOptionReqDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/** 단위 주문 요청 DTO */
@Getter
@Setter
@NoArgsConstructor
public class UnitOrderCreateReqDto {
    @NotEmpty(message = "주문 항목이 비어있습니다.")
    @Valid   // 내부 메뉴 리스트까지 검증
    List<UnitOrderMenuReqDto> menuList;
}
