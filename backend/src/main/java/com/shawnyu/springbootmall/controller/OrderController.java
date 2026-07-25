package com.shawnyu.springbootmall.controller;

import com.shawnyu.springbootmall.dto.OrderQueryParams;
import com.shawnyu.springbootmall.dto.CreateOrderRequest;
import com.shawnyu.springbootmall.model.Order;
import com.shawnyu.springbootmall.model.User;
import com.shawnyu.springbootmall.service.OrderService;
import com.shawnyu.springbootmall.service.UserService;
import com.shawnyu.springbootmall.util.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    // 注入 UserService，用於通過 Email 查詢 userId
    @Autowired
    private UserService userService;

    // 取得訂單列表
    @GetMapping("/users/orders")
    public ResponseEntity<Page<Order>> getOrders(
            @RequestAttribute("email") String userEmail,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "8") @Max(1000) @Min(0) Integer limit)
    {
        // 根據 Email 取得 userId
        Integer userId = getUserIdFromRequest(userEmail);
        
        // 建立 orderQueryParams
        OrderQueryParams orderQueryParams = new OrderQueryParams();
        orderQueryParams.setUserId(userId);
        orderQueryParams.setPage(page);
        orderQueryParams.setLimit(limit);

        // 取得 order list
        List<Order> orderList = orderService.getOrders(orderQueryParams);

        // 取得 order 總數
        Integer total = orderService.countOrder(orderQueryParams);

        // 分頁
        Page<Order> pageObject = new Page<>();
        pageObject.setPage(page);
        pageObject.setLimit(limit);
        pageObject.setTotal(total);
        pageObject.setItems(orderList);

        return ResponseEntity.status(HttpStatus.OK).body(pageObject);
    }

    @PostMapping("/users/orders")
    public ResponseEntity<?> createOrder(@RequestAttribute("email") String userEmail,
                                         @RequestBody @Valid CreateOrderRequest createOrderRequest) {
        // 根據 Email 取得 userId
        Integer userId = getUserIdFromRequest(userEmail);

        Integer orderId = orderService.createOrder(userId, createOrderRequest);

        Order order = orderService.getOrderById(orderId);

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
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
