package com.beyond.jellyorder.domain.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RequestUpdateReqDto {
    @NotBlank(message = "요청사항명은 필수입니다.")
    @Size(max = 10, message = "요청사항명은 최대 10자까지 허용됩니다.")
    private String name;
}
