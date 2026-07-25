<template>
  <header class="app-header" role="banner">
    <!-- 上排：Logo / 搜尋 / 動作 -->
    <div class="topbar shell">
      <!-- 左：Logo -->
      <router-link to="/" class="brand" aria-label="Go Home">
        <img
          src="@/assets/images/site/Bookshop_logo.png"
          alt="Book Galaxy Logo"
          class="brand-logo"
        />
      </router-link>

      <!-- 中：搜尋框 -->
      <form class="search" @submit.prevent="performSearch">
        <input
          v-model="searchQuery"
          type="text"
          class="search-input"
          placeholder="Search any book"
        />
        <button type="submit" class="search-btn">
          <i class="fa fa-search"></i>
        </button>
      </form>

      <!-- 右：心願單 / 購物車 / 登入 -->
      <nav class="actions">
        <router-link to="/wishlist" class="icon-btn">
          <i class="far fa-heart"></i>
        </router-link>

        <router-link to="/cart" class="icon-btn cart-btn">
          <i class="fas fa-shopping-cart"></i>
          <span v-if="cartCount" class="badge">{{ cartCount }}</span>
        </router-link>

        <!-- 登入後顯示名字 + 下拉選單 -->
        <div v-if="isLoggedIn" class="login-dropdown">
          <router-link to="/" class="login-link"> Hi, {{ userName }} </router-link>
          <div class="dropdown-content">
            <router-link to="/profile" class="dropdown-item">個人檔案</router-link>
            <router-link :to="{ name: 'myorder', params: { type: 'history' } }" class="dropdown-item">我的訂單</router-link>
            <a @click.prevent="logout" class="dropdown-item">登出</a>
          </div>
        </div>

        <!-- 未登入 -->
        <router-link v-else to="/login" class="login-link"> 登入 </router-link>
      </nav>
    </div>

    <!-- 下排：Shop / 分類 / 排序 / 暢銷榜 / 新書 -->
    <div class="bottombar shell">
      <div class="nav-left">
        <!-- Shop 固定按鈕（跳到所有書） -->
        <router-link to="/books" class="shop-btn"> 書籍首頁 </router-link>

        <!-- 使用 BaseDropdown（分類 / 出版社等） -->
        <BaseDropdown label="分類" :options="categories" />
      </div>
      <div class="nav-right">
        <!-- 使用 BaseButton（最新 / 最暢銷） -->
        <BaseButton :option="{ text: '最新', query: { sortBy: 'published_date' }, toggle: true }" />
        <BaseButton :option="{ text: '最暢銷', query: { sortBy: 'sales_count' }, toggle: true }"/>
        <BaseButton :option="{ text: '價格 ↑↓', query: { sortBy: 'price' }, toggle: true }"/>
      </div>
    </div>
  </header>
</template>

<script>
import BaseDropdown from "@/components/BaseDropdown";
import BaseButton from "@/components/BaseButton";

/* eslint-disable no-unused-vars */
import getQueryTemplate from "@/models/useQueryHelper";
/* eslint-enable no-unused-vars */

export default {
  name: "AppHeader",
  components: { BaseDropdown, BaseButton },
  data() {
    return {
      searchQuery: "",
    };
  },
  computed: {
    isLoggedIn() {
      return !!this.$store.state.user; // 判斷是否登入
    },

    userName() {
      const user = this.$store.state.user;
      return (user && user.userName) || "User";
    },

    // 從 Vuex 取購物車數量
    cartCount() {
      const s = this.$store && this.$store.state;
      return s && s.cart ? s.cart.numberOfItems : 0;
    },
    // 分類選項
    categories() {
      return [
        { text: "所有書籍", query: { category: null } },
        ...this.$store.state.categories.map((c) => ({
          text: c.category,
          query: { category: c.category },
        })),
      ];
    },
  },
  methods: {
    toggleUserMenu() {
      this.showUserMenu = !this.showUserMenu;
    },
    logout() {
      this.$store.dispatch("logout");
      
      if (this.$route.path !== "/") {
        this.$router.push("/");
      }
    },
    getQuery(newQuery) {
      return getQueryTemplate(this.$route, newQuery);
    },
    // 搜尋功能：跳轉到 /books?search=xxx
    performSearch() {
      this.$router.push({
        name: "books",
        query: this.getQuery({ search: this.searchQuery }),
      });
    },
  },
};
</script>

