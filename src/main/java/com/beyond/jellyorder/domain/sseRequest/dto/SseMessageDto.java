package com.beyond.jellyorder.domain.sseRequest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SseMessageDto {
    private String sender;
    private String receiver;
    private Long orderingId;
}
