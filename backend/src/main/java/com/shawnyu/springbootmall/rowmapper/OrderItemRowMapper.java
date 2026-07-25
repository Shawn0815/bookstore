package com.shawnyu.springbootmall.rowmapper;

import com.shawnyu.springbootmall.model.OrderItem;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderItemRowMapper implements RowMapper<OrderItem> {
    @Override
    public OrderItem mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        OrderItem orderItem = new OrderItem();

        orderItem.setOrderItemId(resultSet.getInt("order_item_id"));
        orderItem.setOrderId(resultSet.getInt("order_id"));
        orderItem.setBookId(resultSet.getInt("book_id"));
        orderItem.setPrice(resultSet.getInt("price"));
        orderItem.setQuantity(resultSet.getInt("quantity"));
        orderItem.setAmount(resultSet.getInt("amount"));

        orderItem.setTitle(resultSet.getString("title"));
        orderItem.setImageUrl(resultSet.getString("image_url"));

        return orderItem;
    }
}
