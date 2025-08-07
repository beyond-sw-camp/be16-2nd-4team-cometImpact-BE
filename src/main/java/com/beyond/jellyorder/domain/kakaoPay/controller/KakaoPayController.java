package com.beyond.jellyorder.domain.kakaoPay.controller;

import com.beyond.jellyorder.common.exception.CommonErrorDTO;
import com.beyond.jellyorder.domain.kakaoPay.dto.KakaoApproveResDto;
import com.beyond.jellyorder.domain.kakaoPay.dto.KakaoReadyReqDto;
import com.beyond.jellyorder.domain.kakaoPay.dto.KakaoReadyResDto;
import com.beyond.jellyorder.domain.kakaoPay.service.KakaoPayService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;

    /** 결제 요청 */
    @PostMapping("/ready")
    public KakaoReadyResDto ready(@RequestBody KakaoReadyReqDto reqDto, HttpSession session) {
        KakaoReadyResDto resDto = kakaoPayService.kakaoPayReady(reqDto);
        session.setAttribute("tid", resDto.getTid());
        session.setAttribute("partner_order_id", reqDto.getPartner_order_id());
        session.setAttribute("partner_user_id", reqDto.getPartner_user_id());

        return resDto;
    }

    /** 결제 성공 */
    @PostMapping("/success")
    public ResponseEntity<KakaoApproveResDto> success(@RequestParam("pg_token") String pgToken, HttpSession session) {
        String tid = (String) session.getAttribute("tid");
        String partnerOrderId = (String) session.getAttribute("partner_order_id");
        String partnerUserId = (String) session.getAttribute("partner_user_id");

        if (tid == null || partnerOrderId == null || partnerUserId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // 승인 요청
        KakaoApproveResDto approveResDto = kakaoPayService.kakaoApprove(tid, partnerOrderId, partnerUserId, pgToken);

        return ResponseEntity.ok(approveResDto);
    }

    /** 결제 취소 */
    @GetMapping("/cancel")
    public ResponseEntity<CommonErrorDTO> cancel() {
        CommonErrorDTO dto = CommonErrorDTO.builder()
                .status_code(HttpStatus.BAD_REQUEST.value())
                .status_message("이미 취소된 결제입니다.")
                .build();

        return ResponseEntity.badRequest().body(dto);
    }

    /** 결제 실패 */
    @GetMapping("/fail")
    public ResponseEntity<CommonErrorDTO> fail() {
        CommonErrorDTO dto = CommonErrorDTO.builder()
                .status_code(HttpStatus.BAD_REQUEST.value())
                .status_message("결제 실패하였습니다.")
                .build();

        return ResponseEntity.badRequest().body(dto);
    }
}
