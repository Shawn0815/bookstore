package com.shawnyu.springbootmall.controller;

import com.shawnyu.springbootmall.dto.AuthResponse;
import com.shawnyu.springbootmall.dto.RefreshTokenRequest;
import com.shawnyu.springbootmall.model.User;
import com.shawnyu.springbootmall.service.RefreshTokenService;
import com.shawnyu.springbootmall.service.UserService;
import com.shawnyu.springbootmall.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @GetMapping("/token/check")
    public ResponseEntity<Map<String, Boolean>> checkToken() {
        // 如果 Token 無效，Spring Security 會直接返回 401
        // 如果能進來，就代表 Token 還有效
        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        Integer userId = refreshTokenService.validateAndRevoke(request.getRefreshToken());

        User user = userService.getUserbyId(userId);
        String accessToken = jwtUtil.generateToken(user.getEmail());
        String newRefreshToken = refreshTokenService.issueRefreshToken(userId);

        return ResponseEntity.ok(new AuthResponse(accessToken, newRefreshToken, user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshTokenRequest request) {
        refreshTokenService.revoke(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
