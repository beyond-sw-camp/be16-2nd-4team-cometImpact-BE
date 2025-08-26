package com.beyond.jellyorder.domain.menu.dto;

import com.beyond.jellyorder.domain.category.domain.Category;
import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.menu.domain.MenuStatus;
import com.beyond.jellyorder.domain.option.mainOption.dto.MainOptionDto;
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
public class MenuCreateReqDto {

    @NotBlank(message = "카테고리명은 필수입니다.")
    private String categoryName;

    @Size(max = 255)
    private String categoryDescription;

    @NotBlank(message = "메뉴 이름은 필수입니다.")
    @Size(max = 30, message = "메뉴 이름은 30자 이하로 입력해주세요.")
    private String name;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private Integer price;

    @NotBlank(message = "설명은 필수입니다.")
    @Size(max = 255, message = "설명은 255자 이하로 입력해주세요.")
    private String description;

    @Size(max = 255, message = "원산지는 255자 이하로 입력해주세요.")
    private String origin;

    @Min(value = -1, message = "판매 한도는 -1 이상이어야 합니다.")
    private Integer salesLimit = -1;

    /**
     * 신규 생성 시 연결할 식자재 ID 목록
     * multipart/form-data에서는 JSON 문자열 배열로 전송
     */
    @Builder.Default
    private List<UUID> ingredientIds = new ArrayList<>();

    /**
     * 옵션 트리 (메인/서브 옵션 구조)
     */
    private List<MainOptionDto> mainOptions;

    @NotNull(message = "이미지 파일은 필수입니다.")
    private MultipartFile imageFile;

    /**
     * 엔티티 변환
     */
    public Menu toEntity(Category category, String imageUrl) {
        if (category == null) throw new IllegalArgumentException("카테고리는 필수입니다.");
        if (imageUrl == null || imageUrl.isBlank()) throw new IllegalArgumentException("이미지 URL은 필수입니다.");

        return Menu.builder()
                .category(category)
                .name(name)
                .price(price)
                .description(description)
                .imageUrl(imageUrl)
                .origin(origin)
                .salesLimit(salesLimit != null ? salesLimit : -1)
                .salesToday(0)
                // 생성 시 기본은 ON_SALE, 이후 식자재 동기화 로직에서 상태 재계산됨
                .stockStatus(MenuStatus.ON_SALE)
                .build();
    }
}
