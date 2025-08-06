package com.beyond.jellyorder.domain.store.service;

import com.beyond.jellyorder.domain.store.dto.BizNoApiResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessVerificationService {

    private final RestTemplate restTemplate;

    @Value("${external.api.bizno-url}")
    private String bizNoUrl;

    @Value("${external.api.bizno-key}")
    private String serviceKey;

    public void verify(String businessNumber) {
//        Http 요청 헤더 세팅
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

//        Http 요청 바디 구성
        Map<String, Object> body = new HashMap<>();
        body.put("b_no", Collections.singleton(businessNumber));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

//        URL 구성
        String urlWithKey = bizNoUrl + "?serviceKey=" + serviceKey;

        try {
            ResponseEntity<BizNoApiResponseDTO> response =
                    restTemplate.exchange(urlWithKey, HttpMethod.POST, request, BizNoApiResponseDTO.class);

//        응답 검증
            BizNoApiResponseDTO responseBody = response.getBody();
            if (responseBody == null || responseBody.getData().isEmpty()) {
                throw new IllegalArgumentException("응답 데이터 없음: 사업자등록번호 검증 실패");
            }

            String statusCode = responseBody.getData().get(0).getB_stt_cd();
            if (!"01".equals(statusCode)) {
                throw new IllegalArgumentException("유효하지 않은 사업자등록번호입니다.");
            }
        } catch (RestClientException e) { // RestTemplate에서 외부 API 요청 중 문제가 발생했을 때 던지는 최상위 예외 클래스
            log.error("사업자등록번호 API 요청 실패", e);
            throw new IllegalArgumentException("사업자등록번호 검증 중 오류 발생");
        }
    }

}
