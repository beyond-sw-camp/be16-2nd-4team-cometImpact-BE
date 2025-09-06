package com.beyond.jellyorder.domain.menu.domain;

import com.beyond.jellyorder.common.BaseIdAndTimeEntity;
import com.beyond.jellyorder.domain.category.domain.Category;
import com.beyond.jellyorder.domain.option.mainOption.domain.MainOption;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static com.beyond.jellyorder.domain.menu.domain.MenuStatus.*;

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
    @Column(name = "stock_status", nullable = false)
    private MenuStatus stockStatus = ON_SALE;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 재고 상태 변환 함수
    public void changeStockStatus(MenuStatus newStatus) {
        this.stockStatus = newStatus;
    }

    // 수동 품절 설정
    public void markSoldOutManually() {
        this.stockStatus = MenuStatus.SOLD_OUT_MANUAL;
    }

    // 수동 품절을 해제 및 재판매
    public void markOnSale() {
        this.stockStatus = MenuStatus.ON_SALE;
    }

    // 하루판매 수량 증가 함수
    public void increaseSalesToday(Integer quantity) {
        this.salesToday += quantity;
        if (this.salesLimit != -1 && this.salesToday >= this.salesLimit
                && this.stockStatus != MenuStatus.SOLD_OUT_MANUAL) {
            this.stockStatus = MenuStatus.OUT_OF_STOCK;
        }
    }

    // 하루판매 수량 감소 함수
    public void decreaseSalesToday(Integer quantity) {
        this.salesToday -= quantity;
        if (!this.salesToday.equals(this.salesLimit) && !this.stockStatus.equals(SOLD_OUT_MANUAL)) {
            this.stockStatus = ON_SALE;
        }
    }

    public void addMenuIngredient(MenuIngredient mi) {
        if (this.menuIngredients == null) this.menuIngredients = new ArrayList<>();
        this.menuIngredients.add(mi);
        mi.setMenu(this);
    }
}