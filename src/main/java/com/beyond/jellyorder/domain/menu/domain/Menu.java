package com.beyond.jellyorder.domain.menu.domain;

import com.beyond.jellyorder.common.BaseIdAndTimeEntity;
import com.beyond.jellyorder.domain.category.domain.Category;
import com.beyond.jellyorder.domain.option.mainOption.domain.MainOption;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
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
    private Integer salesLimit = -1;

    @Builder.Default
    @Column(name = "sales_today", nullable = false)
    private Integer salesToday = 0;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MainOption> mainOptions;

    @OneToMany(mappedBy = "menu",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private List<MenuIngredient> menuIngredients = new ArrayList<>();

    @Builder.Default
    @Column(name = "sold_out", nullable = false)
    private boolean soldOut = false;


    // 하루판매 수량 증가 함수
    public void increaseSalesToday(Integer quantity) {
        this.salesToday += quantity;
        if (this.salesToday.equals(this.salesLimit)) {
            this.soldOut = true;
        }
    }

    // 하루판매 수량 감소 함수
    public void decreaseSalesToday(Integer quantity) {
        this.salesToday -= quantity;
        if (!this.salesToday.equals(this.salesLimit)) {
            this.soldOut = false;
        }
    }
}