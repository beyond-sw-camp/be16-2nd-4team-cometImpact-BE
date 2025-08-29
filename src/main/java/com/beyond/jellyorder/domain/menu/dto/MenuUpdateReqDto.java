package com.beyond.jellyorder.domain.menu.dto;

import com.beyond.jellyorder.domain.option.mainOption.dto.MainOptionDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuUpdateReqDto {

    // 컨트롤러에서 PathVariable 주입
    @NotNull(message = "menuId는 필수입니다.")
    private UUID menuId;

    // 카테고리 변경 가능
    @NotBlank(message = "카테고리명은 필수입니다.")
    private String categoryName;

    // 새 카테고리를 만들 때만 사용 (선택)
    @Size(max = 255)
    private String categoryDescription;

    @NotBlank(message = "메뉴 이름은 필수입니다.")
    @Size(max = 30)
    private String name;

    @NotNull(message = "가격은 필수입니다.")
    @Min(0)
    private Integer price;

    @NotBlank(message = "설명은 필수입니다.")
    @Size(max = 255)
    private String description;

    @Size(max = 255)
    private String origin;

    @Min(-1)
    private Integer salesLimit; // 기본 -1 (null이면 변경 없음으로 해석 가능)

    // 재고/판매 상태 정책에 따라 사용 (예: 수동품절 토글 또는 onSale 표기)
    private Boolean onSale; // null이면 변경 안 함

    // 이미지 교체 없으면 null/empty로 전송
    private MultipartFile imageFile;

    /**
     * 전체 스냅샷 (메인/서브 옵션 트리)
     * - 각 MainOptionDto에는 selectionType(필수)이 포함되어야 함
     * - @Valid로 중첩 DTO Bean Validation 수행
     */
    @Valid
    @Builder.Default
    private List<MainOptionDto> mainOptions = new ArrayList<>();

    /**
     * 연결할 식자재 ID 목록 (null이면 변경 없음으로 처리 가능)
     */
    @Builder.Default
    private List<UUID> ingredientIds = new ArrayList<>();
}
