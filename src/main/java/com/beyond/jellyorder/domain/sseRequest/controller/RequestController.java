package com.beyond.jellyorder.domain.sseRequest.controller;

import com.beyond.jellyorder.common.apiResponse.CommonDTO;
import com.beyond.jellyorder.domain.sseRequest.dto.RequestCreateDto;
import com.beyond.jellyorder.domain.sseRequest.service.RedisRequestService;
import com.beyond.jellyorder.domain.sseRequest.sse.SseEmitters;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sse/request")
@PreAuthorize("hasAnyRole('STORE','STORE_TABLE')")
// 고객 요청 전송용
public class RequestController {
    private final RedisRequestService requestService;
    private final SseEmitters emitters;

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody RequestCreateDto dto) {
        requestService.send(dto);
        emitters.notifyStore(dto.getStoreId().toString(), dto);

        return new ResponseEntity<>(
                CommonDTO.builder()
                        .result(dto)
                        .status_code(HttpStatus.CREATED.value())
                        .status_message("요청완료")
                        .build(),
                HttpStatus.CREATED
        );
    }
}
