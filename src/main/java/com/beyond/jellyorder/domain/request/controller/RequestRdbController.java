package com.beyond.jellyorder.domain.request.controller;

import com.beyond.jellyorder.common.apiResponse.CommonDTO;
import com.beyond.jellyorder.domain.request.dto.RequestRdbDto;
import com.beyond.jellyorder.domain.request.dto.RequestUpdateDto;
import com.beyond.jellyorder.domain.request.service.RdbRequestService;
import com.beyond.jellyorder.domain.sseRequest.dto.RequestResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/request")
@PreAuthorize("hasRole('STORE')")
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

    // 요청사항 목록 조회
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

    // 요청사항 수정
    @PutMapping("/update/{requestId}")
    public ResponseEntity<?> update(@RequestBody RequestUpdateDto dto, @PathVariable UUID requestId) {
        UUID id = requestService.update(dto, requestId);

        return new ResponseEntity<>(
                CommonDTO.builder()
                        .result(id)
                        .status_code(HttpStatus.OK.value())
                        .status_message("요청사항 변경 완료")
                        .build(),
                HttpStatus.OK
        );
    }

    // 요청사항 삭제
    @DeleteMapping("/delete/{requestId}")
    public ResponseEntity<?> delete(@PathVariable UUID requestId) {
        requestService.deleteRequest(requestId);

        return new ResponseEntity<>(
                CommonDTO.builder()
                        .result("OK")
                        .status_code(HttpStatus.OK.value())
                        .status_message("요청사항 삭제 완료")
                        .build(),
                HttpStatus.OK
        );
    }
}
