package com.beyond.jellyorder.domain.option.dto;

import com.beyond.jellyorder.domain.option.mainOption.dto.MainOptionDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionAddReqDto {

    @NotBlank(message = "메뉴 ID(menuId)는 필수입니다.")
    private String menuId;

    @NotEmpty(message = "옵션 리스트는 비어있을 수 없습니다.")
    private List<MainOptionDto> mainOptions;
}