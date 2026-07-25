package com.shawnyu.springbootmall.service.impl;

import com.shawnyu.springbootmall.dao.BookDao;
import com.shawnyu.springbootmall.dao.OrderDao;
import com.shawnyu.springbootmall.dao.UserDao;
import com.shawnyu.springbootmall.dto.BuyItem;
import com.shawnyu.springbootmall.dto.CreateOrderRequest;
import com.shawnyu.springbootmall.dto.OrderQueryParams;
import com.shawnyu.springbootmall.model.Book;
import com.shawnyu.springbootmall.model.Order;
import com.shawnyu.springbootmall.model.OrderItem;
import com.shawnyu.springbootmall.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final Integer USER_ID = 1;

    @Mock
    private OrderDao orderDao;

    @Mock
    private BookDao bookDao;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private OrderServiceImpl orderService;

    private BuyItem buyItem(Integer bookId, Integer quantity) {
        BuyItem item = new BuyItem();
        item.setBookId(bookId);
        item.setQuantity(quantity);
        return item;
    }

    private Book book(Integer bookId, Integer price, Integer stock, Integer salesCount) {
        Book book = new Book();
        book.setBookId(bookId);
        book.setPrice(price);
        book.setStock(stock);
        book.setSalesCount(salesCount);
        return book;
    }

    @Test
    void createOrder_userNotFound_throws400AndSkipsBookLookups() {
        when(userDao.getUserbyId(USER_ID)).thenReturn(null);
        CreateOrderRequest request = new CreateOrderRequest();
        request.setBuyItemList(List.of(buyItem(100, 1)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(USER_ID, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(bookDao, never()).getBookById(any());
    }

    @Test
    void createOrder_bookNotFound_throws400() {
        when(userDao.getUserbyId(USER_ID)).thenReturn(new User());
        when(bookDao.getBookById(100)).thenReturn(null);
        CreateOrderRequest request = new CreateOrderRequest();
        request.setBuyItemList(List.of(buyItem(100, 1)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(USER_ID, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("100"));
    }

    @Test
    void createOrder_insufficientStock_throws400() {
        when(userDao.getUserbyId(USER_ID)).thenReturn(new User());
        when(bookDao.getBookById(100)).thenReturn(book(100, 200, 5, 0));
        when(bookDao.updateStock(100, 10)).thenReturn(0); // 原子扣庫存失敗

        CreateOrderRequest request = new CreateOrderRequest();
        request.setBuyItemList(List.of(buyItem(100, 10)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(USER_ID, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(bookDao, never()).updateSalesCount(any(), any());
        verify(orderDao, never()).createOrder(any(), any());
    }

    @Test
    void createOrder_success_computesTotalAndUpdatesSalesCount() {
        when(userDao.getUserbyId(USER_ID)).thenReturn(new User());
        when(bookDao.getBookById(100)).thenReturn(book(100, 200, 10, 5));
        when(bookDao.getBookById(200)).thenReturn(book(200, 300, 10, 2));
        when(bookDao.updateStock(anyInt(), anyInt())).thenReturn(1);
        when(orderDao.createOrder(eq(USER_ID), eq(1000))).thenReturn(999);
        // totalAmount = 2*200 + 2*300 = 1000

        CreateOrderRequest request = new CreateOrderRequest();
        request.setBuyItemList(List.of(buyItem(100, 2), buyItem(200, 2)));

        Integer orderId = orderService.createOrder(USER_ID, request);

        assertEquals(999, orderId);
        verify(bookDao).updateSalesCount(100, 7); // 5(原本銷量) + 2(本次購買)
        verify(bookDao).updateSalesCount(200, 4); // 2(原本銷量) + 2(本次購買)

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OrderItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(orderDao).createOrderItems(eq(999), captor.capture());
        List<OrderItem> items = captor.getValue();
        assertEquals(2, items.size());
        assertEquals(400, items.get(0).getAmount());
        assertEquals(600, items.get(1).getAmount());
    }

    @Test
    void getOrders_populatesOrderItemsForEachOrder() {
        Order order1 = new Order();
        order1.setOrderId(1);
        Order order2 = new Order();
        order2.setOrderId(2);
        when(orderDao.getOrders(any())).thenReturn(List.of(order1, order2));
        when(orderDao.getOrderItemsByOrderId(1)).thenReturn(List.of(new OrderItem()));
        when(orderDao.getOrderItemsByOrderId(2)).thenReturn(List.of());

        List<Order> result = orderService.getOrders(new OrderQueryParams());

        assertEquals(1, result.get(0).getOrderItemList().size());
        assertEquals(0, result.get(1).getOrderItemList().size());
    }

    @Test
    void getOrderById_populatesOrderItems() {
        Order order = new Order();
        order.setOrderId(1);
        when(orderDao.getOrderById(1)).thenReturn(order);
        when(orderDao.getOrderItemsByOrderId(1)).thenReturn(List.of(new OrderItem(), new OrderItem()));

        Order result = orderService.getOrderById(1);

        assertEquals(2, result.getOrderItemList().size());
    }
}