<style scoped>
/* ===== Header 框架 ===== */
.app-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: var(--primary-color);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.12);
}
.shell {
  max-width: 1280px;
  margin: 0 auto;
  padding: 8px 0;
}
.topbar {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  column-gap: 18px;
}
.bottombar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.12);
  padding-top: 6px;
  padding-bottom: 8px;
  font-size: 1.05em; /* 跟側欄 CategoryNav 的按鈕字級對齊 */
}

/* ===== Logo（回首頁） ===== */
.brand {
  display: inline-flex;
  align-items: center;
  text-decoration: none;
  padding-left: 16px;
}
.brand-logo {
  height: 72px;
  width: auto;
  object-fit: contain;
}

/* ===== 搜尋 ===== */
.search {
  display: flex;
  align-items: center;
  width: 90%;
  max-width: 800px;
  background: #fff;
  border-radius: 999px;
  padding: 4px 8px;
  margin: 0 auto;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}
.search-input {
  flex: 1;
  border: 0;
  outline: 0;
  padding: 10px 12px;
  border-radius: 999px;
  font-size: 20px;
}
.search-input::placeholder {
  font-size: 20px;
}
.search-btn {
  border: 0;
  outline: 0;
  background: transparent;
  height: 40px;
  width: 44px;
  border-radius: 999px;
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: transform 0.06s ease-in-out;
}
.search-btn:active {
  transform: scale(0.96);
}
.search-btn i {
  color: var(--secondary-color, #9a6f4e);
}

/* ===== 右側按紐 ===== */
.actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 14px;
  padding-right: 16px;
}

/* ===== Icon 按紐 ===== */
.icon-btn {
  position: relative;
  display: inline-grid;
  place-items: center;
  height: 40px;
  width: 40px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  text-decoration: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.06s ease-in-out, box-shadow 0.2s ease;
}
.icon-btn:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}
.icon-btn i {
  font-size: 18px;
}

/* ===== Cart 按紐 ===== */
.cart-btn .badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background: #e53935;
  color: #fff;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* ===== Login 按紐 ===== */
.login-link {
  color: #fff;
  font-weight: 700;
  padding: 8px 14px;
  border-radius: 999px;
  text-decoration: none;
  border: 1px solid rgba(255, 255, 255, 0.25);
  background: transparent; /* 這裡保持透明 */
  transition: background-color 0.2s ease, border-color 0.2s ease;
}
.login-link:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.45);
}

/* ===== 下排導覽列 ===== */
.nav-left,
.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
  overflow: visible;
}

/* ===== Shop 按鈕 ===== */
.shop-btn {
  color: var(--primary-color);
  background: #fff;
  border: 2px solid var(--primary-color);
  font-weight: 700;
  padding: 0 18px;
  border-radius: 12px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.shop-btn:hover {
  background: var(--secondary-background-color);
}

/* ===== 登入下拉清單 ===== */
.login-dropdown {
  position: relative;
  display: inline-block;
}

.login-link {
  color: #fff;
  font-weight: 700;
  padding: 8px 14px;
  border-radius: 999px;
  text-decoration: none;
  border: 1px solid rgba(255, 255, 255, 0.25);
  background: transparent;
  transition: background-color 0.2s ease, border-color 0.2s ease;
  cursor: pointer;
}
.login-link:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.45);
}

.dropdown-content {
  display: none;
  position: absolute;
  top: 110%; /* 不會蓋到名字 */
  left: 50%;
  margin-top: 8px;
  transform: translateX(-50%);
  background: var(--secondary-background-color, #fff);
  border-radius: 8px;
  min-width: 120px;
  box-shadow: 0 8px 16px rgba(0,0,0,0.2);
  z-index: 1000;
  text-align: left;
}

.login-dropdown:hover .dropdown-content {
  display: block;
}

.dropdown-item {
  display: block;
  padding: 8px 12px;
  color: var(--primary-color, #333);
  text-decoration: none;
  border-radius: 6px;
  transition: background 0.2s ease;
  cursor: pointer; /* <-- 加這行 */
}

.dropdown-item:hover {
  background: rgba(181, 90, 48, 0.15);
}
</style>
