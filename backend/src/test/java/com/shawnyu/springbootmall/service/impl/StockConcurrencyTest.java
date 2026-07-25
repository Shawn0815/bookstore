package com.shawnyu.springbootmall.service.impl;

import com.shawnyu.springbootmall.dao.BookDao;
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
 * 驗證 BookDaoImpl#updateStock 改成原子 UPDATE 之後，在高併發下不會超賣。
 *
 * 模擬情境：同一本書庫存只有 {@link #INITIAL_STOCK} 本，卻同時有
 * {@link #CONCURRENT_REQUESTS} 個請求搶購 1 本，預期只有恰好 INITIAL_STOCK
 * 個請求成功，其餘全部因庫存不足失敗，且最終庫存不會變成負數。
 */
@SpringBootTest
class StockConcurrencyTest {

    private static final int INITIAL_STOCK = 50;
    private static final int CONCURRENT_REQUESTS = 200;

    @Autowired
    private BookDao bookDao;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private Integer testBookId;

    @BeforeEach
    void setUp() {
        String sql = "INSERT INTO book(title, author, isbn, publisher, published_date, price, category, image_url, " +
                "description, stock, sales_count, created_date, last_modified_date, original_url) VALUES " +
                "(:title, :author, :isbn, :publisher, :publishedDate, :price, :category, :imageUrl, :description, " +
                ":stock, :salesCount, :createdDate, :lastModifiedDate, :originalUrl)";

        Date now = new Date();
        Map<String, Object> map = new HashMap<>();
        map.put("title", "[TEST] 併發扣庫存測試用書");
        map.put("author", "test");
        map.put("isbn", "TEST-" + System.currentTimeMillis());
        map.put("publisher", "test");
        map.put("publishedDate", now);
        map.put("price", 100);
        map.put("category", "test");
        map.put("imageUrl", "http://example.com/test.png");
        map.put("description", "for concurrency test only, safe to delete");
        map.put("stock", INITIAL_STOCK);
        map.put("salesCount", 0);
        map.put("createdDate", now);
        map.put("lastModifiedDate", now);
        map.put("originalUrl", "http://example.com/test");

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);
        testBookId = keyHolder.getKey().intValue();
    }

    @AfterEach
    void tearDown() {
        if (testBookId != null) {
            namedParameterJdbcTemplate.update(
                    "DELETE FROM book WHERE book_id = :bookId",
                    Map.of("bookId", testBookId));
        }
    }

    @Test
    void concurrentUpdateStock_shouldNeverOversell() throws InterruptedException {
        // 每個請求各配一條 thread，避免執行緒池不足時彼此卡住 startLatch
        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(CONCURRENT_REQUESTS);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    int affectedRows = bookDao.updateStock(testBookId, 1);
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

        startLatch.countDown(); // 一次性釋放所有 thread，模擬同時下單搶購
        boolean finishedInTime = doneLatch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        assertTrue(finishedInTime, "所有併發請求應在時限內完成");

        Integer finalStock = namedParameterJdbcTemplate.queryForObject(
                "SELECT stock FROM book WHERE book_id = :bookId",
                Map.of("bookId", testBookId), Integer.class);

        assertEquals(INITIAL_STOCK, successCount.get(), "成功扣減次數應該恰好等於原始庫存，代表沒有多賣給任何人");
        assertEquals(0, finalStock, "最終庫存應該恰好被扣到 0");
        assertTrue(finalStock >= 0, "庫存絕對不能變成負數（超賣）");
    }
}
