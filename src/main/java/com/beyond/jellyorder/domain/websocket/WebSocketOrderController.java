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

    // 방법2. MessageMapping 어노테이션만 활용.
    @MessageMapping("/{storeTableId}")
    public void sendMessage(
            @DestinationVariable UUID storeTableId,
            @Valid UnitOrderCreateReqDto reqDTO
    ) {
        OrderStatusResDTO resDTO = unitOrderService.createUnit(reqDTO, storeTableId);
        messageTemplate.convertAndSend("/topic/" + storeTableId, resDTO);
    }

}
