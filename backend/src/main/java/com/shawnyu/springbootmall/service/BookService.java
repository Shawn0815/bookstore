package com.shawnyu.springbootmall.service;

import com.shawnyu.springbootmall.dto.BookQueryParams;
import com.shawnyu.springbootmall.model.Book;
import com.shawnyu.springbootmall.dto.BookRequest;
import com.shawnyu.springbootmall.model.Category;

import java.util.List;

public interface BookService {

    List<Category> getCategories();

    Integer countBook(BookQueryParams bookQueryParams);

    List<Book> getBooks(BookQueryParams bookQueryParams);

    Book getBookById(Integer bookId);

    Integer createBook(BookRequest bookRequest);

    void updateBook(Integer bookId, BookRequest bookRequest);

    void deleteBookById(Integer bookId);
}
