import axios from "axios";

// 取得後端 base url
const API_BASE_URL = process.env.VUE_APP_API_BASE_URL;

const instance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json"
  }
});

// 攔截請求：加 token，並印出 log
instance.interceptors.request.use(config => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  // 印出完整 URL 與 payload
  const fullUrl = new URL(config.url, config.baseURL);

  if (config.params) {
    Object.entries(config.params).forEach(([key, value]) => {
      fullUrl.searchParams.append(key, value);
    });
  }

  if (config.data) {
    console.log(`[Request] ${config.method.toUpperCase()} ${fullUrl.toString()}`, config.data);
  } else {
    console.log(`[Request] ${config.method.toUpperCase()} ${fullUrl.toString()}`);
  }

  return config;
}, error => {
  console.error("[Request Error]", error);
  return Promise.reject(error);
});

// 攔截回應：處理錯誤、自動登出
instance.interceptors.response.use(
  (res) => {
    const fullUrl = new URL(res.config.url, res.config.baseURL);
    console.log(`[Response] ${res.config.method.toUpperCase()} ${fullUrl.toString()}`, res.data);
    return res.data; // 保持只回傳 data，store.js 不用改
  },
  (error) => {
    if (error.response) {
      const status = error.response.status;
      const errData = error.response.data;

      let errMsg = ""
      if (errData.message) {
        errMsg = errData.message;
      }
      else {
        errMsg = errData.status + "    " + errData.error;
      }

      console.error(`[Response Error ${status}]`, errMsg);

      // 如果沒有 silent 才 alert
      if (!error.config.silent) {
        alert(errMsg);
      }

      // Token 過期 → 登出
      if (status === 401) {
        console.warn("Token expired or invalid. Logging out...");
        localStorage.clear();
        sessionStorage.clear();
        window.location.href = "/login";
      }

      return Promise.reject(new Error(errMsg));
    }

    return Promise.reject(error);
  }
);

// API 封裝
export default {
  // 商品功能
  fetchAllBooks() {
    return instance.get("/books");
  },

  fetchBook(bookId) {
    return instance.get(`/books/${bookId}`);
  },

  fetchBooksByFilter(filters) {
    return instance.get("/books", { params: filters });
  },

  fetchCategories() {
    return instance.get("/categories");
  },

  // 註冊 / 登入
  register(userInfo) {
    return instance.post("/users/register", userInfo);
  },

  login(credentials) {
    return instance.post("/users/login", credentials);
  },

  // 訂單功能
  placeOrder(orderData) {
    return instance.post("/users/orders", orderData);
  },

  fetchMyOrders(page = 1) {
    return instance.get("/users/orders", { params: { page } });
  },

  // 購物車功能
  getCart() {
    return instance.get("/users/cart");
  },

  updateCart(cartRequest) {
    return instance.put("/users/cart/items", cartRequest);
  },

  mergeCart(guestCart) {
    return instance.post("/users/cart/merge", guestCart);
  },

  deleteCartItem(bookId) {
    return instance.delete(`/users/cart/items/${bookId}`);
  },

  clearCart() {
    return instance.delete("/users/cart");
  },
  
  checkToken() {
    const token = localStorage.getItem("token");
    return axios.get(`${API_BASE_URL}/users/token/check`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
  }
};
