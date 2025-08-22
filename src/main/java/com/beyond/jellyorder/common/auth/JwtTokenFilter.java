package com.beyond.jellyorder.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component // (service 와 유사한 성격)
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    @Value("${jwt.secretKeyAt}")
    private String secretKey;

    private Key secret_at_key;

    @PostConstruct
    public void init() {
        this.secret_at_key = new SecretKeySpec(
                java.util.Base64.getDecoder().decode(secretKey),
                io.jsonwebtoken.SignatureAlgorithm.HS512.getJcaName()
        );
    }

//    private static final List<String> authFree = List.of(
//            "/store/create",
//            "/store/do-login",
//            "/store-table/do-login",
//            "/store/refresh-at",
//            "/store-table/refresh-at",
//            "/sse/**",
//            "/payment/**",
//            "/v3/api-docs/**",  // swagger 추가
//            "/swagger-ui/**",   // swagger 추가
//            "/swagger-ui.html",  // swagger 추가
//            "/request/**",
//            "/password/**",
//            "/store/check-login-id",
//            "/store/check-business-number"
//    );
//
//    private boolean isAuthFree(HttpServletRequest httpServletRequest) {
//        if ("OPTIONS".equalsIgnoreCase(httpServletRequest.getMethod())) {
//            return true;
//        }
//        String url = httpServletRequest.getRequestURI();
//        for (String authList : authFree) {
//            if (url.startsWith(authList)) {
//                return true;
//            }
//        }
//        return false;
//    }

//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        try {
//            HttpServletRequest req = (HttpServletRequest) servletRequest;
//
//            String bearerToken = req.getHeader("Authorization");
//            if (bearerToken == null || !bearerToken.startsWith("Bearer "))   {
//                filterChain.doFilter(servletRequest, servletResponse);
//                return;
//            }
//            String token = bearerToken.substring(7);
//            Claims claims = Jwts.parserBuilder()
//                    .setSigningKey(secret_at_key)
//                    .build()
//                    .parseClaimsJws(token)
//                    .getBody();
//
//            req.setAttribute("storeId", claims.get("storeId", String.class));
//            req.setAttribute("storeTableId", claims.get("storeTableId", String.class));
//
//            List<GrantedAuthority> authorities = new ArrayList<>();
//            authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role")));
//            Authentication authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
//        filterChain.doFilter(servletRequest, servletResponse);
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        if (isAuthFree(request)) {
//            filterChain.doFilter(request, response);
//            return;
//        }
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
//                return;
            return;
        }

        String token = bearerToken.substring(7);

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secret_at_key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            request.setAttribute("storeId", claims.get("storeId", String.class));
            request.setAttribute("storeTableId", claims.get("storeTableId", String.class));

            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role")));
            Authentication authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}
