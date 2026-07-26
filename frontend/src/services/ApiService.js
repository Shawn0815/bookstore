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

// Token 過期時用來自動 refresh 的狀態（避免多個請求同時各自打一次 refresh）
let isRefreshing = false;
let refreshSubscribers = [];

function subscribeTokenRefresh(callback) {
  refreshSubscribers.push(callback);
}

function onTokenRefreshed(newToken) {
  refreshSubscribers.forEach((callback) => callback(newToken));
  refreshSubscribers = [];
}

function forceLogout() {
  localStorage.clear();
  sessionStorage.clear();
  window.location.href = "/login";
}

// 攔截回應：處理錯誤、401 時先嘗試用 refresh token 換新 access token
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
      const originalRequest = error.config;

      let errMsg = ""
      if (errData.message) {
        errMsg = errData.message;
      }
      else {
        errMsg = errData.status + "    " + errData.error;
      }

      console.error(`[Response Error ${status}]`, errMsg);

      // Token 過期：先試著用 refresh token 換一個新的 access token，換到就重打原本那個請求
      const storedRefreshToken = localStorage.getItem("refreshToken");
      const isRefreshCall = originalRequest.url.includes("/users/token/refresh");

      if (status === 401 && storedRefreshToken && !isRefreshCall && !originalRequest._retried) {
        originalRequest._retried = true;

        if (!isRefreshing) {
          isRefreshing = true;
          return instance
            .post("/users/token/refresh", { refreshToken: storedRefreshToken }, { silent: true })
            .then((data) => {
              localStorage.setItem("token", data.token);
              localStorage.setItem("refreshToken", data.refreshToken);
              isRefreshing = false;
              onTokenRefreshed(data.token);
              originalRequest.headers.Authorization = `Bearer ${data.token}`;
              return instance(originalRequest);
            })
            .catch((refreshError) => {
              isRefreshing = false;
              console.warn("Refresh token 也失效了，強制登出", refreshError.message);
              forceLogout();
              return Promise.reject(refreshError);
            });
        }

        // 已經有另一個請求正在 refresh 了，這個請求排隊等新 token 出來再重打一次
        return new Promise((resolve) => {
          subscribeTokenRefresh((newToken) => {
            originalRequest.headers.Authorization = `Bearer ${newToken}`;
            resolve(instance(originalRequest));
          });
        });
      }

      // 如果沒有 silent 才 alert
      if (!error.config.silent) {
        alert(errMsg);
      }

      // refresh 也失敗了，或本來就沒有 refresh token 可用 → 直接登出
      if (status === 401) {
        console.warn("Token expired or invalid, and no refresh token available. Logging out...");
        forceLogout();
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

  logout(refreshToken) {
    return instance.post("/users/logout", { refreshToken }, { silent: true });
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
    return instance.get("/users/token/check", { silent: true });
  }
};
