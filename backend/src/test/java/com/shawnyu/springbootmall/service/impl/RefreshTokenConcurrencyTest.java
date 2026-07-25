package com.shawnyu.springbootmall.service.impl;

import com.shawnyu.springbootmall.dao.RefreshTokenDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 驗證 RefreshTokenDaoImpl#validateAndRevoke 的原子 UPDATE 在高併發下只會成功一次。
 *
 * 模擬情境：同一支 refresh token 同時被 {@link #CONCURRENT_REQUESTS} 個請求拿去用
 * （例如攻擊者重放被盜的 token，跟合法使用者同時 refresh），預期只有恰好 1 個請求成功，
 * 其餘全部因為已被撤銷而失敗。
 */
@SpringBootTest
class RefreshTokenConcurrencyTest {

    private static final int CONCURRENT_REQUESTS = 200;
    private static final String TEST_TOKEN_HASH = "test-token-hash-" + System.currentTimeMillis();

    @Autowired
    private RefreshTokenDao refreshTokenDao;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private Integer testUserId;
    private Integer testRefreshTokenId;

    @BeforeEach
    void setUp() {
        Date now = new Date();

        String userSql = "INSERT INTO user(user_name, email, password, created_date, last_modified_date) " +
                "VALUES (:userName, :email, :password, :createdDate, :lastModifiedDate)";
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userName", "[TEST] refresh token 併發測試用使用者");
        userMap.put("email", "refresh-token-concurrency-test-" + System.currentTimeMillis() + "@example.com");
        userMap.put("password", "not-a-real-password");
        userMap.put("createdDate", now);
        userMap.put("lastModifiedDate", now);

        KeyHolder userKeyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(userSql, new MapSqlParameterSource(userMap), userKeyHolder);
        testUserId = userKeyHolder.getKey().intValue();

        String tokenSql = "INSERT INTO refresh_token(user_id, token_hash, expiry_date, revoked, created_date, last_modified_date) " +
                "VALUES (:userId, :tokenHash, :expiryDate, 0, :now, :now)";
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("userId", testUserId);
        tokenMap.put("tokenHash", TEST_TOKEN_HASH);
        tokenMap.put("expiryDate", new Date(System.currentTimeMillis() + 60_000));
        tokenMap.put("now", now);

        KeyHolder tokenKeyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(tokenSql, new MapSqlParameterSource(tokenMap), tokenKeyHolder);
        testRefreshTokenId = tokenKeyHolder.getKey().intValue();
    }

    @AfterEach
    void tearDown() {
        if (testRefreshTokenId != null) {
            namedParameterJdbcTemplate.update(
                    "DELETE FROM refresh_token WHERE refresh_token_id = :id",
                    Map.of("id", testRefreshTokenId));
        }
        if (testUserId != null) {
            namedParameterJdbcTemplate.update(
                    "DELETE FROM user WHERE user_id = :id",
                    Map.of("id", testUserId));
        }
    }

    @Test
    void concurrentValidateAndRevoke_shouldSucceedExactlyOnce() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(CONCURRENT_REQUESTS);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    int affectedRows = refreshTokenDao.validateAndRevoke(TEST_TOKEN_HASH);
                    if (affectedRows > 0) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 一次性釋放所有 thread，模擬同一個 token 被同時重放使用
        boolean finishedInTime = doneLatch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        assertTrue(finishedInTime, "所有併發請求應在時限內完成");
        assertEquals(1, successCount.get(), "同一個 refresh token 併發使用時，應該恰好只有一次成功");
    }
}
