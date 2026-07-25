package com.shawnyu.springbootmall.dao;

import com.shawnyu.springbootmall.dto.BookQueryParams;
import com.shawnyu.springbootmall.model.Book;
import com.shawnyu.springbootmall.dto.BookRequest;
import com.shawnyu.springbootmall.model.Category;

import java.util.List;

public interface BookDao {

    List<Category> getCategories();

    Integer countBook(BookQueryParams bookQueryParams);

    List<Book> getBooks(BookQueryParams bookQueryParams);

    Book getBookById(Integer bookId);

    Integer createBook(BookRequest bookRequest);

    void updateBook(Integer bookId, BookRequest bookRequest);

    void updateStock(Integer bookId, Integer stock);

    void updateSalesCount(Integer bookId, Integer salesCount);

    void deleteBookById(Integer bookId);
}
