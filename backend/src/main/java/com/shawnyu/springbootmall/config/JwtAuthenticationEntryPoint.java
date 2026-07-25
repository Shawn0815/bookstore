package com.shawnyu.springbootmall.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException ex) throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String message = switch (ex.getClass().getSimpleName()) {
            case "BadCredentialsException" -> "Token 無效";
            case "CredentialsExpiredException" -> "Token 已過期";
            case "UsernameNotFoundException" -> "使用者不存在或帳號已刪除";
            case "AuthenticationCredentialsNotFoundException" -> "缺少授權憑證";
            default -> "未授權存取，請重新登入";
        };

        String json = String.format("""
                {
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "%s",
                  "path": "%s"
                }
                """, message, request.getRequestURI());

        response.getWriter().write(json);

        System.out.println("🔥 JwtAuthenticationEntryPoint: " + ex.getClass().getName());
    }
}