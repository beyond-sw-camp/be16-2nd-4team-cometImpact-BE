package com.beyond.jellyorder.sseRequest.controller;

import com.beyond.jellyorder.common.apiResponse.CommonDTO;
import com.beyond.jellyorder.sseRequest.dto.RequestCreateDto;
import com.beyond.jellyorder.sseRequest.service.RedisRequestService;
import com.beyond.jellyorder.sseRequest.sse.SseEmitters;
import com.beyond.jellyorder.sseRequest.sse.SseEmitters;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sse/request")
// 고객 요청 전송용
public class RequestController {
    private final RedisRequestService requestService;
    private final SseEmitters emitters;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody RequestCreateDto dto) {
        requestService.save(dto);
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
