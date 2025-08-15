package com.beyond.jellyorder.domain.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 카테고리 생성 요청을 위한 DTO 클래스.
 * 클라이언트가 카테고리를 생성할 때 전달해야 할 필드들을 정의하며,
 * 유효성 검사 어노테이션을 통해 요청값의 형식을 검증한다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateReqDto {
    /**
     * 카테고리 이름.
     * 필수이며, 최대 20자까지 입력 가능하다.
     */
    @NotBlank
    @Size(max = 20)
    private String name;

    /**
     * 카테고리 설명 문구.
     * 필수이며, 최대 255자까지 입력 가능하다.
     */
    @NotBlank
    @Size(max = 255)
    private String description;
}
