package com.example.demo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // 缓存密钥，避免重复生成
    private SecretKey secretKey;

    /**
     * 懒加载获取签名密钥
     */
    private SecretKey getSignKey() {
        if (secretKey == null) {
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            secretKey = Keys.hmacShaKeyFor(keyBytes);
        }
        return secretKey;
    }

    /**
     * 生成 Token（可扩展 roles 参数）
     */
    public String generateToken(String username) {
        return generateToken(username, Collections.emptyList());
    }

    /**
     * 生成包含角色的 Token
     */
    public String generateToken(String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles); // 将角色写入 claims
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())
                .compact();
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * 从 Token 中获取角色列表（新增）
     */
    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaims(token);
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?>) {
            return ((List<?>) rolesObj).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token, String username) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            boolean valid = tokenUsername.equals(username) && !isTokenExpired(token);
            if (!valid) {
                log.warn("Token validation failed: username mismatch or expired");
            }
            return valid;
        } catch (Exception e) {
            log.warn("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断 Token 是否过期
     */
    public boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    /**
     * 解析 Claims（内部统一异常处理可选，此处保持抛出，由调用方处理）
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())  // 直接传入 SecretKey，无需强制转换
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}