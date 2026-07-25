package com.shawnyu.springbootmall.service.impl;

import com.shawnyu.springbootmall.dao.RefreshTokenDao;
import com.shawnyu.springbootmall.model.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    private static final long REFRESH_TOKEN_EXPIRATION_MS = 1_209_600_000L; // 14 天

    @Mock
    private RefreshTokenDao refreshTokenDao;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        // @Value 欄位在純 Mockito 測試裡不會被 Spring 注入，手動設定
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION_MS);
    }

    @Test
    void issueRefreshToken_storesHashNotRawToken_andReturnsRawToken() {
        long before = System.currentTimeMillis();

        String rawToken = refreshTokenService.issueRefreshToken(1);

        long after = System.currentTimeMillis();

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Date> expiryCaptor = ArgumentCaptor.forClass(Date.class);
        verify(refreshTokenDao).createRefreshToken(eq(1), hashCaptor.capture(), expiryCaptor.capture());

        assertNotEquals(rawToken, hashCaptor.getValue()); // 存進 DB 的是雜湊值，不是原始 token
        assertEquals(64, hashCaptor.getValue().length()); // SHA-256 十六進位固定 64 字元

        long expiryMillis = expiryCaptor.getValue().getTime();
        assertTrue(expiryMillis >= before + REFRESH_TOKEN_EXPIRATION_MS);
        assertTrue(expiryMillis <= after + REFRESH_TOKEN_EXPIRATION_MS);
    }

    @Test
    void issueRefreshToken_generatesDistinctTokensAcrossCalls() {
        String token1 = refreshTokenService.issueRefreshToken(1);
        String token2 = refreshTokenService.issueRefreshToken(1);

        assertNotEquals(token1, token2);
    }

    @Test
    void validateAndRevoke_success_returnsUserId() {
        when(refreshTokenDao.validateAndRevoke(anyString())).thenReturn(1);
        RefreshToken stored = new RefreshToken();
        stored.setUserId(42);
        when(refreshTokenDao.getByTokenHash(anyString())).thenReturn(stored);

        Integer userId = refreshTokenService.validateAndRevoke("some-raw-token");

        assertEquals(42, userId);
    }

    @Test
    void validateAndRevoke_alreadyUsedOrExpired_throws401() {
        when(refreshTokenDao.validateAndRevoke(anyString())).thenReturn(0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> refreshTokenService.validateAndRevoke("some-raw-token"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void revoke_delegatesToDaoWithHashedToken() {
        refreshTokenService.revoke("some-raw-token");

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(refreshTokenDao).revokeByTokenHash(hashCaptor.capture());
        assertNotEquals("some-raw-token", hashCaptor.getValue());
        assertEquals(64, hashCaptor.getValue().length());
    }
}
