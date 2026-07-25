package com.shawnyu.springbootmall.controller;

import com.shawnyu.springbootmall.dto.AuthResponse;
import com.shawnyu.springbootmall.dto.UserLoginRequest;
import com.shawnyu.springbootmall.dto.UserRegisterRequest;
import com.shawnyu.springbootmall.model.User;
import com.shawnyu.springbootmall.service.RefreshTokenService;
import com.shawnyu.springbootmall.service.UserService;
import com.shawnyu.springbootmall.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/users/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid UserRegisterRequest userRegisterRequest) {
        Integer userId = userService.register(userRegisterRequest);

        User user = userService.getUserbyId(userId);

        String token = jwtUtil.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.issueRefreshToken(user.getUserId());

        System.out.println("token: " + token);

        System.out.println("expired token: " + jwtUtil.generateExpiredToken(user.getEmail()));

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token, refreshToken, user));
    }

    @PostMapping("/users/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid UserLoginRequest userLoginRequest) {
        User user = userService.login(userLoginRequest);
        String token = jwtUtil.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.issueRefreshToken(user.getUserId());

        System.out.println("token: " + token);
        System.out.println("expired token: " + jwtUtil.generateExpiredToken(user.getEmail()));

        return ResponseEntity.status(HttpStatus.OK).body(new AuthResponse(token, refreshToken, user));
    }
}
