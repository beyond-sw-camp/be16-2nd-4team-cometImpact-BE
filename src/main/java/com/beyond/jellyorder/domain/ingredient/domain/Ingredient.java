package com.beyond.jellyorder.domain.ingredient.domain;

import com.beyond.jellyorder.common.BaseIdAndTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 재료(Ingredient) 엔티티.
 * 매장에서 사용하는 식자재(예: 치즈, 소스 등)를 정의한다.
 * 기본적으로 이름(name)과 재고 상태(status)를 포함하며,
 * BaseIdAndTimeEntity를 상속받아 UUID 기반의 식별자와 생성/수정 시각을 함께 관리한다.
 */
@Getter
@Entity
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "ingredient")
public class Ingredient extends BaseIdAndTimeEntity {

    /**
     * 카테고리를 소속시킬 매장의 고유 식별자.
     * 현재는 테스트 및 개발 편의상 String 타입으로 지정되어 있으며,
     * 추후 UUID 타입으로 변경 예정이다.
     */
    @Column(name = "store_id", nullable = false)
    private String storeId;

    /**
     * 식자재명.
     * - 최대 20자까지 입력 가능
     * - null 불가
     * - 예: '체다 치즈', '양상추', '바베큐 소스'
     */
    @Column(length = 20, nullable = false)
    private String name;

    /**
     * 재고 상태.
     * - Enum 클래스 IngredientStatus에 정의된 값 사용
     * - EnumType.STRING으로 저장하여 데이터베이스에 문자열 형태로 보관 (가독성 및 안정성 향상)
     * - 기본값은 SUFFICIENT (충분) 상태
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private IngredientStatus status = IngredientStatus.SUFFICIENT;

    // 추후: 재고 상태 변경 메서드 등 도메인 로직 추가 가능 (e.g., updateStatus(IngredientStatus.INSUFFICIENT))
}
