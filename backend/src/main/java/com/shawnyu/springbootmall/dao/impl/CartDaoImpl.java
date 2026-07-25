package com.shawnyu.springbootmall.dao.impl;

import com.shawnyu.springbootmall.dao.CartDao;
import com.shawnyu.springbootmall.dto.CartItemDTO;
import com.shawnyu.springbootmall.dto.CartRequest;
import com.shawnyu.springbootmall.model.CartItem;
import com.shawnyu.springbootmall.rowmapper.CartItemDTORowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CartDaoImpl implements CartDao {

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<CartItemDTO> getCart(Integer userId) {

        String sql = "SELECT ci.cart_item_id, ci.user_id, ci.book_id, ci.quantity, b.title, b.image_url, b.price, b.stock " +
                "FROM cart_item as ci LEFT JOIN book as b ON ci.book_id = b.book_id WHERE user_id = :userId";

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);

        List<CartItemDTO> cartItemList = namedParameterJdbcTemplate.query(sql, map, new CartItemDTORowMapper());

        return cartItemList;
    }

    @Override
    public CartItemDTO getCartItemByBookId(Integer userId, Integer bookId) {

        String sql = "SELECT ci.cart_item_id, ci.user_id, ci.book_id, ci.quantity, b.title, b.image_url, b.price, b.stock " +
                "FROM cart_item as ci LEFT JOIN book as b ON ci.book_id = b.book_id WHERE user_id = :userId AND ci.book_id = :bookId";

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("bookId", bookId);

        List<CartItemDTO> cartItemList = namedParameterJdbcTemplate.query(sql, map, new CartItemDTORowMapper());

        if (cartItemList.size() <= 0) {
            return null;
        }

        return cartItemList.getFirst();
    }

    @Override
    public void upsertCartItem(Integer userId, CartRequest cartRequest) {
        String sql = "INSERT INTO cart_item (user_id, book_id, quantity) VALUES (:userId, :bookId, :quantity) " +
                "ON DUPLICATE KEY UPDATE quantity = :quantity";

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("bookId", cartRequest.getBookId());
        map.put("quantity", cartRequest.getQuantity());

        namedParameterJdbcTemplate.update(sql, map);
    }

    @Override
    public void deleteCartItem(Integer userId, Integer bookId) {

        String sql = "DELETE FROM cart_item WHERE user_id = :userId AND book_id = :bookId";

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("bookId", bookId);

        namedParameterJdbcTemplate.update(sql, map);
    }

    @Override
    public void deleteCart(Integer userId) {

        String sql = "DELETE FROM cart_item WHERE user_id = :userId";

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);

        namedParameterJdbcTemplate.update(sql, map);
    }
}
