package com.shawnyu.springbootmall.dto;

import com.shawnyu.springbootmall.model.User;

public class AuthResponse {
    private final String token;
    private final String refreshToken;
    private final User user;

    public AuthResponse(String token, String refreshToken, User user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
    public User getUser() { return user; }
}
