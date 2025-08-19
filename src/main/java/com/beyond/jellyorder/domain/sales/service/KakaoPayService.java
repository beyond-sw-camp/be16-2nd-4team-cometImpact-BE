package com.beyond.jellyorder.domain.sales.payment.service;

import com.beyond.jellyorder.common.auth.KakaoPayProperties;
import com.beyond.jellyorder.domain.sales.payment.dto.KakaoApproveResDto;
import com.beyond.jellyorder.domain.sales.payment.dto.KakaoReadyReqDto;
import com.beyond.jellyorder.domain.sales.payment.dto.KakaoReadyResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class KakaoPayService {

    private final KakaoPayProperties payProperties;
    private RestTemplate restTemplate = new RestTemplate();
    private KakaoReadyResDto readyResDto;

    private String tid;
    private String partnerOrderId;
    private String partnerUserId;


    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = "DEV_SECRET_KEY " + payProperties.getDevSecretKey();
        headers.set("Authorization", auth);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    /** 결제 완료 요청 */
    public KakaoReadyResDto kakaoPayReady(KakaoReadyReqDto reqDto) {

        if (reqDto.getCid() == null) reqDto.setCid(payProperties.getCid());
        if (reqDto.getApproval_url() == null) reqDto.setApproval_url(payProperties.getApprovalUrl());
        if (reqDto.getFail_url() == null) reqDto.setFail_url(payProperties.getFailUrl());
        if (reqDto.getCancel_url() == null) reqDto.setCancel_url(payProperties.getCancelUrl());

        HttpEntity<KakaoReadyReqDto> requestEntity = new HttpEntity<>(reqDto, getHeaders());

        KakaoReadyResDto resDto = restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/ready",
                requestEntity,
                KakaoReadyResDto.class
        );

        this.tid = resDto.getTid();
        this.partnerOrderId = reqDto.getPartner_order_id();
        this.partnerUserId = reqDto.getPartner_user_id();

        log.info("KakaoPay Ready Response: {}", resDto);
        return resDto;
    }

    /** 결제 완료 승인 */
    public KakaoApproveResDto kakaoApprove (String tid, String partnerOrderId, String partnerUserId, String pgToken) {
        Map<String, String> approve = new HashMap<>();
        approve.put("cid", payProperties.getCid());
        approve.put("tid", tid);
        approve.put("partner_order_id", partnerOrderId);
        approve.put("partner_user_id", partnerUserId);
        approve.put("pg_token", pgToken);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(approve, this.getHeaders());

        KakaoApproveResDto approveResDto = restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/approve",
                requestEntity,
                KakaoApproveResDto.class
        );

        log.info("KakaoPay Approve Response: {}", approveResDto);
        return approveResDto;
    }
}