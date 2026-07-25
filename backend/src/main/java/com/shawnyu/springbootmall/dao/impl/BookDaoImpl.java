package com.shawnyu.springbootmall.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.shawnyu.springbootmall.dao.BookDao;
import com.shawnyu.springbootmall.dto.BookQueryParams;
import com.shawnyu.springbootmall.dto.BookRequest;
import com.shawnyu.springbootmall.dto.BookSortColumn;
import com.shawnyu.springbootmall.model.Book;
import com.shawnyu.springbootmall.model.Category;
import com.shawnyu.springbootmall.rowmapper.BookRowMapper;
import com.shawnyu.springbootmall.rowmapper.CategoryRowMapper;

@Component
public class BookDaoImpl implements BookDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<Category> getCategories() {
        String sql = "SELECT DISTINCT category FROM book";

        Map<String, Object> map = new HashMap<>();

        List<Category> categoryList = namedParameterJdbcTemplate.query(sql, map, new CategoryRowMapper());

        return categoryList;
    }

    @Override
    public Integer countBook(BookQueryParams bookQueryParams) {
        String sql = "SELECT COUNT(*) FROM book WHERE 1=1";

        Map<String, Object> map = new HashMap<>();

        // 查詢條件
        if (bookQueryParams.getCategory() != null) {
            sql = sql + " AND category = :category";
            map.put("category", bookQueryParams.getCategory());
        }

        if (bookQueryParams.getSearch() != null) {
            sql = sql + " AND title LIKE :search";
            map.put("search", "%" + bookQueryParams.getSearch() + "%");
        }

        Integer total = namedParameterJdbcTemplate.queryForObject(sql, map, Integer.class);

        return total;
    }

    @Override
    public List<Book> getBooks(BookQueryParams bookQueryParams) {
        String sql = "SELECT book_id, title, author, publisher, published_date, price, category, image_url, " +
                "description, stock, sales_count, created_date, last_modified_date, original_url  FROM book WHERE 1=1";

        Map<String, Object> map = new HashMap<>();

        // 查詢條件
        if (bookQueryParams.getCategory() != null) {
            sql = sql + " AND category = :category";
            map.put("category", bookQueryParams.getCategory());
        }

        if (bookQueryParams.getSearch() != null) {
            sql = sql + " AND (title LIKE :search OR author LIKE :search)";
            map.put("search", "%" + bookQueryParams.getSearch() + "%");
        }

        // 排序：sortBy/order 直接來自使用者輸入，透過白名單轉換避免 SQL Injection
        String sortColumn = BookSortColumn.fromParam(bookQueryParams.getSortBy()).getColumnName();
        String sortOrder = "asc".equalsIgnoreCase(bookQueryParams.getOrder()) ? "ASC" : "DESC";
        // book_id 當作次要排序鍵，確保排序值相同時分頁結果穩定；
        // 方向要跟主排序一致，索引才能整個往同一個方向掃過去，不然 MySQL 還是得多做一次 filesort
        sql = sql + " ORDER BY " + sortColumn + " " + sortOrder + ", book_id " + sortOrder;

        // 分頁
        sql = sql + " LIMIT :limit OFFSET :offset";
        map.put("limit", bookQueryParams.getLimit());
        map.put("offset", (bookQueryParams.getPage() - 1) * bookQueryParams.getLimit());

        List<Book> bookList = namedParameterJdbcTemplate.query(sql, map, new BookRowMapper());

        return bookList;
    }

    @Override
    public Book getBookById(Integer bookId) {
        String sql = "SELECT book_id, title, author, publisher, published_date, price, category, image_url, " +
                "description, stock, sales_count, created_date, last_modified_date, original_url  FROM book WHERE book_id = :bookId";

        Map<String, Object> map = new HashMap<>();
        map.put("bookId", bookId);

        List<Book> bookList = namedParameterJdbcTemplate.query(sql, map, new BookRowMapper());

        if (bookList.size() <= 0) {
            return null;
        }

        return bookList.get(0);
    }

    @Override
    public Integer createBook(BookRequest bookRequest) {
        String sql = "INSERT INTO book(title, author, publisher, published_date, price, category, image_url, description, " +
                "stock, sales_count, created_date, last_modified_date, original_url) VALUES " +
                "(:title, :author, :publisher, :published_date, :price, :category, :image_url, :description, :stock, :salesCount, " +
                " :created_date, :last_modified_date, originalUrl)";

        Map<String, Object> map = new HashMap<>();
        map.put("title", bookRequest.getTitle());
        map.put("author", bookRequest.getAuthor());
        map.put("publisher", bookRequest.getPublisher());
        map.put("published_date", bookRequest.getPublishedDate());
        map.put("price", bookRequest.getPrice());
        map.put("category", bookRequest.getCategory());
        map.put("image_url", bookRequest.getImageUrl());
        map.put("description", bookRequest.getDescription());
        map.put("stock", bookRequest.getStock());
        map.put("salesCount", bookRequest.getSalesCount());
        map.put("originalUrl", bookRequest.getOriginalUrl());


        Date now = new Date();
        map.put("created_date", now);
        map.put("last_modified_date", now);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);

        Integer bookId = keyHolder.getKey().intValue();

        return bookId;
    }

    @Override
    public void updateBook(Integer bookId, BookRequest bookRequest) {
        String sql = "UPDATE book SET title = :title, author = :author, publisher = :publisher, published_date = :publishedDate, " +
                "price = :price, category = :category, image_url = :imageUrl, description = :description, " +
                "stock = :stock, sales_count = :salesCount, original_url = :originalUrl WHERE book_id = :bookId";

        Map<String, Object> map = new HashMap<>();
        map.put("bookId", bookId); // 記得要額外設定成參數值
        map.put("title", bookRequest.getTitle());
        map.put("author", bookRequest.getAuthor());
        map.put("publisher", bookRequest.getPublisher());
        map.put("publishedDate", bookRequest.getPublishedDate());
        map.put("price", bookRequest.getPrice());
        map.put("category", bookRequest.getCategory());
        map.put("imageUrl", bookRequest.getImageUrl());
        map.put("description", bookRequest.getDescription());
        map.put("stock", bookRequest.getStock());
        map.put("salesCount", bookRequest.getSalesCount());
        map.put("orinalUrl", bookRequest.getOriginalUrl());
        map.put("last_modified_date", new Date());

        namedParameterJdbcTemplate.update(sql, map);
    }

    @Override
    public int updateStock(Integer bookId, Integer quantity) {
        // 使用 atomic update 來保證原子性
        String sql = "UPDATE book SET stock = stock - :quantity, last_modified_date = :lastModifiedDate " +
                "WHERE book_id = :bookId AND stock >= :quantity";

        Map<String, Object> map = new HashMap<>();
        map.put("bookId", bookId);
        map.put("quantity", quantity);
        map.put("lastModifiedDate", new Date());

        return namedParameterJdbcTemplate.update(sql, map);
    }

    @Override
    public void updateSalesCount(Integer bookId, Integer salesCount) {
        String sql = "UPDATE book SET sales_count = :salesCount, last_modified_date = :lastModifiedDate " +
                "WHERE book_id = :bookId";

        Map<String, Object> map = new HashMap<>();
        map.put("bookId", bookId);
        map.put("salesCount", salesCount);
        map.put("lastModifiedDate", new Date());

        namedParameterJdbcTemplate.update(sql, map);
    }

    @Override
    public void deleteBookById(Integer bookId) {
        String sql = "DELETE FROM book WHERE book_id = :bookId";

        Map<String, Object> map = new HashMap<>();
        map.put("bookId", bookId);

        namedParameterJdbcTemplate.update(sql, map);
    }
}
