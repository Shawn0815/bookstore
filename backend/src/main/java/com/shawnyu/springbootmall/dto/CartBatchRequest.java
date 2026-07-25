package com.shawnyu.springbootmall.dto;

import com.shawnyu.springbootmall.model.CartItem;

import java.util.List;

public class CartBatchRequest {

    private List<CartRequest> cartItemList;

    public List<CartRequest> getCartItemList() {
        return cartItemList;
    }

    public void setCartItemList(List<CartRequest> cartItemList) {
        this.cartItemList = cartItemList;
    }
}
