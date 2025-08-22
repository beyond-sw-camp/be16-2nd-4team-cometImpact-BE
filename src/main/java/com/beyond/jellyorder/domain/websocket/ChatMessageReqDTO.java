package com.beyond.jellyorder.domain.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageReqDTO {
    private String message;
    private String tableName;
    private String menuName;
}
