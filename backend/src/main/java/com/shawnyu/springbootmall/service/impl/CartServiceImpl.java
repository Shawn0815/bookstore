package com.shawnyu.springbootmall.service.impl;

import com.shawnyu.springbootmall.constant.CartStatus;
import com.shawnyu.springbootmall.dao.BookDao;
import com.shawnyu.springbootmall.dao.CartDao;
import com.shawnyu.springbootmall.dao.UserDao;
import com.shawnyu.springbootmall.dto.CartBatchRequest;
import com.shawnyu.springbootmall.dto.CartItemDTO;
import com.shawnyu.springbootmall.dto.CartRequest;
import com.shawnyu.springbootmall.dto.CartResponse;
import com.shawnyu.springbootmall.model.*;
import com.shawnyu.springbootmall.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class CartServiceImpl implements CartService {

    // 創建一個 Log 出來（括號內填入這個 class 的名稱）
    private final static Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private CartDao cartDao;

    @Autowired
    private BookDao bookDao;

    @Autowired
    private UserDao userDao;

    @Override
    public CartResponse getCart(Integer userId) {

        CartResponse cartResponse = new CartResponse();
        int numberOfItmes = 0, total = 0;

        // 1. 呼叫 Dao 取得 cartItemDTOList
        List<CartItemDTO> cartItemList = cartDao.getCart(userId);

        // 2. 依序 loop 每個 CartItem
        for (CartItemDTO cartItem: cartItemList) {

            // a. 檢查書籍是否存在以及庫存是否足夠，並標記
            if (cartItem.getTitle() == null) {
                cartItem.setStatus(CartStatus.DISCONTINUED);
                cartItem.setMessage("商品已下架");

                log.warn("書籍 id = {} 已下架", cartItem.getBookId());
            }
            else if (cartItem.getStock() <= 0) {
                cartItem.setStatus(CartStatus.OUT_OF_STOCK);
                cartItem.setMessage("商品目前缺貨");

                log.warn("書籍 id = {} 目前缺貨，剩餘庫存 {}",
                        cartItem.getBookId(), cartItem.getStock());
            }
            else if (cartItem.getQuantity() > cartItem.getStock()) {
                cartItem.setStatus(CartStatus.OUT_OF_STOCK);
                cartItem.setMessage("庫存不足，僅剩 " + cartItem.getStock() + " 本");

                log.warn("書籍 id = {} 庫存不足，剩餘庫存 {}，欲購買數量 {}",
                        cartItem.getBookId(), cartItem.getStock(), cartItem.getQuantity());
            }
            else if (cartItem.getQuantity() == cartItem.getStock()) {
                cartItem.setStatus(CartStatus.REACHED_LIMIT);
                cartItem.setMessage("已達購買上限（最多可購買 " + cartItem.getStock() + " 本）");
            }
            else {
                cartItem.setStatus(CartStatus.AVAILABLE);
                cartItem.setMessage("商品正常販售中");
            }

            numberOfItmes += cartItem.getQuantity();

            // b. 計算該 CartItem 總金額
            int amount = (cartItem.getStatus() == CartStatus.AVAILABLE || cartItem.getStatus() == CartStatus.REACHED_LIMIT)
                    ? cartItem.getQuantity() * cartItem.getPrice() : 0;
            cartItem.setAmount(amount);

            // c. 計算購物車總金額
            total += amount;
        }

        // 3. 設定回傳 Cart 物件
        cartResponse.setCartItemList(cartItemList);
        cartResponse.setNumberOfItems(numberOfItmes);
        cartResponse.setTotal(total);

        return cartResponse;
    }

    @Override
    public CartResponse updateCart(Integer userId, CartRequest cartRequest) {

        // 1. 檢查商品是否存在，噴 error
        Book book = bookDao.getBookById(cartRequest.getBookId());

        if (book == null) {
            log.warn("書籍 id = {} 已下架，無法新增購物車", cartRequest.getBookId());
            cartDao.deleteCartItem(userId, cartRequest.getBookId()); // 刪除商品
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品已下架，無法更新購物車。"); // 噴 error
        }

        // 2. 檢查商品庫存
        int requested = cartRequest.getQuantity();
        int stock = book.getStock();

        if (stock == 0) {
            log.warn("書籍 id = {} 缺貨，原請求 {} → 修正為 0", book.getBookId(), requested);
            cartDao.deleteCartItem(userId, cartRequest.getBookId()); // 刪除商品
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品目前缺貨，無法加入購物車"); // 噴 error
        }
        else if (stock < requested) {
            cartRequest.setQuantity(stock);
            log.warn("書籍 id = {} 庫存不足，欲購買 {} → 修正為 {}", book.getBookId(), requested, stock);
        }

        // 3. Call Dao 決定新增或創建 CartItem
        cartDao.upsertCartItem(userId, cartRequest);

        return getCart(userId);
    }

    @Override
    public CartResponse mergeCart(Integer userId, CartBatchRequest cartBatchRequest) {

        if (cartBatchRequest == null || cartBatchRequest.getCartItemList() == null ||
                cartBatchRequest.getCartItemList().isEmpty()) {
            return getCart(userId);
        }

        for (CartRequest cartRequest: cartBatchRequest.getCartItemList()) {
            CartItemDTO current = cartDao.getCartItemByBookId(userId, cartRequest.getBookId());

            int newQuantity = (current == null)
                    ? cartRequest.getQuantity()
                    : current.getQuantity() + cartRequest.getQuantity();

            cartRequest.setQuantity(newQuantity);
            cartDao.upsertCartItem(userId, cartRequest);
        }

        return getCart(userId);
    }

    @Override
    public CartResponse deleteCartItem(Integer userId, Integer bookId) {
        cartDao.deleteCartItem(userId, bookId);
        return getCart(userId);
    }

    @Override
    public void deleteCart(Integer userId) {
        cartDao.deleteCart(userId);
    }
}
