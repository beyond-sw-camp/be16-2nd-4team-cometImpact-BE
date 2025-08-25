package com.beyond.jellyorder.domain.sales.controller;


import com.beyond.jellyorder.common.exception.CommonErrorDTO;
import com.beyond.jellyorder.domain.order.entity.TotalOrder;
import com.beyond.jellyorder.domain.order.repository.TotalOrderRepository;
import com.beyond.jellyorder.domain.sales.dto.*;
import com.beyond.jellyorder.domain.sales.entity.OrderType;
import com.beyond.jellyorder.domain.sales.entity.PaymentMethod;
import com.beyond.jellyorder.domain.sales.entity.Sales;
import com.beyond.jellyorder.domain.sales.service.KakaoPayService;
import com.beyond.jellyorder.domain.sales.service.SalesService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SalesController {
    private final SalesService salesService;
    private final KakaoPayService kakaoPayService;
    private final TotalOrderRepository totalOrderRepository;

    // QR 결제 준비
    @PostMapping("/qr/ready")
    public ResponseEntity<KakaoReadyResDto> qrReady(@RequestBody KakaoReadyReqDto req) {
        UUID orderId = req.getOrderId();

        // orderId 유효성 체크 (없으면 예외 던지기)
        if (!totalOrderRepository.existsById(orderId)) {
            throw new EntityNotFoundException("TotalOrder not found: " + orderId);
        }

        // 1) Sales PENDING 생성/업데이트
        Sales sales = salesService.createPending(
                req.getOrderId(),
                OrderType.TABLE,
                PaymentMethod.QR,
                req.getTotal_amount() == null ? 0L : req.getTotal_amount().longValue()
        );

        // 2) 카카오 ready (orderId 제거하여 전달)
        KakaoReadyReqDto kakaoReq = toKakaoReady(req);
        KakaoReadyResDto res = kakaoPayService.kakaoPayReady(kakaoReq);

        // 3) tid를 DB에 저장
        salesService.attachTid(req.getOrderId(), res.getTid());

        return ResponseEntity.ok(res);
    }

    // orderId 제거하여 카카오에 요청하는 dto set
    private KakaoReadyReqDto toKakaoReady(KakaoReadyReqDto req) {
        KakaoReadyReqDto dto = new KakaoReadyReqDto();

        dto.setPartner_order_id(req.getPartner_order_id());
        dto.setPartner_user_id(req.getPartner_user_id());
        dto.setItem_name(req.getItem_name());
        dto.setQuantity(req.getQuantity());
        dto.setTotal_amount(req.getTotal_amount());
        dto.setTax_free_amount(req.getTax_free_amount());

        return dto;
    }

    // QR 결제 승인
    @PostMapping("/qr/success")
    public ResponseEntity<KakaoApproveResDto> qrSuccess(
            @RequestParam("pg_token") String pgToken,
            @RequestBody OrderIdReqDto req) {

        Sales sales = salesService.getByOrderIdOrThrow(req.getOrderId());

        // 승인 요청
        KakaoApproveResDto approved = kakaoPayService.kakaoApprove(
                sales.getTid(), req.getPartner_order_id(), req.getPartner_user_id(), pgToken
        );

        Long amount = approved.getAmount() != null
                ? Long.valueOf(approved.getAmount().getTotal())
                : sales.getTotalAmount();

        salesService.complete(req.getOrderId(), PaymentMethod.QR, amount, approved.getApproved_at());

        return ResponseEntity.ok(approved);
    }

    // QR 취소
    @PostMapping("/qr/cancel")
    public ResponseEntity<CommonErrorDTO> qrCancel() {
        CommonErrorDTO dto = CommonErrorDTO.builder()
                .status_code(HttpStatus.BAD_REQUEST.value())
                .status_message("이미 취소된 결제입니다.")
                .build();

        return ResponseEntity.badRequest().body(dto);
    }

    // QR 실패
    @PostMapping("/qr/fail")
    public ResponseEntity<CommonErrorDTO> qrFail() {
        CommonErrorDTO dto = CommonErrorDTO.builder()
                .status_code(HttpStatus.BAD_REQUEST.value())
                .status_message("결제 실패하였습니다.")
                .build();

        return ResponseEntity.badRequest().body(dto);
    }

    // 카운터 결제 선택 (식사 후)
    @PreAuthorize("hasRole('STORE')")
    @PostMapping("/counter/choose")
    public ResponseEntity<String> counterChoose(@RequestBody OrderIdReqDto req) {
        UUID orderId = req.getOrderId();

        // orderId 유효성 체크
        if (!totalOrderRepository.existsById(orderId)) {
            throw new EntityNotFoundException("TotalOrder not found: " + orderId);
        }

        // Sales PENDING 생성/업데이트
        salesService.createPending(
                orderId,
                OrderType.COUNTER,
                null,
                req.getTotalAmount());

        return ResponseEntity.ok("식사 후 카운터에서 결제를 요청해 주세요.");
    }

    // 카운터 결제 완료
    @PreAuthorize("hasRole('STORE')")
    @PostMapping("/counter/complete")
    public ResponseEntity<Void> counterComplete(@RequestBody CounterCompleteReqDto req) {
        salesService.complete(
                req.getOrderId(),
                req.getMethod(),
                req.getTotalAmount() == null ? null : req.getTotalAmount(),
                null
        );

        return ResponseEntity.ok().build();
    }
}