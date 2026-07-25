package com.shawnyu.springbootmall.dto;

import jakarta.validation.constraints.NotNull;

public class CartRequest {

    @NotNull
    private int bookId;

    @NotNull
    private int quantity;

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
