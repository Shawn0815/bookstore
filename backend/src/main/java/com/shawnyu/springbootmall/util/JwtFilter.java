package com.shawnyu.springbootmall.util;

import com.shawnyu.springbootmall.model.User;
import com.shawnyu.springbootmall.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthenticationCredentialsNotFoundException("缺少授權憑證");
        }

        try {
            String token = authHeader.substring(7);
            String email = jwtUtil.validateToken(token); // 驗證簽章與過期時間

            User user = userService.getUserByEmail(email);
            if (user == null) {
                // Token 合法但使用者不存在（可能帳號被刪除）
                throw new UsernameNotFoundException("使用者不存在");
            }

            Authentication authentication =
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            email, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("email", email);

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new CredentialsExpiredException("Token 已過期");
        } catch (io.jsonwebtoken.JwtException e) {
            throw new BadCredentialsException("Token 無效");
        } catch (UsernameNotFoundException e) {
            throw e; // 讓 ExceptionTranslationFilter 抓到
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/users/login")
                || path.startsWith("/users/register")
                || path.startsWith("/users/token/refresh")
                || path.startsWith("/users/logout")
                || path.startsWith("/books")
                || path.startsWith("/categories")
                || path.startsWith("/test")
                || path.startsWith("/error");
    }
}