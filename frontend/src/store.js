import Vue from "vue";
import Vuex from "vuex";
import ApiService from "./services/ApiService";
import GuestCart from "./models/GuestCart";

Vue.use(Vuex);

export const CART_STORAGE_KEY = "cart";
export const CURRENT_ORDER_STORAGE_KEY = "currentOrder";
export const USER_STORAGE_KEY = "user";
export const CHECKOUT_FORM_KEY = "checkoutForm";
// 【新增】統一管理 Token 的 Key
export const TOKEN_STORAGE_KEY = "token"; 

export default new Vuex.Store({
  state: {
    allBooks: [],
    books: [],
    categories: [],
    selectedCategoryName: "",
    currentPage: 1,
    totalPages: 1,
    cart: JSON.parse(localStorage.getItem(CART_STORAGE_KEY)) || { cartItemList: [], total: 0, numberOfItems: 0 },
    user: JSON.parse(localStorage.getItem(USER_STORAGE_KEY)) || null,
    token: localStorage.getItem(TOKEN_STORAGE_KEY) || null, 
    myOrders: [],
    myOrdersPage: 1,
    myOrdersHasMore: true,
    currentOrder: JSON.parse(sessionStorage.getItem(CURRENT_ORDER_STORAGE_KEY)) || null,
    checkoutForm: JSON.parse(sessionStorage.getItem(CHECKOUT_FORM_KEY)) || null
  },

  mutations: {
    /* 商品功能 */
    SET_ALL_BOOKS(state, books) {
      state.allBooks = books;
    },
    SET_BOOKS(state, books) {
      state.books = books;
    },
    SET_CATEGORIES(state, newCategories) {
      state.categories = newCategories;
    },
    SELECT_CATEGORY(state, categoryName) {
      state.selectedCategoryName = categoryName;
    },
    SET_CURRENT_PAGE(state, page) {
      state.currentPage = page;
    },
    SET_TOTAL_PAGES(state, total) {
      state.totalPages = total;
    },

    /* 帳號功能 */
    SET_USER(state, user) {
      state.user = user;
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
    },
    SET_TOKEN(state, token) {
      state.token = token;
      if (token) {
        localStorage.setItem(TOKEN_STORAGE_KEY, token); 
      } else {
        localStorage.removeItem(TOKEN_STORAGE_KEY); 
      }
    },
    LOGOUT(state) {
      // 清除 Vuex State
      state.user = null;
      state.token = null;
      state.myOrders = [];
      state.myOrdersPage = 1;
      state.myOrdersHasMore = true;
      state.currentOrder = null;
      state.cart = { items: [], total: 0 };
      localStorage.clear();
      sessionStorage.clear();
    },

    /* 購物車功能 */
    SET_CART(state, shoppingCartResponse) {
      state.cart = {
        cartItemList: shoppingCartResponse.cartItemList || [],
        total: shoppingCartResponse.total || 0,
        numberOfItems: shoppingCartResponse.numberOfItems || 0
      };
      localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(state.cart));
    },
    CLEAR_CART(state) {
      state.cart = [];
      localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(state.cart));
    },

    /* 訂單功能 */
    SET_MY_ORDERS(state, orders) {
      state.myOrders = orders;
      state.myOrdersPage = 1;
      state.myOrdersHasMore = true;
    },
    CLEAR_MY_ORDERS(state) {
      state.myOrders = [];
      state.myOrdersPage = 1;
      state.myOrdersHasMore = true;
    },
    SET_CURRENT_ORDER(state, order) {
      state.currentOrder = order;
    },
    CLEAR_CURRENT_ORDER(state) {
      state.currentOrder = null;
    },
    APPEND_MY_ORDERS(state, orders) {
      const existingIds = new Set(state.myOrders.map(o => o.orderId));
      orders.forEach(order => {
        if (!existingIds.has(order.orderId)) {
          state.myOrders.push(order);
        }
      });
    },
    INCREMENT_MY_ORDERS_PAGE(state) {
      state.myOrdersPage += 1;
    },
    SET_MY_ORDERS_HAS_MORE(state, hasMore) {
      state.myOrdersHasMore = hasMore;
    },

    SET_CHECKOUT_FORM(state, form) {
      state.checkoutForm = form;
      sessionStorage.setItem(CHECKOUT_FORM_KEY, JSON.stringify(form));
    },
    CLEAR_CHECKOUT_FORM(state) {
      state.checkoutForm = null;
      sessionStorage.removeItem(CHECKOUT_FORM_KEY);
    }
  },

  actions: {
    /* 商品功能 */
    async fetchAllBooks(context) {
      return ApiService.fetchAllBooks()
        .then((result) => {
          context.commit("SET_ALL_BOOKS", result.items);
          context.commit("SET_BOOKS", result.items);
        })
        .catch((reason) => { throw reason; });
    },

    async fetchBooksByFilter(context, filters = {}) {
      return ApiService.fetchBooksByFilter(filters)
        .then((result) => {
          context.commit("SET_BOOKS", result.items);
          context.commit("SET_TOTAL_PAGES", Math.ceil(result.total / result.limit));
          context.commit("SET_CURRENT_PAGE", parseInt(result.page) || 1);
        })
        .catch((reason) => { throw reason; });
    },

    async fetchCategories(context) {
      return ApiService.fetchCategories()
        .then((categories) => {
          context.commit("SET_CATEGORIES", categories);
        })
        .catch((reason) => { throw reason; });
    },

    async selectCategory(context, categoryName) {
      context.commit("SELECT_CATEGORY", categoryName);
    },

    /* 帳號功能 */
    async register({ commit }, { name, email, password }) {
      return ApiService.register({ name, email, password })
        .then((data) => {
          commit("SET_TOKEN", data.token);
          commit("SET_USER", data.user);
        })
        .catch((error) => { 
          alert(error.message);
          throw error; });
    },
    
    async login({ commit, dispatch }, { email, password }) {
      try {
        const data = await ApiService.login({ email, password });
        commit("SET_TOKEN", data.token);
        commit("SET_USER", data.user);

        await dispatch("mergeCart"); // 合併 guestCart
        await dispatch("fetchCart"); // 再抓一次 server 購物車
      } catch (error) {
        alert(error.message);
        throw error; 
      }
    },

    async logout({ commit }) {
      commit("LOGOUT");
    },

    /* 購物車功能 */
    async fetchCart({ commit, state }) {
      if (!state.user) {
        commit("SET_CART", await GuestCart.getGuestCart());
        return;
      }
      try {
        const data = await ApiService.getCart();
        commit("SET_CART", data);
      } catch (err) {
        console.error("取得購物車失敗", err.message);
      }
    },

    async addToCart({ commit, state }, book) {
      if (!state.user) {
        const cart = await GuestCart.addToGuestCart(book, 1);
        commit("SET_CART", cart);
        return;
      }

      const existingItem = state.cart.cartItemList.find(i => i.bookId === book.bookId);
      const newQuantity = existingItem ? existingItem.quantity + 1 : 1;

      const data = await ApiService.updateCart({ bookId: book.bookId, quantity: newQuantity });
      commit("SET_CART", data);
    },

    async updateCart({ commit, state }, cartData) {
      if (!state.user) {
        const cart = await GuestCart.updateGuestCart(cartData.bookId, cartData.quantity);
        commit("SET_CART", cart);
        return;
      }
      const data = await ApiService.updateCart(cartData);
      commit("SET_CART", data);
    },

    async deleteCartItem({ commit, state }, bookId) {
      if (!state.user) {
        const cart = await GuestCart.deleteGuestCartItem(bookId);
        commit("SET_CART", cart);
        return;
      }
      const data = await ApiService.deleteCartItem(bookId);
      commit("SET_CART", data);
    },

    async clearCart({ commit, state, dispatch }) {
      if (!state.user) {
        await GuestCart.clearGuestCart();
        commit("SET_CART", { cartItemList: [], total: 0, numberOfItems: 0 });
        return;
      }
      await ApiService.clearCart();
      await dispatch("fetchCart");
    },

    async mergeCart({ commit }) {
      const localCart = JSON.parse(localStorage.getItem("cart")) || { cartItemList: [] };
      console.log("localCart: ", localCart)

      if (!localCart.cartItemList.length) {
        console.log("購物車沒東西")
        return;
      }

      try {
        const data = await ApiService.mergeCart(localCart);
        commit("SET_CART", data);
        console.log("data: ", data)
      } catch (err) {
        console.error("合併 cart 失敗:", err.message);
      }
    },

    /* 暫存 checkout 表格 */
    async saveCheckoutForm({ commit }, form) {
      commit("SET_CHECKOUT_FORM", form);
    },
    async clearCheckoutForm({ commit }) {
      commit("CLEAR_CHECKOUT_FORM");
    },
    

    /* 訂單功能 */
    async placeOrder({ commit, state }) {
      const userId = state.user && state.user.userId;
      if (!userId) return Promise.reject(new Error("User not logged in"));

      const orderData = {
        buyItemList: state.cart.cartItemList.map(item => ({
          bookId: item.bookId,
          quantity: item.quantity
        }))
      };

      return ApiService.placeOrder(orderData)
        .then(order => {
          commit("CLEAR_CART");
          commit("SET_CURRENT_ORDER", order);
        })
        .catch(err => {
          console.error(err);
          alert(err.message);
          throw err;
        });
    },

    async fetchMyOrders({ commit }, page = 1) {
      return ApiService.fetchMyOrders(page)
        .then(pages => {
          if (page === 1) commit("SET_MY_ORDERS", pages.items || []);
          else commit("APPEND_MY_ORDERS", pages.items || []);

          if (!pages.items || pages.items.length === 0) {
            commit("SET_MY_ORDERS_HAS_MORE", false);
          } else {
            commit("INCREMENT_MY_ORDERS_PAGE");
          }
        })
        .catch(err => {
          console.error(err);
          alert(err.message);
          commit("SET_MY_ORDERS_HAS_MORE", false);
        });
    },

    async fetchCurrentOrder({ commit, dispatch, state }) {
      return dispatch("fetchMyOrders", 1).then(() => {
        const firstOrder = state.myOrders[0];
        console.log("current order: ", firstOrder)
        if (firstOrder) commit("SET_CURRENT_ORDER", firstOrder);
      });
    }
  }
});