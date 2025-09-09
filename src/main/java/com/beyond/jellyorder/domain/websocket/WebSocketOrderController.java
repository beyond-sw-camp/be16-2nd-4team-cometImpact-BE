package com.beyond.jellyorder.domain.websocket;

import com.beyond.jellyorder.domain.order.dto.UnitOrderCreateReqDto;
import com.beyond.jellyorder.domain.order.dto.orderStatus.OrderStatusResDTO;
import com.beyond.jellyorder.domain.order.service.OrderPubSubService;
import com.beyond.jellyorder.domain.order.service.UnitOrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@Slf4j
public class WebSocketOrderController {

    private final UnitOrderService unitOrderService;
    private final OrderPubSubService orderPubSubService;
    private final SimpMessageSendingOperations messageTemplate;
    private final ObjectMapper objectMapper;

    public WebSocketOrderController(
            UnitOrderService unitOrderService,
            OrderPubSubService orderPubSubService,
            SimpMessageSendingOperations messageTemplate,
            ObjectMapper objectMapper
    ) {
        this.unitOrderService = unitOrderService;
        this.orderPubSubService = orderPubSubService;
        this.messageTemplate = messageTemplate;
        this.objectMapper = objectMapper;
    }

    // 주문전송
    @MessageMapping("/{storeId}/orders")
    public void sendOrder(
            @DestinationVariable UUID storeId,
            @Valid UnitOrderCreateReqDto reqDTO
    ) throws JsonProcessingException {
        final String tableTopic = "/topic/" + reqDTO.getStoreTableId();

        try {
            // 1. 주문 생성 및 DB에 저장.
            OrderStatusResDTO orderStatusResDTO = unitOrderService.createUnit(reqDTO, storeId);
            log.info("주문 생성 및 DB 저장 성공{}", orderStatusResDTO);

            OrderAckDto result = OrderAckDto.builder()
                    .reqId(String.valueOf(reqDTO.getStoreTableId()))
                    .ok(true)
                    .code("ok")
                    .message("주문이 접수되었습니다.")
                    .data(orderStatusResDTO)
                    .build();

            // 2. 주문 요청한 테이블에게 성공 응답 보내기.
            messageTemplate.convertAndSend(tableTopic, result);
            log.info("테이블 OrderAckDto 발행 성공");

            // 3. redis에 보낼 응답객체 직렬화 변환.
            OrderStompResDTO stompResDTO = OrderStompResDTO.builder()
                    .storeId(storeId)
                    .orderStatusResDTO(orderStatusResDTO)
                    .build();
            String message = objectMapper.writeValueAsString(stompResDTO);

            // 4. redis에 발행(publish).
            orderPubSubService.publish("order", message);
            log.info("redis pub/sub에 메시지 발행 message: {}, topic: {}", stompResDTO, "order");
        } catch (Exception e) {
            OrderAckDto result = OrderAckDto.builder()
                    .reqId(String.valueOf(reqDTO.getStoreTableId()))
                    .ok(false)
                    .code("fail")
                    .message(e.getMessage())
                    .data(null)
                    .build();
            messageTemplate.convertAndSend(tableTopic, result);
            log.error("주문 실패: tableTopic={}, code={}, cause={}",tableTopic, "fail", e.getMessage());
        }


    }

}
