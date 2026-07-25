package com.shawnyu.springbootmall.rowmapper;

import com.shawnyu.springbootmall.model.Book;
import com.shawnyu.springbootmall.model.Category;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CategoryRowMapper implements RowMapper<Category> {

    @Override
    public Category mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Category category = new Category();

        category.setCategory(resultSet.getString("category"));

        return category;
    }
}
