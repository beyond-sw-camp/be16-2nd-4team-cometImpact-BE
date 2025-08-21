package com.beyond.jellyorder.domain.websocket;

import com.beyond.jellyorder.domain.order.dto.UnitOrderCreateReqDto;
import com.beyond.jellyorder.domain.order.dto.UnitOrderResDto;
import com.beyond.jellyorder.domain.order.dto.orderStatus.OrderStatusResDTO;
import com.beyond.jellyorder.domain.order.service.UnitOrderService;
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

    private final SimpMessageSendingOperations messageTemplate;
    private final UnitOrderService unitOrderService;

    public WebSocketOrderController(SimpMessageSendingOperations messageTemplate, UnitOrderService unitOrderService) {
        this.messageTemplate = messageTemplate;
        this.unitOrderService = unitOrderService;
    }

    // 주문전송
    @MessageMapping("/{storeId}/orders")
    public void sendOrder(
            @DestinationVariable UUID storeId,
            @Valid UnitOrderCreateReqDto reqDTO,
            Principal principal
    ) {
        // 주문 생성 및 저장
        OrderStatusResDTO resDTO = unitOrderService.createUnit(reqDTO);

        // 점주 대시보드에 브로드캐스트 전달
        messageTemplate.convertAndSend("/topic/" + storeId, resDTO);

        // 주문응답값 return
        OrderAckDto orderAckDto = OrderAckDto.builder()
                .type(OrderAckDto.Type.ACK)
                .storeId(storeId)
                .storeTableId(reqDTO.getStoreTableId())
                .unitOrderId(resDTO.getUnitOrderId())
                .message("주문이 접수되었습니다.")
                .build();

        // 주문한 테이블 자기 자신에게 응답값 보내기
        // queue의 인자값은 (보낼사람, 경로, 보내는 dto)
        messageTemplate.convertAndSendToUser(principal.getName(), "/queue/ack", orderAckDto);
    }

}
