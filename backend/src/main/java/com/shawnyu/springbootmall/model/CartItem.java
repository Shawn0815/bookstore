package com.shawnyu.springbootmall.model;

import com.shawnyu.springbootmall.constant.CartStatus;

public class CartItem {

    // 資料庫欄位
    private Integer cartItemId;
    private Integer userId;
    private Integer bookId;
    private Integer quantity;

//    // 新增欄位
//    private String title;
//    private String imageUrl;
//    private Integer price;
//    private Integer stock;
//    private Integer amount; // Service 層計算
//
//    // 判斷欄位
//    private CartStatus status;
//    private String message;

    public Integer getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Integer cartItemId) {
        this.cartItemId = cartItemId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}