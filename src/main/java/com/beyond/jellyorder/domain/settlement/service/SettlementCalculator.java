package com.beyond.jellyorder.domain.settlement.service;

import com.beyond.jellyorder.domain.order.entity.OrderMenu;
import com.beyond.jellyorder.domain.order.entity.OrderMenuOption;
import com.beyond.jellyorder.domain.order.entity.TotalOrder;
import com.beyond.jellyorder.domain.order.entity.UnitOrder;
import com.beyond.jellyorder.domain.sales.entity.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class SettlementCalculator {
    // 정책값은 properties로 빼는 것이 이상적이지만, 우선 상수로 둡니다.
    private final BigDecimal vatRate = BigDecimal.valueOf(0.10);  // 부가세 10%
    private final BigDecimal cardFee = BigDecimal.valueOf(0.027); // 카드 수수료 2.7%
    private final BigDecimal qrFee   = BigDecimal.valueOf(0.015); // QR 수수료 1.5%
    private final BigDecimal cashFee = BigDecimal.valueOf(0.0);   // 현금 0%

    /** 권장: 스냅샷(totalPrice) 기반 총매출 */
    public long calcTotalAmount(TotalOrder totalOrder) {
        Integer tp = totalOrder.getTotalPrice(); // null 방지: 엔티티에서 0으로 default
        return tp == null ? 0L : tp.longValue();
    }

    /** 선택: 라인아이템을 다시 합산(스냅샷 불신/검증용) */
    public long calcTotalAmountFromLines(TotalOrder totalOrder) {
        BigDecimal sum = BigDecimal.ZERO;

        for (UnitOrder unit : totalOrder.getUnitOrderList()) {
            for (OrderMenu om : unit.getOrderMenus()) {
                BigDecimal optSum = BigDecimal.ZERO;
                for (OrderMenuOption opt : om.getOrderMenuOptionList()) {
                    optSum = optSum.add(BigDecimal.valueOf(nvl(opt.getOptionPrice())));
                }

                BigDecimal line = BigDecimal
                        .valueOf(nvl(om.getMenuPrice()))
                        .add(optSum)
                        .multiply(BigDecimal.valueOf(nvl(om.getQuantity())));
                sum = sum.add(line);
            }
        }
        return sum.setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    /** 정산예상금 = 총매출 - VAT - 결제수수료  (총액에 대해 부가세/수수료 공제) */
    public long calcSettlementAmount(long totalAmount, PaymentMethod method) {
        BigDecimal total = BigDecimal.valueOf(totalAmount);

        // VAT 포함가정: 공급가 = total / (1 + VAT), VAT = total - 공급가
        BigDecimal supply = total.divide(BigDecimal.ONE.add(vatRate), 0, RoundingMode.HALF_UP);
        BigDecimal vat = total.subtract(supply);

        BigDecimal feeRate = switch (method) {
            case CARD -> cardFee;
            case QR   -> qrFee;
            case CASH -> cashFee;
        };
        BigDecimal fee = total.multiply(feeRate).setScale(0, RoundingMode.HALF_UP);

        BigDecimal net = total.subtract(vat).subtract(fee);
        return net.max(BigDecimal.ZERO).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    /** Integer/Long 모두 수용 */
    private long nvl(Number v) { return v == null ? 0L : v.longValue(); }
}

