package com.example.demo.common.filter;

import com.example.demo.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (token != null) {
                authenticate(token);
            }
            // 无 token 时保持原上下文（匿名用户），不做清理
        } catch (Exception e) {
            // 任何解析异常都不应中断过滤器链，且必须清除上下文防止残留
            log.warn("JWT authentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX_LENGTH);
        }
        return null;
    }

    private void authenticate(String token) {
        String username = jwtUtils.getUsernameFromToken(token);
        if (username == null || !jwtUtils.validateToken(token, username)) {
            // 无效 token 必须清除上下文，防止旧认证残留
            SecurityContextHolder.clearContext();
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Authenticated user: {}", username);
    }
}