package com.example.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT (JSON Web Token) 工具
 *
 * 使用 HMAC 簽章演算法，生成 JWT token
 *
 * @Value("${jwt.secret}") 從 application.properties 讀取 JWT secret key
 * @Value("${jwt.expiration}") 從 application.properties 讀取 JWT token 的過期時間
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpiration;

    /**
     * 產生 JWT token
     *
     * @param userDetails 使用者資訊
     * @return JWT token
     */
    public String generateToken(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("UserDetails cannot be null");
        }
        
        if (userDetails.getUsername() == null || userDetails.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        return Jwts.builder()
            // JWT 的 subject，通常放 username 或 userId
            .setSubject(userDetails.getUsername())
            // Token 簽發時間
            .setIssuedAt(new Date())
            // Token 過期時間
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration * 1000))
            // 使用 secret key 進行簽章
            .signWith(getSigningKey())
            // 產生最終 JWT 字串
            .compact();
    }

    /**
     * 解析 JWT token 並返回 Claims
     *
     * @param token JWT token
     * @return Claims 對象，解析失敗返回 null
     */
    private Claims parseTokenClaims(String token) {
        if (token == null || token.trim().isEmpty()) {
            logger.warn("JWT token is null or empty");
            return null;
        }
        
        try {
            return Jwts.parserBuilder()
                // 設定驗證用的 secret key
                .setSigningKey(getSigningKey())
                // 允許 30 秒的時間差（處理時鐘不同步）
                .setAllowedClockSkewSeconds(30)
                .build()
                // 解析 token 並驗證 signature
                .parseClaimsJws(token)
                .getBody();
        } catch (io.jsonwebtoken.security.SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            return null;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            return null;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            return null;
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error parsing JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 從 JWT token 取出使用者名稱
     *
     * @param token JWT token
     * @return 使用者名稱，解析失敗返回 null
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseTokenClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 驗證 JWT token
     *
     * @param token JWT token
     * @param userDetails 使用者資訊
     * @return 是否驗證成功
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        if (token == null || token.trim().isEmpty()) {
            logger.warn("JWT token is null or empty");
            return false;
        }
        
        if (userDetails == null) {
            logger.warn("UserDetails is null");
            return false;
        }
        
        Claims claims = parseTokenClaims(token);
        if (claims == null) {
            return false;
        }
        
        try {
            final String username = claims.getSubject();
            return (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(claims));
        } catch (Exception e) {
            logger.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 檢查 JWT token 是否過期
     *
     * @param token JWT token
     * @return 是否過期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = parseTokenClaims(token);
        return isTokenExpired(claims);
    }

    /**
     * 檢查 Claims 是否過期
     *
     * @param claims JWT Claims
     * @return 是否過期
     */
    private boolean isTokenExpired(Claims claims) {
        if (claims == null) {
            return true;
        }
        
        try {
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            logger.error("Failed to check JWT token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 取得用於簽章的 Key
     *
     * @return 用於簽章的 Key
     */
    private Key getSigningKey() {
        // 將 Base64 secret 解碼
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        // 產生 HMAC SHA Key
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
