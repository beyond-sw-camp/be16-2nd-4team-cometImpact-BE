package com.beyond.jellyorder.domain.websocket;

import com.beyond.jellyorder.domain.order.dto.UnitOrderCreateReqDto;
import com.beyond.jellyorder.domain.order.dto.UnitOrderResDto;
import com.beyond.jellyorder.domain.order.dto.orderStatus.OrderStatusResDTO;
import com.beyond.jellyorder.domain.order.service.OrderPubSubService;
import com.beyond.jellyorder.domain.order.service.UnitOrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class WebSocketOrderController {

    private final UnitOrderService unitOrderService;
    private final OrderPubSubService orderPubSubService;

    public WebSocketOrderController(UnitOrderService unitOrderService, OrderPubSubService orderPubSubService) {
        this.unitOrderService = unitOrderService;
        this.orderPubSubService = orderPubSubService;
    }

    // 주문전송
    @MessageMapping("/{storeId}/orders")
    public void sendOrder(
            @DestinationVariable UUID storeId,
            @Valid UnitOrderCreateReqDto reqDTO
//            ,Principal principal
    ) throws JsonProcessingException {
        // 주문 생성 및 DB에 저장
        OrderStatusResDTO orderStatusResDTO = unitOrderService.createUnit(reqDTO, storeId);

        // redis에 보낼 객체 변환 및 redis에 발행.
        OrderStompResDTO stompResDTO = OrderStompResDTO.builder()
                .storeId(storeId)
                .orderStatusResDTO(orderStatusResDTO)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(stompResDTO);
        orderPubSubService.publish("order", message);
    }

}
