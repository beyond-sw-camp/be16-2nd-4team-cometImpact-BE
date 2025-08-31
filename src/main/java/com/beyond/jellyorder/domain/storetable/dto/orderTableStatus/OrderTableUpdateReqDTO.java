package com.beyond.jellyorder.domain.storetable.dto.orderTableStatus;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTableUpdateReqDTO {
    private UUID unitOrderId;
    @NotEmpty(message = "수정되는 주문이 없습니다.")
    private List<MenuDetail> menuDetailList;
}
