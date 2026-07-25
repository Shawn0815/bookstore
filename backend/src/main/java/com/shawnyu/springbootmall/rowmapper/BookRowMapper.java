package com.shawnyu.springbootmall.rowmapper;

import com.shawnyu.springbootmall.model.Book;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BookRowMapper implements RowMapper<Book> {

    @Override
    public Book mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Book book = new Book();

        book.setBookId(resultSet.getInt("book_id"));
        book.setTitle(resultSet.getString("title"));
        book.setAuthor(resultSet.getString("author"));
        book.setPublisher(resultSet.getString("publisher"));
        book.setPublishedDate(resultSet.getDate("published_date"));
        book.setPrice(resultSet.getInt("price"));
        book.setCategory(resultSet.getString("category"));
        book.setImageUrl(resultSet.getString("image_url"));
        book.setDescription(resultSet.getString("description"));
        book.setStock(resultSet.getInt("stock"));
        book.setSalesCount(resultSet.getInt("sales_count"));
        book.setCreatedDate(resultSet.getTimestamp("created_date"));
        book.setLastModifiedDate(resultSet.getTimestamp("last_modified_date"));
        book.setOriginalUrl(resultSet.getString("original_url"));

        return book;
    }
}
