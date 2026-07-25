package com.shawnyu.springbootmall.service;

import com.shawnyu.springbootmall.dto.CartBatchRequest;
import com.shawnyu.springbootmall.dto.CartRequest;
import com.shawnyu.springbootmall.dto.CartResponse;

public interface CartService {

    public CartResponse getCart(Integer userId);

    public CartResponse updateCart(Integer userId, CartRequest cartRequest);

    public CartResponse mergeCart(Integer userId, CartBatchRequest cartBatchRequest);

    public CartResponse deleteCartItem(Integer userId, Integer bookId);

    public void deleteCart(Integer userId);
}
