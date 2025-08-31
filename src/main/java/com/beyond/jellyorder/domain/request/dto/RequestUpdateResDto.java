package com.beyond.jellyorder.domain.request.dto;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestUpdateResDto {
    private UUID id;
    private String name;
}
