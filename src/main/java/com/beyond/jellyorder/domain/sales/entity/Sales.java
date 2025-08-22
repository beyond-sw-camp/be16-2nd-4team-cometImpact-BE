package com.beyond.jellyorder.domain.sales.entity;

import com.beyond.jellyorder.common.BaseIdAndTimeEntity;
import com.beyond.jellyorder.domain.order.entity.TotalOrder;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Sales extends BaseIdAndTimeEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "total_order_id", nullable = false, unique = true)
    private TotalOrder totalOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "total_amount")
    private Long totalAmount;

    @Column(name = "settlement_amount")
    private Long settlementAmount;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // 추가사항 (카카오페이 tid)
    @Column(name = "tid")
    private String tid;

    // 카운터 결제 선택 시 Status = PENDING
    @PrePersist // Entity가 DB에 insert 되기 전에 호출됨
    void prePersist() {
        if (status == null) {
            status = Status.PENDING;
        }
        validateConsistency(false);
    }

    @PreUpdate  // Entity가 DB에 update 되기 전에 호출됨
    void preUpdate() {
        validateConsistency(true);
    }

    private void validateConsistency(boolean isUpdate) {
        // TABLE은 QR만
        if (orderType == OrderType.TABLE && paymentMethod != null && paymentMethod != PaymentMethod.QR) {
            throw new IllegalStateException("TABLE 결제는 QR만 허용됩니다.");
        }
        // COUNTER 완료 시점에만 CARD/CASH 확정 (PENDING일 땐 null 허용)
        if (orderType == OrderType.COUNTER && paymentMethod == PaymentMethod.QR) {
            throw new IllegalStateException("COUNTER 결제는 QR을 사용할 수 없습니다.");
        }
        // COMPLETED면 필수값 검증
        if (status == Status.COMPLETED) {
            if (paidAt == null) {
                throw new IllegalStateException("COMPLETED 상태에는 paid_at이 필요합니다.");
            }
        }
    }
}
