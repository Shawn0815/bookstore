package com.shawnyu.springbootmall.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List; // 移除對 List 的依賴

/**
 * 統一錯誤回應發送工具，輸出格式採用單一 message，對齊 Spring Boot 原生格式。
 */
public class ErrorResponseSender {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    /**
     * 發送統一格式的 JSON 錯誤回應
     * @param response HTTP Response 物件
     * @param status HTTP 狀態碼
     * @param errorDescription 狀態碼的 HTTP 標準描述 (例如 "Unauthorized")
     * @param message 唯一的錯誤訊息
     * @param requestPath 發生錯誤的請求路徑
     */
    public static void sendErrorResponse(HttpServletResponse response,
                                         int status,
                                         String errorDescription,
                                         String message,
                                         String requestPath) throws IOException {

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String timestamp = ZonedDateTime.now().format(FORMATTER);

        // 統一錯誤格式：使用單一 "message" 欄位
        String jsonError = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                timestamp,
                status,
                errorDescription,
                message.replace("\"", "\\\""), // 確保訊息中的引號被跳脫
                requestPath
        );

        response.getWriter().write(jsonError);
    }
}