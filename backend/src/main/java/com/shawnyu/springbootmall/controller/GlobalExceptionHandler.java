package com.shawnyu.springbootmall.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    /**
     * 輔助方法：統一組裝 JSON 響應的 Map (包含 status, error, message, timestamp, path)。
     */
    private Map<String, Object> buildResponse(HttpStatus status, String message, String path) {
        Map<String, Object> response = new HashMap<>();

        // 將 status 作為 Key 放入 Map 中
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("timestamp", ZonedDateTime.now().format(FORMATTER));
        response.put("path", path);

        // A/B 錯誤會帶有精準 message，C/D 錯誤則可能為 null 或空字串
        if (message != null && !message.isEmpty()) {
            response.put("message", message);
        }

        return response;
    }

    // --- 1. 處理業務錯誤 (ResponseStatusException) ---
    // 這處理了您丟擲的：throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "密碼錯誤");
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            HttpServletRequest request, ResponseStatusException ex) {

        String path = request.getRequestURI();
        HttpStatus status = (HttpStatus) ex.getStatusCode();
        // 精準使用您在 throw new ResponseStatusException(..., message) 中指定的訊息。
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();

        // **重要：HTTP 狀態碼仍由 ResponseEntity 傳遞**
        return new ResponseEntity<>(
                buildResponse(status, message, path),
                status
        );
    }

    // --- 2. 處理驗證錯誤 (MethodArgumentNotValidException) ---
    // 這處理了您在 Model/DTO 中設定的 @NotBlank(message = "...") 訊息。
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            HttpServletRequest request, MethodArgumentNotValidException ex) {

        String path = request.getRequestURI();
        HttpStatus status = HttpStatus.BAD_REQUEST;

        // 提取最高優先級的訊息 (使用您的自定義排序邏輯)
        Optional<String> topError = getTopPriorityError(ex.getBindingResult().getFieldErrors());

        // 如果 topError.isPresent()，則 finalMessage 為自定義訊息，否則為 null (讓 buildResponse 忽略)
        String finalMessage = topError.orElse(null);

        return new ResponseEntity<>(
                buildResponse(status, finalMessage, path),
                status
        );
    }

    // 輔助方法：整合您的自定義錯誤排序邏輯
    private Optional<String> getTopPriorityError(List<FieldError> fieldErrors) {

        // 1. 優先檢查空白錯誤
        Optional<String> blankError = fieldErrors.stream()
                .filter(err -> Objects.nonNull(err.getDefaultMessage()) && err.getDefaultMessage().contains("不可為空白"))
                .map(FieldError::getDefaultMessage)
                .findFirst();

        if (blankError.isPresent()) {
            return blankError;
        }

        // 2. 如果沒有空白錯誤，則應用您的自定義排序規則
        return fieldErrors.stream()
                .filter(err -> Objects.nonNull(err.getDefaultMessage()) && !err.getDefaultMessage().contains("不可為空白"))
                .sorted(Comparator.comparingInt(this::getErrorOrder))
                .map(FieldError::getDefaultMessage)
                .findFirst();
    }

    // 自訂錯誤排序規則 (沿用您的邏輯)
    private int getErrorOrder(FieldError error) {
        if (error == null || error.getField() == null) return 99;

        String message = error.getDefaultMessage();

        if ("email".equals(error.getField())) {
            return 1;
        }
        if ("password".equals(error.getField())) {
            if (message != null && (message.contains("至少") || message.contains("長度"))) {
                return 2;
            } else {
                return 3;
            }
        }
        return 99;
    }
}