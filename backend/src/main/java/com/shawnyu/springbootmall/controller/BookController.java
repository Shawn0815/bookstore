package com.shawnyu.springbootmall.controller;

import com.shawnyu.springbootmall.dto.BookQueryParams;
import com.shawnyu.springbootmall.model.Book;
import com.shawnyu.springbootmall.dto.BookRequest;
import com.shawnyu.springbootmall.model.Category;
import com.shawnyu.springbootmall.service.BookService;
import com.shawnyu.springbootmall.util.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
public class BookController {

    @Autowired
    BookService bookService;

    // 取得所有類別
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        List<Category> categoryList = bookService.getCategories();

        return ResponseEntity.status(HttpStatus.OK).body(categoryList);
    }

    // 取得所有書籍（可篩選）
    @GetMapping("/books")
    public ResponseEntity<Page<Book>> getBooks(
            // 查詢條件 Filtering
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,

            // 排序 Sorting
            @RequestParam(defaultValue = "published_date") String sortBy,
            @RequestParam(defaultValue = "desc") String order,

            // 分頁 Pagination
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "8") @Max(1000) @Min(0) Integer limit
    ) {

        BookQueryParams bookQueryParams = new BookQueryParams();
        bookQueryParams.setCategory(category);
        bookQueryParams.setSearch(search);
        bookQueryParams.setSortBy(sortBy);
        bookQueryParams.setOrder(order);
        bookQueryParams.setPage(page);
        bookQueryParams.setLimit(limit);

        // 取得 book list
        List<Book> bookList = bookService.getBooks(bookQueryParams);

        // 取得 book 總數
        Integer total = bookService.countBook(bookQueryParams);

        // 分頁
        Page<Book> pageObject = new Page<>();
        pageObject.setPage(page);
        pageObject.setLimit(limit);
        pageObject.setTotal(total);
        pageObject.setItems(bookList);

        return ResponseEntity.status(HttpStatus.OK).body(pageObject);
    }

    // 取得特定 id 的書籍
    @GetMapping("/books/{bookId}")
    public ResponseEntity<Book> getBook(@PathVariable Integer bookId) {
        Book book = bookService.getBookById(bookId);

        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(book);
    }

    // Create：新增書籍
    @PostMapping("/books")
    public ResponseEntity<Book> createBook(@RequestBody @Valid BookRequest bookRequest) {
        Integer bookId = bookService.createBook(bookRequest);

        Book book = bookService.getBookById(bookId);

        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }

    // Update：更新特定書籍
    @PutMapping("books/{bookId}")
    public ResponseEntity<Book> updateBook(@PathVariable Integer bookId,
                                           @RequestBody @Valid BookRequest bookRequest) {

        // 檢查 book 是否存在
        Book book = bookService.getBookById(bookId);

        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        bookService.updateBook(bookId, bookRequest);

        Book updatedBook = bookService.getBookById(bookId);

        return ResponseEntity.status(HttpStatus.OK).body(updatedBook);
    }

    // Delete：刪除特定書籍
    @DeleteMapping("/books/{bookId}")
    public ResponseEntity<?> deleteBook(@PathVariable Integer bookId) {
        bookService.deleteBookById(bookId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
