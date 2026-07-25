package com.shawnyu.springbootmall.service.impl;

import com.shawnyu.springbootmall.constant.CartStatus;
import com.shawnyu.springbootmall.dao.BookDao;
import com.shawnyu.springbootmall.dao.CartDao;
import com.shawnyu.springbootmall.dao.UserDao;
import com.shawnyu.springbootmall.dto.CartBatchRequest;
import com.shawnyu.springbootmall.dto.CartItemDTO;
import com.shawnyu.springbootmall.dto.CartRequest;
import com.shawnyu.springbootmall.dto.CartResponse;
import com.shawnyu.springbootmall.model.Book;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    private static final Integer USER_ID = 1;
    private static final Integer BOOK_ID = 100;

    @Mock
    private CartDao cartDao;

    @Mock
    private BookDao bookDao;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private CartServiceImpl cartService;

    private CartItemDTO cartItem(String title, Integer stock, Integer quantity, Integer price) {
        CartItemDTO item = new CartItemDTO();
        item.setBookId(BOOK_ID);
        item.setTitle(title);
        item.setStock(stock);
        item.setQuantity(quantity);
        item.setPrice(price);
        return item;
    }

    @Test
    void getCart_bookDeleted_isDiscontinuedWithZeroAmount() {
        when(cartDao.getCart(USER_ID)).thenReturn(List.of(cartItem(null, 10, 2, 100)));

        CartResponse response = cartService.getCart(USER_ID);
        CartItemDTO result = response.getCartItemList().get(0);

        assertEquals(CartStatus.DISCONTINUED, result.getStatus());
        assertEquals("商品已下架", result.getMessage());
        assertEquals(0, result.getAmount());
        assertEquals(2, response.getNumberOfItems()); // 件數仍然計入
        assertEquals(0, response.getTotal());
    }

    @Test
    void getCart_zeroStock_isOutOfStockWithZeroAmount() {
        when(cartDao.getCart(USER_ID)).thenReturn(List.of(cartItem("書", 0, 2, 100)));

        CartResponse response = cartService.getCart(USER_ID);
        CartItemDTO result = response.getCartItemList().get(0);

        assertEquals(CartStatus.OUT_OF_STOCK, result.getStatus());
        assertEquals("商品目前缺貨", result.getMessage());
        assertEquals(0, result.getAmount());
    }

    @Test
    void getCart_quantityExceedsStock_isOutOfStockWithZeroAmount() {
        when(cartDao.getCart(USER_ID)).thenReturn(List.of(cartItem("書", 3, 5, 100)));

        CartResponse response = cartService.getCart(USER_ID);
        CartItemDTO result = response.getCartItemList().get(0);

        assertEquals(CartStatus.OUT_OF_STOCK, result.getStatus());
        assertEquals("庫存不足，僅剩 3 本", result.getMessage());
        assertEquals(0, result.getAmount());
    }

    @Test
    void getCart_quantityEqualsStock_reachedLimitCountsAmount() {
        when(cartDao.getCart(USER_ID)).thenReturn(List.of(cartItem("書", 3, 3, 100)));

        CartResponse response = cartService.getCart(USER_ID);
        CartItemDTO result = response.getCartItemList().get(0);

        assertEquals(CartStatus.REACHED_LIMIT, result.getStatus());
        assertEquals(300, result.getAmount());
        assertEquals(300, response.getTotal());
    }

    @Test
    void getCart_available_countsAmount() {
        when(cartDao.getCart(USER_ID)).thenReturn(List.of(cartItem("書", 10, 2, 100)));

        CartResponse response = cartService.getCart(USER_ID);
        CartItemDTO result = response.getCartItemList().get(0);

        assertEquals(CartStatus.AVAILABLE, result.getStatus());
        assertEquals(200, result.getAmount());
        assertEquals(200, response.getTotal());
    }

    @Test
    void getCart_totalOnlyCountsAvailableAndReachedLimit() {
        CartItemDTO discontinued = cartItem(null, 10, 1, 100);
        CartItemDTO outOfStock = cartItem("A", 0, 1, 100);
        CartItemDTO available = cartItem("B", 10, 1, 100);
        when(cartDao.getCart(USER_ID)).thenReturn(List.of(discontinued, outOfStock, available));

        CartResponse response = cartService.getCart(USER_ID);

        assertEquals(3, response.getNumberOfItems());
        assertEquals(100, response.getTotal()); // 只有 available 那筆算進總額
    }

    @Test
    void updateCart_bookNotFound_deletesItemAndThrows404() {
        CartRequest request = new CartRequest();
        request.setBookId(BOOK_ID);
        request.setQuantity(2);
        when(bookDao.getBookById(BOOK_ID)).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cartService.updateCart(USER_ID, request));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(cartDao).deleteCartItem(USER_ID, BOOK_ID);
        verify(cartDao, never()).upsertCartItem(any(), any());
    }

    @Test
    void updateCart_zeroStock_deletesItemAndThrows400() {
        CartRequest request = new CartRequest();
        request.setBookId(BOOK_ID);
        request.setQuantity(2);
        Book book = new Book();
        book.setBookId(BOOK_ID);
        book.setStock(0);
        when(bookDao.getBookById(BOOK_ID)).thenReturn(book);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cartService.updateCart(USER_ID, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(cartDao).deleteCartItem(USER_ID, BOOK_ID);
        verify(cartDao, never()).upsertCartItem(any(), any());
    }

    @Test
    void updateCart_stockLessThanRequested_clampsQuantityAndUpserts() {
        CartRequest request = new CartRequest();
        request.setBookId(BOOK_ID);
        request.setQuantity(10);
        Book book = new Book();
        book.setBookId(BOOK_ID);
        book.setStock(3);
        when(bookDao.getBookById(BOOK_ID)).thenReturn(book);
        when(cartDao.getCart(USER_ID)).thenReturn(Collections.emptyList());

        cartService.updateCart(USER_ID, request);

        assertEquals(3, request.getQuantity()); // 被削減成庫存量，不拋例外
        verify(cartDao).upsertCartItem(USER_ID, request);
    }

    @Test
    void updateCart_stockEnough_upsertsAsIs() {
        CartRequest request = new CartRequest();
        request.setBookId(BOOK_ID);
        request.setQuantity(2);
        Book book = new Book();
        book.setBookId(BOOK_ID);
        book.setStock(10);
        when(bookDao.getBookById(BOOK_ID)).thenReturn(book);
        when(cartDao.getCart(USER_ID)).thenReturn(Collections.emptyList());

        cartService.updateCart(USER_ID, request);

        assertEquals(2, request.getQuantity());
        verify(cartDao).upsertCartItem(USER_ID, request);
    }

    @Test
    void mergeCart_emptyList_returnsCartWithoutWriting() {
        CartBatchRequest batchRequest = new CartBatchRequest();
        batchRequest.setCartItemList(Collections.emptyList());
        when(cartDao.getCart(USER_ID)).thenReturn(Collections.emptyList());

        cartService.mergeCart(USER_ID, batchRequest);

        verify(cartDao, never()).upsertCartItem(any(), any());
        verify(cartDao, never()).getCartItemByBookId(any(), any());
    }

    @Test
    void mergeCart_nullRequest_returnsCartWithoutWriting() {
        when(cartDao.getCart(USER_ID)).thenReturn(Collections.emptyList());

        cartService.mergeCart(USER_ID, null);

        verify(cartDao, never()).upsertCartItem(any(), any());
    }

    @Test
    void mergeCart_existingItem_addsQuantities() {
        CartRequest request = new CartRequest();
        request.setBookId(BOOK_ID);
        request.setQuantity(2);
        CartBatchRequest batchRequest = new CartBatchRequest();
        batchRequest.setCartItemList(List.of(request));

        CartItemDTO existing = new CartItemDTO();
        existing.setQuantity(3);
        when(cartDao.getCartItemByBookId(USER_ID, BOOK_ID)).thenReturn(existing);
        when(cartDao.getCart(USER_ID)).thenReturn(Collections.emptyList());

        cartService.mergeCart(USER_ID, batchRequest);

        assertEquals(5, request.getQuantity()); // 3(舊) + 2(新)
        verify(cartDao).upsertCartItem(USER_ID, request);
    }

    @Test
    void mergeCart_noExistingItem_usesRequestedQuantity() {
        CartRequest request = new CartRequest();
        request.setBookId(BOOK_ID);
        request.setQuantity(2);
        CartBatchRequest batchRequest = new CartBatchRequest();
        batchRequest.setCartItemList(List.of(request));

        when(cartDao.getCartItemByBookId(USER_ID, BOOK_ID)).thenReturn(null);
        when(cartDao.getCart(USER_ID)).thenReturn(Collections.emptyList());

        cartService.mergeCart(USER_ID, batchRequest);

        assertEquals(2, request.getQuantity());
        verify(cartDao).upsertCartItem(USER_ID, request);
    }

    @Test
    void deleteCartItem_delegatesToDao() {
        when(cartDao.getCart(USER_ID)).thenReturn(Collections.emptyList());

        cartService.deleteCartItem(USER_ID, BOOK_ID);

        verify(cartDao).deleteCartItem(USER_ID, BOOK_ID);
    }

    @Test
    void deleteCart_delegatesToDao() {
        cartService.deleteCart(USER_ID);

        verify(cartDao).deleteCart(USER_ID);
    }
}
