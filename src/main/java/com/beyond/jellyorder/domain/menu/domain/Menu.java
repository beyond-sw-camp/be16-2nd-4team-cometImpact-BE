package com.beyond.jellyorder.domain.menu.domain;

import com.beyond.jellyorder.common.BaseIdAndTimeEntity;
import com.beyond.jellyorder.domain.category.domain.Category;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "menu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu extends BaseIdAndTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(length = 30, nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(length = 255, nullable = false)
    private String description;

    @Column(name = "image_url", length = 512, nullable = false)
    private String imageUrl;

    @Column(length = 255)
    private String origin;

    @Builder.Default
    @Column(name = "sales_limit", nullable = false)
    private Long salesLimit = -1L;

    @Builder.Default
    @Column(name = "sales_today", nullable = false)
    private Integer salesToday = 0;

//    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<MenuIngredient> menuIngredients;

    // 메뉴 수량 증가 함수
    public void increaseSalesLimit(Long quantity) {
        this.salesLimit += quantity;
    }

    // 메뉴 수량 감소 함수
    public void decreaseSalesLimit(Long quantity) {
        this.salesLimit -= quantity;
    }

    // 하루판매 수량 증가 함수
    public void increaseSalesToday(Integer quantity) {
        this.salesLimit += quantity;
    }

    // 하루판매 수량 감소 함수
    public void decreaseSalesToday(Integer quantity) {
        this.salesLimit -= quantity;
    }
}
