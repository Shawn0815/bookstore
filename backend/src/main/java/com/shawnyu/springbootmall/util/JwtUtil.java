package com.shawnyu.springbootmall.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Base64;

@Component
public class JwtUtil {

    // 透過 Spring 的 @Value 註解，從 application.properties 讀取密鑰
    // 這樣可以避免將密鑰硬編碼在程式碼中，更安全。
    @Value("${jwt.secret}")
    private String secret;

    // 從 application.properties 讀取 access token 有效時間（毫秒）
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    // 根據 Base64 編碼的密鑰字串，生成用於簽名的 Key
    private Key getSigningKey() {
        // 將 Base64 字串解碼為位元組陣列
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /*
     * 生成 JWT Token
     * @param email 用於設定 Token 的主題 (Subject)
     * @return JWT 字串
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email) // 設定主題 (通常是使用者 ID 或 Email)
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /*
     * 驗證 Token 並取出主體 (Subject/Email)
     * @param token 待驗證的 JWT
     * @return 驗證成功則回傳 Email
     * @throws io.jsonwebtoken.JwtException 如果 Token 無效或過期，會拋出例外
     */
    public String validateToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /* 生成過期 token */
    public String generateExpiredToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis() - accessTokenExpiration - 1))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
