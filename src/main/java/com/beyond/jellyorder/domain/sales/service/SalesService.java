package com.beyond.jellyorder.domain.sales.service;

import com.beyond.jellyorder.domain.order.entity.TotalOrder;
import com.beyond.jellyorder.domain.sales.entity.OrderType;
import com.beyond.jellyorder.domain.sales.entity.PaymentMethod;
import com.beyond.jellyorder.domain.sales.entity.Sales;
import com.beyond.jellyorder.domain.sales.entity.Status;
import com.beyond.jellyorder.domain.sales.repository.SalesRepository;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.entity.TableStatus;
import com.beyond.jellyorder.domain.storetable.repository.StoreTableRepository;
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
    private final StoreTableRepository storeTableRepository;
    private final EntityManager em;

    // 주문을 기준으로 결제 PENDING 생성(있다면 갱신)
    public Sales createPending(UUID orderId, OrderType orderType, PaymentMethod paymentMethod, Long totalAmount) {
        Sales sales = salesRepository.findByTotalOrderId(orderId).orElseGet(() ->
                Sales.builder()
                        .totalOrder(em.getReference(TotalOrder.class, orderId))
                        .status(Status.PENDING)
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
        sales.setStatus(Status.PENDING);
        sales.setTotalAmount(null);
        sales.setPaidAt(null);
        sales.setTid(null);

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

        if (sales.getStatus() != Status.PENDING) {
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

        sales.setPaidAt(paidAt != null ? paidAt : LocalDateTime.now());
        sales.setStatus(Status.COMPLETED);

        // 결제 완료 후 테이블 status 리셋
        TotalOrder totalOrder = sales.getTotalOrder();
        if (totalOrder != null && totalOrder.getStoreTable() != null) {
            StoreTable table = totalOrder.getStoreTable();
            updateTableStatus(table, totalOrder);
        }

        return sales;
    }

    private void updateTableStatus(StoreTable storeTable, TotalOrder totalOrder) {
        if (storeTable.getStatus() == TableStatus.EATING && totalOrder.getOrderedAt() != null) {
            storeTable.changeStatus(TableStatus.PAY_DONE);
        }
    }

    /** 취소 처리 */
    public Sales cancel(UUID orderId) {
        Sales sales = getByOrderIdOrThrow(orderId);
        if (sales.getStatus() == Status.COMPLETED) {
            throw new IllegalStateException("이미 COMPLETED 상태입니다.");
        }
        sales.setStatus(Status.CANCELLED);
        return sales;
    }

    /** orderId 조회 */
    @Transactional(readOnly = true)
    public Sales getByOrderIdOrThrow(UUID orderId) {
        return salesRepository.findByTotalOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Sales not found for orderId=" + orderId));
    }
}
