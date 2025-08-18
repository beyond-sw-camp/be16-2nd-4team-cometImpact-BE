package com.beyond.jellyorder.domain.order.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.order.dto.orderStatus.OrderStatusResDTO;
import com.beyond.jellyorder.domain.order.dto.orderStatus.OrderStatusUpdateReqDTO;
import com.beyond.jellyorder.domain.order.dto.orderStatus.UnitOrderStatusResDTO;
import com.beyond.jellyorder.domain.order.entity.OrderStatus;
import com.beyond.jellyorder.domain.order.service.OrderStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order-status")
@PreAuthorize("hasRole('STORE')")
public class OrderStatusController {

    private final OrderStatusService orderStatusService;

    @GetMapping("/{orderStatus}")
    public ResponseEntity<?> getOrderListInOrderStatus(
            @PathVariable OrderStatus orderStatus,
            @PageableDefault(size = 20, sort = "acceptedAt", direction = Sort.Direction.ASC)Pageable pageable
            ) {
        Page<OrderStatusResDTO> resDTOs = orderStatusService.getOrderListInOrderStatus(orderStatus, pageable);
        return ApiResponse.ok(resDTOs);
    }

    @PatchMapping("/{unitOrderId}/status")
    public ResponseEntity<?> updateUnitOrderStatus(
            @PathVariable UUID unitOrderId,
            @RequestBody OrderStatusUpdateReqDTO reqDTO
            ) {
        UnitOrderStatusResDTO resDTO = orderStatusService.updateUnitOrderStatus(unitOrderId, reqDTO);
        return ApiResponse.ok(resDTO);
    }




















}
