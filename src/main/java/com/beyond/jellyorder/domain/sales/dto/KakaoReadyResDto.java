package com.beyond.jellyorder.domain.sales.dto;

import lombok.Data;

@Data
public class KakaoReadyResDto {
    private String tid;                         // 결제 고유 번호
    private String next_redirect_app_url;       // 요청한 클라이언트가 모바일 앱일 경우
    private String next_redirect_mobile_url;    // 요청한 클라이언트가 모바일 웹일 경우
    private String next_redirect_pc_url;        // 요청한 클라이언트가 PC 웹일 경우
    private String android_app_scheme;          // 카카오페이 결제 화면으로 이동(안드로이드)
    private String ios_app_scheme;              // 카카오페이 결제 화면으로 이동(ios)
    private String created_at;                  // 결제 준비 요청 시간
}