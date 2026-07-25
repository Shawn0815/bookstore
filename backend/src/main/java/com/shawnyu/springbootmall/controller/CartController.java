package com.shawnyu.springbootmall.controller;

import com.shawnyu.springbootmall.dto.CartBatchRequest;
import com.shawnyu.springbootmall.dto.CartRequest;
import com.shawnyu.springbootmall.dto.CartResponse;
import com.shawnyu.springbootmall.model.User;
import com.shawnyu.springbootmall.service.CartService;
import com.shawnyu.springbootmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    // 注入 UserService，用於通過 Email 查詢 userId
    @Autowired
    private UserService userService;

    @GetMapping("/users/cart")
    public ResponseEntity<CartResponse> getCart(@RequestAttribute("email") String userEmail) {

        // 1. 根據 Email 取得 userId
        Integer userId = getUserIdFromRequest(userEmail);

        // 2. 取得該 user 購物車內容
        CartResponse cartResponse = cartService.getCart(userId);

        return ResponseEntity.status(HttpStatus.OK).body(cartResponse);
    }

    @PutMapping("/users/cart/items")
    public ResponseEntity<CartResponse> updateCart(@RequestAttribute("email") String userEmail,
                                                   @RequestBody CartRequest cartRequest) {
        // 1. 根據 Email 取得 userId
        Integer userId = getUserIdFromRequest(userEmail);

        // 2. 更新該 user 購物車內容，並回傳
        CartResponse cartResponse = cartService.updateCart(userId, cartRequest);

        return ResponseEntity.status(HttpStatus.OK).body(cartResponse);
    }

    @PostMapping("/users/cart/merge")
    public ResponseEntity<CartResponse> mergeCart(@RequestAttribute("email") String userEmail,
                                                  @RequestBody CartBatchRequest cartBatchRequest) {
        // 1. 根據 Email 取得 userId
        Integer userId = getUserIdFromRequest(userEmail);

        // 2. 更新該 user 購物車內容
        CartResponse cartResponse = cartService.mergeCart(userId, cartBatchRequest);

        return ResponseEntity.status(HttpStatus.OK).body(cartResponse);
    }

    @DeleteMapping("/users/cart/items/{bookId}")
    public ResponseEntity<CartResponse> deleteCart(@RequestAttribute("email") String userEmail,
                                                   @PathVariable Integer bookId) {

        // 1. 根據 Email 取得 userId
        Integer userId = getUserIdFromRequest(userEmail);

        // 2. 刪除該 user 特定購物車商品
        CartResponse cartResponse = cartService.deleteCartItem(userId, bookId);

        return ResponseEntity.status(HttpStatus.OK).body(cartResponse);
    }

    @DeleteMapping("/users/cart")
    public ResponseEntity<?> deleteAllCart(@RequestAttribute("email") String userEmail) {

        // 1. 根據 Email 取得 userId
        Integer userId = getUserIdFromRequest(userEmail);

        // 2. 刪除該 user 所有購物車商品
        cartService.deleteCart(userId);

        return ResponseEntity.status(HttpStatus.OK).body(new CartResponse());
    }

    // Helper Method: 根據 Email 從 Request 屬性中解析出 userId
    private Integer getUserIdFromRequest(String email) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            // 理論上，如果 JWT 有效，使用者就不應該為 null。
            // 但作為安全防護，如果找不到使用者，則拋出未授權錯誤。
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found based on token email.");
        }
        return user.getUserId();
    }

}
