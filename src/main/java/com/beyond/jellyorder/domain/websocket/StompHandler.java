package com.beyond.jellyorder.domain.websocket;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class StompHandler implements ChannelInterceptor {

    @Value("${jwt.secretKeyAt}")
    private String secretKey;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT == accessor.getCommand()) {
            System.out.println("// == connect요청 시 토큰 유효성 검증 중... == //");
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            String token = bearerToken.substring(7);
            // 토큰 검증
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            log.info("토큰 검증 완료");

            // Principal 생성
            String username = String.valueOf(claims.get("storeTableId"));
            var auth = new UsernamePasswordAuthenticationToken(username, null, List.of());
            accessor.setUser(auth);
            System.out.println("WS CONNECT ok: principal=" + username + ", session=" + accessor.getSessionId());
            System.out.println("auth = " + auth);

        }
        // 메시지들어오는 값 로그 확인 로직
        if (StompCommand.SEND.equals(accessor.getCommand())) {
            // 클라이언트가 send() 한 메시지
            log.info("accessor.getDestination = {}", accessor.getDestination());
            log.info("{}", new String((byte[]) message.getPayload()));
        }

        return message;
    }
}
