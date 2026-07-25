package com.shawnyu.springbootmall.controller;

import com.shawnyu.springbootmall.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/token")
    public List<String> generateToken(@RequestBody String email) {

        List<String> tokenList = new ArrayList<>();

        String token = jwtUtil.generateToken(email);
        String expiredToken = jwtUtil.generateExpiredToken(email);

        tokenList.add("token: " + token);
        tokenList.add("expired token: " + expiredToken);

        return tokenList;
    }
}