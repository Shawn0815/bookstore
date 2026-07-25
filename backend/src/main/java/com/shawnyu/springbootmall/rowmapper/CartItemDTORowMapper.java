package com.shawnyu.springbootmall.rowmapper;

import com.shawnyu.springbootmall.dto.CartItemDTO;
import com.shawnyu.springbootmall.model.CartItem;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CartItemDTORowMapper implements RowMapper<CartItemDTO> {

    @Override
    public CartItemDTO mapRow(ResultSet resultSet, int rowNum) throws SQLException {

        CartItemDTO CartItemDTO = new CartItemDTO();

        CartItemDTO.setCartItemId(resultSet.getInt("cart_item_id"));
        CartItemDTO.setUserId(resultSet.getInt("user_id"));
        CartItemDTO.setBookId(resultSet.getInt("book_id"));
        CartItemDTO.setQuantity(resultSet.getInt("quantity"));

        CartItemDTO.setTitle(resultSet.getString("title"));
        CartItemDTO.setImageUrl(resultSet.getString("image_url"));
        CartItemDTO.setPrice(resultSet.getInt("price"));
        CartItemDTO.setStock(resultSet.getInt("stock"));

        return CartItemDTO;
    }
}
