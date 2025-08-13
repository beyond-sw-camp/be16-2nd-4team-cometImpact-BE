package com.beyond.jellyorder.domain.order.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.order.dto.UnitOrderCreateReqDto;
import com.beyond.jellyorder.domain.order.dto.UnitOrderResDto;
import com.beyond.jellyorder.domain.order.service.OrderService;
import com.beyond.jellyorder.domain.storetable.service.StoreTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@PreAuthorize("hasRole('STORE')")
public class OrderController {

    private final OrderService unitOrderService;
    private final StoreTableService storeTableService;

    /** 단위주문 생성 */
    @PostMapping("/unit/create")
    public ResponseEntity<?> createUnit(@RequestBody @Valid UnitOrderCreateReqDto reqDTO) {
        UnitOrderResDto resDTO = unitOrderService.createUnit(reqDTO);

        return ApiResponse.created(resDTO, "단위주문이 생성되었습니다.");
    }
}
