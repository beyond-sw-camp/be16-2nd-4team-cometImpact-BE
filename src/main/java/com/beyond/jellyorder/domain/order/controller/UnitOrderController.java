package com.beyond.jellyorder.domain.order.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.order.dto.UnitOrderCreateReqDto;
import com.beyond.jellyorder.domain.order.dto.UnitOrderResDto;
import com.beyond.jellyorder.domain.order.dto.orderStatus.OrderStatusResDTO;
import com.beyond.jellyorder.domain.order.service.UnitOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@PreAuthorize("hasRole('STORE')")
public class UnitOrderController {

    private final UnitOrderService orderService;

    /** 단위주문 생성 */
    @PostMapping("/unit/create/{storeTableId}")
    public ResponseEntity<?> createUnit(
            @RequestBody @Valid UnitOrderCreateReqDto reqDTO,
            @PathVariable UUID storeTableId
            ) {
        OrderStatusResDTO resDTO = orderService.createUnit(reqDTO, storeTableId);

        return ApiResponse.created(resDTO, "단위주문이 생성되었습니다.");
    }

}
