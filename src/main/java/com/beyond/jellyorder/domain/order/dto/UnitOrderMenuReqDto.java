package com.beyond.jellyorder.domain.order.dto;

import com.beyond.jellyorder.domain.option.dto.MainOptionReqDto;
import com.beyond.jellyorder.domain.option.dto.SubOptionReqDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "메뉴 ID는 필수값입니다.")
    private UUID menuId;

    @NotNull(message = "수량은 필수값입니다.")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    private Integer quantity;

    @Valid
    private List<SubOptionReqDto> optionList;
}
