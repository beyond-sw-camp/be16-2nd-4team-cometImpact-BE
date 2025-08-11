package com.beyond.jellyorder.domain.storetable.dto.orderTableStatus;

import com.beyond.jellyorder.domain.order.entity.TotalOrder;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.entity.TableStatus;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTableResDTO {
    // 테이블 정보
    private UUID tableId;
    private String tableName;
    private Integer seatCount;
    private TableStatus status;

    // 주문 정보
    private UUID totalOrderId;
    private LocalTime orderedAt; // 11:00 이런식으로 변환 해야 됨.
    private Integer totalPrice;

    // 주문 메뉴들
    private List<OrderMenuDetail> orderMenuDetailList;

    public static OrderTableResDTO from(
            StoreTable storeTable,
            @Nullable TotalOrder totalOrder,
            List<OrderMenuDetail> orderMenuDetailList
    ) {
        // 공통 필드 세팅
        OrderTableResDTOBuilder builder = OrderTableResDTO.builder()
                .tableId(storeTable.getId())
                .tableName(storeTable.getName())
                .seatCount(storeTable.getSeatCount())
                .status(storeTable.getStatus())
                .orderMenuDetailList(orderMenuDetailList);

        // 주문이 있을 경우
        if (totalOrder != null) {
            builder.totalOrderId(totalOrder.getId())
                    .orderedAt(LocalTime.from(totalOrder.getOrderedAt()))
                    .totalPrice(totalOrder.getTotalPrice());
        }

        return builder.build();
    }
}
