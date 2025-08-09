package com.beyond.jellyorder.domain.option.mainOption.dto;

import com.beyond.jellyorder.domain.option.subOption.dto.SubOptionDto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainOptionDto {

    @NotBlank(message = "메인 옵션 이름은 필수입니다.")
    private String name;

    // 서브 옵션은 없어도 될 수 있음
    private List<SubOptionDto> subOptions;
}
