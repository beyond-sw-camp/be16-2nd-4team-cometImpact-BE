package com.beyond.jellyorder.common.auth;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// application-local.yml에 있는 카카오페이 설정값을 자바 객체로 매핑하기 위한 설정 클래스
@Component
@ConfigurationProperties(prefix = "kakaopay")
@Data
public class KakaoPayProperties {
    // secretKey 노출 방지
    @ToString.Exclude
    private String devSecretKey;
    private String cid;
    private String approvalUrl;
    private String failUrl;
    private String cancelUrl;
}
