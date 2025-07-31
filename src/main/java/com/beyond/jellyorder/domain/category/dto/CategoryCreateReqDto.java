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
     * 카테고리를 등록할 대상 매장의 고유 식별자.
     * 빈 문자열은 허용되지 않으며, 일반적으로 UUID 형식의 문자열이다.
     * (2025-07-31 기준) 테스트 편의를 위해 현재는 String 타입으로 임시 지정되어 있음.
     * 추후 로그인 및 인증 기능이 구현되면, 인증된 사용자 정보(Authentication 객체)로부터
     * storeId(UUID)를 추출할 수 있으므로 본 필드는 제거될 예정임.
     */
    @NotBlank
    private String storeId;

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
