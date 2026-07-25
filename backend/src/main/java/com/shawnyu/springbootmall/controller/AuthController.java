package com.shawnyu.springbootmall.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class AuthController {

    @GetMapping("/token/check")
    public ResponseEntity<Map<String, Boolean>> checkToken() {
        // 如果 Token 無效，Spring Security 會直接返回 401
        // 如果能進來，就代表 Token 還有效
        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", true);
        return ResponseEntity.ok(response);
    }
}
