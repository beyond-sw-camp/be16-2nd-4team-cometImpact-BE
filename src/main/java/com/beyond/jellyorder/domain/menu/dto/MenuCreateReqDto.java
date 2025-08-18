package com.beyond.jellyorder.domain.menu.dto;

import com.beyond.jellyorder.domain.category.domain.Category;
import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.option.mainOption.dto.MainOptionDto;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuCreateReqDto {
    @NotNull(message = "카테고리명은 필수입니다.")
    private String categoryName;

    @NotBlank(message = "메뉴 이름(name)은 필수입니다.")
    @Size(max = 30, message = "메뉴 이름은 30자 이하로 입력해주세요.")
    private String name;

    @NotNull(message = "가격(price)은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private Integer price;

    @NotBlank(message = "설명(description)은 필수입니다.")
    @Size(max = 255, message = "설명은 255자 이하로 입력해주세요.")
    private String description;

    @Size(max = 255, message = "원산지(origin)는 255자 이하로 입력해주세요.")
    private String origin;

    @Min(value = -1, message = "판매 한도는 -1 이상이어야 합니다.")
    private Integer salesLimit = -1;

    @Builder.Default
    private List<String> ingredients = new ArrayList<>();

    @NotNull(message = "이미지 파일(imageFile)은 필수입니다.")
    private MultipartFile imageFile;

    private List<MainOptionDto> mainOptions;

    @Builder.Default
    private boolean onSale = true;

    public Menu toEntity(Category category, String imageUrl) {
        if (category == null) throw new IllegalArgumentException("카테고리는 필수입니다.");
        if (imageUrl == null || imageUrl.isBlank()) throw new IllegalArgumentException("이미지 URL은 필수입니다.");

        return Menu.builder()
                .category(category)
                .name(getName())
                .price(getPrice())
                .description(getDescription())
                .imageUrl(imageUrl)
                .origin(getOrigin())
                .salesLimit(getSalesLimit() != null ? getSalesLimit() : -1)
                .salesToday(0)
                .build();
    }
}
