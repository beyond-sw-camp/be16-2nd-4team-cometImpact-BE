package com.beyond.jellyorder.domain.category.domain;

import com.beyond.jellyorder.common.BaseIdAndTimeEntity;
import com.beyond.jellyorder.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

/**
 * 카테고리(Category) 도메인 엔티티.
 * 매장(Store) 내에서 메뉴를 분류하는 단위이며,
 * 이름과 설명을 포함한다.
 *
 * 상위 BaseIdAndTimeEntity를 상속받아 ID, 생성일, 수정일을 공통 관리한다.
 */
@Entity
@Table(name = "category")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = """
  update category
     set deleted = true,
         deleted_at = now()
   where id = ?
""")
public class Category extends BaseIdAndTimeEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    @ToString.Exclude
    private Store store;

    @Column(length = 20, nullable = false)
    private String name;

    @Column(length = 255, nullable = false)
    private String description;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
