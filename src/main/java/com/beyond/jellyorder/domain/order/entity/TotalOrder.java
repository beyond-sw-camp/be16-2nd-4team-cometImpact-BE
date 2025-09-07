package com.beyond.jellyorder.domain.order.entity;

import com.beyond.jellyorder.common.BaseIdEntity;
import com.beyond.jellyorder.domain.openclose.entity.StoreOpenClose;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import jakarta.persistence.*;
import lombok.*;

import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "total_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TotalOrder extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_table_id", nullable = false)
    private StoreTable storeTable;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "total_price", nullable = false)
    @Builder.Default
    private Integer totalPrice = 0;

    @Column(name = "count", nullable = false)
    @Builder.Default
    private Integer count = 0;

    @Column(name = "paymented_at")
    private LocalDateTime paymentedAt;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @OneToMany(mappedBy = "totalOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UnitOrder> unitOrderList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_open_close_id")
    private StoreOpenClose storeOpenClose;

    public void setStoreOpenClose(StoreOpenClose soc) {
        this.storeOpenClose = soc;
    }
    public StoreOpenClose getStoreOpenClose() { return storeOpenClose; }

    // 최초 주문 생성 시 자동 주입
    @PrePersist
    protected void onCreate() {
        this.orderedAt = LocalDateTime.now();
    }

    // 단위주문 총 금액
    public void addUnitTotal(int price, int count) {
        this.totalPrice += price;
        this.count += count;
    }

    // 총 주문 개수 변경 메서드
    public void updateCount(Integer updateCount) {
        this.count += updateCount;
    }

    // 결제 시간 업데이트 메서드
    public void updatePaymentedAt(LocalDateTime paidAt) {
        this.paymentedAt = paidAt;
    }

    // 총 가격 감소 메서드
    public void decreaseTotalPrice(Integer decreasePrice) {
        this.totalPrice -= decreasePrice;
    }

    // 주문 완료 시간 변경
    public void updateEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    // 정산 금액 변환 메서드
    public Long changeSettlementAmount() {
        return (long) (this.totalPrice * 0.9);
    }

    /**
     * 주문 메뉴 목록(List<OrderMenu>)만으로 총액을 다시 계산해서 갱신한다.
     * (menu.price + Σ subOption.price) * orderMenu.quantity
     *
     * @param orderMenus 리포지토리에서 fetch join으로 가져온 목록
     * @return 갱신된 총액
     */
    public Integer refreshTotaCount(List<OrderMenu> orderMenus) {
        long sum = 0L; // 내부 계산은 여유 있게 long으로

        if (orderMenus != null) {
            for (OrderMenu om : orderMenus) {
                if (om == null) continue;

                int qty        = nz(om.getQuantity());
                int menuPrice  = (om.getMenu() != null) ? nz(om.getMenu().getPrice()) : 0;

                int optionSum = 0;
                if (om.getOrderMenuOptionList() != null) {
                    for (OrderMenuOption omo : om.getOrderMenuOptionList()) {
                        if (omo != null && omo.getSubOption() != null) {
                            optionSum += nz(omo.getSubOption().getPrice());
                        }
                    }
                }

                sum += (long) (menuPrice + optionSum) * qty;
            }
        }

        // Integer 필드에 반영 (오버플로 방지)
        int newTotal = (sum > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) sum;
        this.totalPrice = newTotal;
        return newTotal;
    }

    private static int nz(Integer v) { return (v != null) ? v : 0; }

}
