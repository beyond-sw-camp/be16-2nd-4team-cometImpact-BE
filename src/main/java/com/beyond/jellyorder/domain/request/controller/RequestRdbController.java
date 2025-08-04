package com.beyond.jellyorder.domain.request.controller;

import com.beyond.jellyorder.common.apiResponse.CommonDTO;
import com.beyond.jellyorder.domain.request.dto.RequestRdbDto;
import com.beyond.jellyorder.domain.request.service.RdbRequestService;
import com.beyond.jellyorder.domain.sseRequest.dto.RequestResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/request")
public class RequestRdbController {
    private final RdbRequestService requestService;

    // 요청사항 생성
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody RequestRdbDto dto) {
        UUID id = requestService.create(dto);

        return new ResponseEntity<>(
                CommonDTO.builder()
                        .result(dto)
                        .status_code(HttpStatus.CREATED.value())
                        .status_message("요청사항 등록 완료")
                        .build(),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> getRequestList() {
        List<RequestResponseDto> requests = requestService.getMyRequests();
        return ResponseEntity.ok(
                CommonDTO.builder()
                        .result(requests)
                        .status_code(HttpStatus.OK.value())
                        .status_message("요청사항 목록 조회 완료")
                        .build()
        );
    }
}
