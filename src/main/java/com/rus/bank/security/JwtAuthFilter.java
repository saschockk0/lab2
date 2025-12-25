package com.rus.bank.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwt;

    public JwtAuthFilter(JwtTokenProvider jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();
        if (!StringUtils.hasText(token)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            Jws<Claims> jws = jwt.parseAndValidate(token);
            Claims claims = jws.getBody();

            jwt.assertTokenType(claims, "access");

            Long userId = claims.get("uid", Long.class);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            var authorities = List.of(new SimpleGrantedAuthority(role));

            var auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    authorities
            );

            auth.setDetails(userId);

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception ex) {
            // Для публичных эндпоинтов просто пропускаем дальше
            // Spring Security сам проверит permitAll() правила
            SecurityContextHolder.clearContext();
        }

        // Всегда продолжаем цепочку фильтров
        chain.doFilter(request, response);
    }

}