package com.shawnyu.springbootmall.service.impl;

import com.shawnyu.springbootmall.dao.BookDao;
import com.shawnyu.springbootmall.dto.BookQueryParams;
import com.shawnyu.springbootmall.model.Book;
import com.shawnyu.springbootmall.dto.BookRequest;
import com.shawnyu.springbootmall.model.Category;
import com.shawnyu.springbootmall.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookServiceImpl implements BookService {

    @Autowired
    BookDao bookDao;

    @Override
    public List<Category> getCategories() {
        return bookDao.getCategories();
    }

    @Override
    public Integer countBook(BookQueryParams bookQueryParams) {
        return bookDao.countBook(bookQueryParams);
    }

    @Override
    public List<Book> getBooks(BookQueryParams bookQueryParams) {
        return bookDao.getBooks(bookQueryParams);
    }

    @Override
    public Book getBookById(Integer bookId) {
        return bookDao.getBookById(bookId);
    }

    @Override
    public Integer createBook(BookRequest bookRequest) {
        return bookDao.createBook(bookRequest);
    }

    @Override
    public void updateBook(Integer bookId, BookRequest bookRequest) {
        bookDao.updateBook(bookId, bookRequest);
    }

    @Override
    public void deleteBookById(Integer bookId) {
        bookDao.deleteBookById(bookId);
    }
}
