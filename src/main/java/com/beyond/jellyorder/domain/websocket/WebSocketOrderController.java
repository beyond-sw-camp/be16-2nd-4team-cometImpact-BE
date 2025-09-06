package com.beyond.jellyorder.domain.websocket;

import com.beyond.jellyorder.domain.order.dto.UnitOrderCreateReqDto;
import com.beyond.jellyorder.domain.order.dto.UnitOrderResDto;
import com.beyond.jellyorder.domain.order.dto.orderStatus.OrderStatusResDTO;
import com.beyond.jellyorder.domain.order.service.OrderPubSubService;
import com.beyond.jellyorder.domain.order.service.UnitOrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@Slf4j
public class WebSocketOrderController {

    private final UnitOrderService unitOrderService;
    private final OrderPubSubService orderPubSubService;
    private final ObjectMapper objectMapper;

    public WebSocketOrderController(
            UnitOrderService unitOrderService,
            OrderPubSubService orderPubSubService,
            ObjectMapper objectMapper
    ) {

        this.unitOrderService = unitOrderService;
        this.orderPubSubService = orderPubSubService;
        this.objectMapper = objectMapper;
    }

    // 주문전송
    @MessageMapping("/{storeId}/orders")
    public void sendOrder(
            @DestinationVariable UUID storeId,
            @Valid UnitOrderCreateReqDto reqDTO
//            ,Principal principal
    ) throws JsonProcessingException {
        // 1. 주문 생성 및 DB에 저장.
        OrderStatusResDTO orderStatusResDTO = unitOrderService.createUnit(reqDTO, storeId);
        log.info("주문 생성 및 DB 저장 성공{}", orderStatusResDTO);

        // 2. redis에 보낼 응답객체 직렬화 변환.
        OrderStompResDTO stompResDTO = OrderStompResDTO.builder()
                .storeId(storeId)
                .orderStatusResDTO(orderStatusResDTO)
                .build();
        String message = objectMapper.writeValueAsString(stompResDTO);

        // 3. redis에 발행(publish).
        orderPubSubService.publish("order", message);
        log.info("redis pub/sub에 메시지 발행 message: {}, topic: {}", stompResDTO, "order");
    }

}
