package com.shawnyu.springbootmall.dao;

import com.shawnyu.springbootmall.dto.BuyItem;
import com.shawnyu.springbootmall.dto.CartItemDTO;
import com.shawnyu.springbootmall.dto.CartRequest;
import com.shawnyu.springbootmall.model.CartItem;

import java.util.List;

public interface CartDao {

    public List<CartItemDTO> getCart(Integer userId);

    public CartItemDTO getCartItemByBookId(Integer userId, Integer bookId);

    public void upsertCartItem(Integer userId, CartRequest cartRequest);

    public void deleteCartItem(Integer userId, Integer bookId);

    public void deleteCart(Integer userId);
}
