package com.shawnyu.springbootmall.service.impl;

import com.shawnyu.springbootmall.dao.RefreshTokenDao;
import com.shawnyu.springbootmall.model.RefreshToken;
import com.shawnyu.springbootmall.service.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Component
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final static Logger log = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);

    @Autowired
    private RefreshTokenDao refreshTokenDao;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Override
    public String issueRefreshToken(Integer userId) {
        String rawToken = generateRawToken();
        Date expiryDate = new Date(System.currentTimeMillis() + refreshTokenExpiration);

        refreshTokenDao.createRefreshToken(userId, sha256Hex(rawToken), expiryDate);

        return rawToken;
    }

    @Override
    public Integer validateAndRevoke(String rawRefreshToken) {
        String tokenHash = sha256Hex(rawRefreshToken);

        int affectedRows = refreshTokenDao.validateAndRevoke(tokenHash);
        if (affectedRows == 0) {
            log.warn("refresh token 無效、已過期或已被使用過");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token 無效或已過期，請重新登入");
        }

        RefreshToken refreshToken = refreshTokenDao.getByTokenHash(tokenHash);
        return refreshToken.getUserId();
    }

    @Override
    public void revoke(String rawRefreshToken) {
        refreshTokenDao.revokeByTokenHash(sha256Hex(rawRefreshToken));
    }

    private String generateRawToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
