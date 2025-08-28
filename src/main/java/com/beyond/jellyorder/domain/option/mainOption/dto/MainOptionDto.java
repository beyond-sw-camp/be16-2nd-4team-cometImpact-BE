package com.beyond.jellyorder.domain.option.mainOption.dto;

import com.beyond.jellyorder.domain.option.mainOption.domain.MainOption;
import com.beyond.jellyorder.domain.option.mainOption.domain.OptionSelectionType;
import com.beyond.jellyorder.domain.option.subOption.domain.SubOption;
import com.beyond.jellyorder.domain.option.subOption.dto.SubOptionDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainOptionDto {

    @NotBlank(message = "메인 옵션 이름은 필수입니다.")
    private String name;

    @NotNull(message = "메인 옵션의 선택 유형은 필수입니다.")
    private OptionSelectionType selectionType; // REQUIRED_SINGLE / REQUIRED_MULTIPLE / OPTIONAL_SINGLE / OPTIONAL_MULTIPLE

    // 서브 옵션은 없어도 될 수 있음
    private List<SubOptionDto> subOptions;

    public MainOption toEntity() {
        String n = (name == null) ? "" : name.trim();
        if (n.isEmpty()) {
            throw new IllegalArgumentException("메인 옵션 이름은 필수입니다.");
        }
        if (selectionType == null) {
            throw new IllegalArgumentException("메인 옵션의 선택 유형은 필수입니다.");
        }

        // 중복명 검증(트림 기준)
        List<SubOption> subs = new ArrayList<>();
        Set<String> dupGuard = new HashSet<>();
        if (subOptions != null) {
            for (int i = 0; i < subOptions.size(); i++) {
                SubOptionDto dto = subOptions.get(i);
                SubOption so = dto.toEntity();
                String subName = (so.getName() == null) ? "" : so.getName().trim();
                if (!dupGuard.add(subName)) {
                    throw new IllegalArgumentException("메인 옵션 '" + n + "'에 중복된 서브 옵션이 존재합니다: " + subName);
                }
                subs.add(so);
            }
        }

        MainOption mo = MainOption.builder()
                .name(n)
                .selectionType(selectionType)
                .build();
        mo.setSubOptions(subs); // 역참조 연결은 Service 계층에서 처리
        return mo;
    }

    public static MainOptionDto fromEntity(MainOption e) {
        return MainOptionDto.builder()
                .name(e.getName())
                .selectionType(e.getSelectionType())
                .subOptions(
                        (e.getSubOptions() == null) ? List.of()
                                : e.getSubOptions().stream()
                                .map(SubOptionDto::fromEntity)
                                .toList()
                )
                .build();
    }
}
