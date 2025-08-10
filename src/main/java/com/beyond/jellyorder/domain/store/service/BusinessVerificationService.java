package com.beyond.jellyorder.domain.store.service;

import com.beyond.jellyorder.domain.store.dto.BizNoApiReqDTO;
import com.beyond.jellyorder.domain.store.dto.BizNoApiResDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessVerificationService {

    private final RestTemplate restTemplate;

    @Value("${external.api.bizno-url}")
    private String bizNoUrl;   // 예: https://api.odcloud.kr/api/nts-businessman/v1/validate

    @Value("${external.api.bizno-key}")
    private String serviceKey; // 일반 인증키(Decoding)

    @PostConstruct
    public void logApiKeyInfo() {
        log.info("bizNoUrl = {}", bizNoUrl);
        log.info("keyLen = {}", serviceKey == null ? null : serviceKey.strip().length());
        log.info("encodedKey = {}", serviceKey != null && serviceKey.contains("%"));
    }

    /**
     * 사업자 진위확인 + 상태 확인
     * 유효하지 않으면 IllegalArgumentException 던집니다.
     */
    public BizNoApiResDTO.Item verify(String businessNumber, String openDate, String ownerName) {
        // 0) 사업자번호 정규화
        String bNo = businessNumber == null ? null : businessNumber.replaceAll("-", "").trim();

        // 1) Decoding 키를 URL 인코딩
        String key = serviceKey == null ? null : serviceKey.strip();
        String encodedKey = key == null ? null : URLEncoder.encode(key, StandardCharsets.UTF_8);

        // 2) URI (추가 인코딩 금지)
        URI uri = UriComponentsBuilder.fromHttpUrl(bizNoUrl)
                .queryParam("serviceKey", encodedKey)   // %2B, %3D%3D 형태
                .queryParam("returnType", "JSON")
                .build(true)
                .toUri();

        // (진단 로그)
        String uriStr = uri.toASCIIString();
        log.info("odcloud uri(check) = {}...{}",
                uriStr.substring(0, Math.min(80, uriStr.length())),
                uriStr.substring(Math.max(0, uriStr.length() - 10)));

        // 3) 요청 바디
        BizNoApiReqDTO body = BizNoApiReqDTO.ofSingle(bNo, openDate, ownerName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BizNoApiReqDTO> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<BizNoApiResDTO> resp =
                    restTemplate.exchange(uri, HttpMethod.POST, request, BizNoApiResDTO.class);

            BizNoApiResDTO res = resp.getBody();
            if (res == null || res.data() == null || res.data().isEmpty()) {
                throw new IllegalArgumentException("사업자 진위확인 응답이 비어있습니다.");
            }

            BizNoApiResDTO.Item item = res.data().get(0);

            if (!"01".equals(item.valid())) { // 진위 일치 코드
                throw new IllegalArgumentException("사업자 진위 불일치: " + item.validMsg());
            }

            BizNoApiResDTO.Status status = item.status();
            if (status != null && !"01".equals(status.bSttCd())) { // 상태 정상 코드
                throw new IllegalArgumentException("사업자 상태 비정상(코드 " + status.bSttCd() + ", " + status.bStt() + ")");
            }

            return item;

        } catch (HttpClientErrorException e) {
            log.error("사업자등록번호 API 호출 실패: {}", e.getResponseBodyAsString(), e);
            throw new IllegalArgumentException("사업자등록번호 검증 실패: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            log.error("사업자등록번호 통신 오류", e);
            throw new IllegalArgumentException("사업자등록번호 검증 통신 오류");
        }
    }
}