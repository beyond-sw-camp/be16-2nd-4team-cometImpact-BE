package com.beyond.jellyorder.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtTokenFilter extends GenericFilter {
    @Value("${jwt.secretKeyAt}")
    private String secretKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken == null){
            chain.doFilter(req, response);
            return;
        }

        String token = bearerToken.substring(7);

        Claims claims = Jwts.parserBuilder()
                            .setSigningKey(secretKey)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role").toString()));

        Authentication authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(),' ', authorities);

        SecurityContextHolder.getContext().
                setAuthentication(authentication);

        chain.doFilter(req, response);
    }
}