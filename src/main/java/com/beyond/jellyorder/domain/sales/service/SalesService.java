package com.beyond.jellyorder.domain.sales.service;

import com.beyond.jellyorder.domain.openclose.entity.StoreOpenClose;
import com.beyond.jellyorder.domain.openclose.repository.StoreOpenCloseRepository;
import com.beyond.jellyorder.domain.openclose.service.StoreOpenCloseService;
import com.beyond.jellyorder.domain.order.entity.OrderStatus;
import com.beyond.jellyorder.domain.order.entity.TotalOrder;
import com.beyond.jellyorder.domain.order.repository.TotalOrderRepository;
import com.beyond.jellyorder.domain.sales.dto.CounterPaymentReqDTO;
import com.beyond.jellyorder.domain.sales.entity.OrderType;
import com.beyond.jellyorder.domain.sales.entity.PaymentMethod;
import com.beyond.jellyorder.domain.sales.entity.Sales;
import com.beyond.jellyorder.domain.sales.entity.SalesStatus;
import com.beyond.jellyorder.domain.sales.repository.SalesRepository;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.entity.TableStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class SalesService {

    private final SalesRepository salesRepository;
    private final EntityManager em;
    private final TotalOrderRepository totalOrderRepository;
    private final StoreOpenCloseRepository storeOpenCloseRepository;

    // 주문을 기준으로 결제 PENDING 생성(있다면 갱신)
    public Sales createPending(UUID orderId, OrderType orderType, PaymentMethod paymentMethod, Long totalAmount) {
        Sales sales = salesRepository.findByTotalOrderId(orderId).orElseGet(() ->
                Sales.builder()
                        .totalOrder(em.getReference(TotalOrder.class, orderId))
                        .status(SalesStatus.PENDING)
                        .build()
        );

        if (orderType == OrderType.TABLE && paymentMethod != PaymentMethod.QR) {
            throw new IllegalStateException("TABLE 결제는 QR만 허용됩니다.");
        }
        if (orderType == OrderType.COUNTER && paymentMethod == PaymentMethod.QR) {
            throw new IllegalStateException("COUNTER 결제는 QR을 사용할 수 없습니다.");
        }

        sales.setOrderType(orderType);
        sales.setPaymentMethod(paymentMethod);
        sales.setStatus(SalesStatus.PENDING);
        sales.setTotalAmount(null);
        sales.setPaidAt(null);
        sales.setTid(null);

        TotalOrder totalOrder = sales.getTotalOrder();
        UUID storeId = totalOrder.getStoreTable().getStore().getId();
        StoreOpenClose openClose = storeOpenCloseRepository.findOpen(storeId)
                .orElseThrow(() -> new IllegalStateException("영업 오픈 상태가 아닙니다."));
        sales.setStoreOpenClose(openClose);

        return salesRepository.save(sales);
    }

    /** 카카오페이 결제 식별자(tid)를 Sales에 연결 */
    public void attachTid(UUID orderId, String tid) {
        Sales sales = getByOrderIdOrThrow(orderId);
        sales.setTid(tid);
        // save 생략: JPA dirty checking
    }

    /** 공통 완료 처리 (QR/COUNTER 공용) */
    public Sales complete(UUID orderId, PaymentMethod method, Long totalAmount, LocalDateTime paidAt) {
        Sales sales = getByOrderIdOrThrow(orderId);

        // ★ 혹시 null이면 다시 보강(과거 데이터 보호)
        if (sales.getStoreOpenClose() == null) {
            UUID storeId = sales.getTotalOrder().getStoreTable().getStore().getId();
            StoreOpenClose openClose = storeOpenCloseRepository.findOpen(storeId)
                    .orElseThrow(() -> new IllegalStateException("영업 오픈 상태가 아닙니다."));
            sales.setStoreOpenClose(openClose);
        }

        if (sales.getStatus() != SalesStatus.PENDING) {
            return sales; // 멱등 처리
        }

        // 결제수단 확정(카운터의 경우 이 시점에 CARD/CASH 확정)
        if (method != null) {
            if (sales.getOrderType() == OrderType.TABLE && method != PaymentMethod.QR) {
                throw new IllegalStateException("TABLE 결제는 QR만 허용됩니다.");
            }
            if (sales.getOrderType() == OrderType.COUNTER &&
                    !(method == PaymentMethod.CARD || method == PaymentMethod.CASH)) {
                throw new IllegalStateException("COUNTER 결제는 CARD/CASH만 허용됩니다.");
            }
            sales.setPaymentMethod(method);
        }
        // ===== 금액/정산 확정 (집계에 필수) =====
        TotalOrder toForAmount = sales.getTotalOrder();
        long gross = (totalAmount != null)
                ? totalAmount
                : (toForAmount.getTotalPrice() != null ? toForAmount.getTotalPrice().longValue() : 0L);
        sales.setTotalAmount(gross);

        // 정산금액: TotalOrder에 기존 로직이 있으면 그대로 사용, 없으면 10% 공제 fallback
        Long settlement = (toForAmount.changeSettlementAmount() != null)
                ? toForAmount.changeSettlementAmount()
                : Math.round(gross * 0.9);
        sales.setSettlementAmount(settlement);


        sales.setPaidAt(paidAt != null ? paidAt : LocalDateTime.now());
        sales.setStatus(SalesStatus.COMPLETED);

        // 결제 완료 후 테이블 status 리셋 && totalOrder의 paymentedAt 시간 업데이트
        TotalOrder totalOrder = sales.getTotalOrder();
        if (totalOrder != null && totalOrder.getStoreTable() != null) {
            StoreTable table = totalOrder.getStoreTable();
            if (method == PaymentMethod.QR) {
                updateTableStatusQR(table, totalOrder);
            } else if ((method == PaymentMethod.CARD || method == PaymentMethod.CASH)) {
                updateTableStatusCounter(table, totalOrder);
            }
            totalOrder.updatePaymentedAt(sales.getPaidAt());
            totalOrder.updateEndedAt(sales.getPaidAt());
        }

        return sales;
    }

    private void updateTableStatusQR(StoreTable storeTable, TotalOrder totalOrder) {
        if (storeTable.getStatus() == TableStatus.EATING && totalOrder.getOrderedAt() != null) {
            storeTable.changeStatus(TableStatus.PAY_DONE);
        }
    }

    private void updateTableStatusCounter(StoreTable storeTable, TotalOrder totalOrder) {
        if (storeTable.getStatus() == TableStatus.EATING && totalOrder.getOrderedAt() != null) {
            storeTable.changeStatus(TableStatus.STANDBY);
        }
    }

    /** 취소 처리 */
    public Sales cancel(UUID orderId) {
        Sales sales = getByOrderIdOrThrow(orderId);
        if (sales.getStatus() == SalesStatus.COMPLETED) {
            throw new IllegalStateException("이미 COMPLETED 상태입니다.");
        }
        sales.setStatus(SalesStatus.CANCELLED);
        return sales;
    }

    /** orderId 조회 */
    @Transactional(readOnly = true)
    public Sales getByOrderIdOrThrow(UUID orderId) {
        return salesRepository.findByTotalOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Sales not found for orderId=" + orderId));
    }

    public void processPayment(CounterPaymentReqDTO reqDTO) {
        TotalOrder totalOrder = totalOrderRepository.findById(reqDTO.getTotalOrderId())
                .orElseThrow(() -> new EntityNotFoundException("해당 전체주문이 없습니다."));
        if (totalOrder.getPaymentedAt() != null) {
            throw new IllegalArgumentException("이미 결제가 완료된 주문건입니다.");
        }

        // sales 엔티티 생성
        Sales sales = Sales.builder()
                .totalOrder(totalOrder)
                .orderType(OrderType.COUNTER)
                .paymentMethod(reqDTO.getMethod())
                .status(SalesStatus.COMPLETED)
                .totalAmount(Long.valueOf(totalOrder.getTotalPrice()))
                .settlementAmount(totalOrder.changeSettlementAmount())
                .paidAt(LocalDateTime.now())
                .build();
        salesRepository.save(sales);

        // table 상태값 변경
        totalOrder.getStoreTable().changeStatus(TableStatus.STANDBY);

        // totalOrder 상태값 변경
        totalOrder.updateEndedAt(LocalDateTime.now());
        totalOrder.updatePaymentedAt(LocalDateTime.now());

        // 현재 주문 상태 확인
        totalOrder.getUnitOrderList().forEach(unitOrder -> {
            if (unitOrder.getStatus() == OrderStatus.ACCEPT) {
                throw new IllegalStateException("현재 활성상태인 주문이 있습니다." + unitOrder.getOrderNumber());
            }
        });
    }
}
