package com.beyond.jellyorder.domain.sales.domain;

import com.beyond.jellyorder.common.BaseIdAndTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sales extends BaseIdAndTimeEntity {
    private UUID orderId;
    @Enumerated(EnumType.STRING)
    private OrderType orderType;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING)
    private SalesStatus status;
    // 금액 스냅샷
    private Long totalAmount;
    private Long settlementAmount;
    // 결제 완료 시각(정산 기준 시각)
    private LocalDateTime paidAt;

    public void complete(LocalDateTime paidAt, long settlementAmount) {
        this.status = SalesStatus.COMPLETED;
        this.paidAt = paidAt;
        this.settlementAmount = settlementAmount;
    }

    public void cancel(LocalDateTime cancelledAt) {
        this.status = SalesStatus.CANCELLED;
        this.paidAt = cancelledAt;
        this.settlementAmount = 0L;
    }
}
