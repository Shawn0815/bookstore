<template>
  <header class="app-header" role="banner">
    <!-- 上排：Logo / 搜尋 / 動作 -->
    <div class="topbar shell">
      <!-- 左：Logo（連回首頁 /） -->
      <router-link to="/" class="brand" aria-label="Go Home">
        <img
          src="@/assets/images/site/Bookshop_logo.png"
          alt="Book Galaxy Logo"
          class="brand-logo"
        />
      </router-link>

      <!-- 中：搜尋 -->
      <form
        class="search"
        @submit.prevent="performSearch"
        role="search"
        aria-label="Search books"
      >
        <input
          v-model="searchQuery"
          type="text"
          class="search-input"
          placeholder="Search any book"
          aria-label="Search any book"
        />
        <button type="submit" class="search-btn" aria-label="Search">
          <i class="fa fa-search"></i>
        </button>
      </form>

      <!-- 右：動作 -->
      <nav class="actions" aria-label="Header actions">
        <router-link to="/wishlist" class="icon-btn" aria-label="Wishlist">
          <i class="far fa-heart"></i>
        </router-link>

        <router-link to="/cart" class="icon-btn cart-btn" aria-label="Cart">
          <i class="fas fa-shopping-cart"></i>
          <span v-if="cartCount" class="badge">{{ cartCount }}</span>
        </router-link>

        <router-link to="/login" class="login-link" aria-label="Login">
          Login
        </router-link>
      </nav>
    </div>

    <!-- 下排：Shop（去 /books） / 分類 / 排序 -->
    <div class="bottombar shell" role="navigation" aria-label="Browse filters">
      <div class="nav-left">
        <!-- 主按鈕：帶到所有書 -->
        <router-link to="/books" class="shop-btn" aria-label="Browse all books">
          Shop
        </router-link>

        <div class="nav-item">
          <category-dropdown />
        </div>
        <div class="nav-item">
          <sort-dropdown />
        </div>
      </div>

      <div class="nav-right"><!-- 可放其他連結 --></div>
    </div>
  </header>
</template>

<script>
import CategoryDropdown from "@/components/CategoryDropdown";
import SortDropdown from "@/components/SortDropdown";

export default {
  name: "AppHeader",
  components: { CategoryDropdown, SortDropdown },
  data() {
    return { searchQuery: "" };
  },
  computed: {
    cartCount() {
      const s = this.$store && this.$store.state;
      return s && s.cart ? s.cart.numberOfItems : 0;
    },
  },
  methods: {
    getQuery(newQuery) {
      const filteredQuery = { ...this.$route.query };
      if (newQuery.search && newQuery.search.trim() !== "") {
        filteredQuery.search = newQuery.search.trim();
      } else {
        delete filteredQuery.search;
      }
      if (filteredQuery.page && filteredQuery.page !== 1)
        filteredQuery.page = 1;
      return filteredQuery;
    },
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
/* ===== 骨架 ===== */
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
  padding: 12px 0;
}
.topbar {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  column-gap: 18px;
}
.bottombar {
  display: grid;
  grid-template-columns: 1fr 1fr;
  align-items: center;
  column-gap: 12px;
  padding-top: 10px;
  padding-bottom: 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.12);
  background: transparent;
  overflow: visible;
}

/* ===== Logo（回首頁） ===== */
.brand {
  display: inline-flex;
  align-items: center;
  text-decoration: none;
  padding-left: 16px;
}
.brand-logo {
  height: 96px;
  width: auto;
  object-fit: contain;
}

/* ===== 搜尋 ===== */
.search {
  display: flex;
  align-items: center;
  width: 90%;
  max-width: 900px;
  background: #fff;
  border-radius: 999px;
  padding: 6px 8px;
  margin: 0 auto;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}
.search-input {
  flex: 1;
  border: 0;
  outline: 0;
  padding: 12px 14px;
  border-radius: 999px;
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

/* ===== 動作區 ===== */
.actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 14px;
  padding-right: 16px;
}
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
.login-link {
  color: #fff;
  font-weight: 700;
  padding: 8px 14px;
  border-radius: 999px;
  text-decoration: none;
  border: 1px solid rgba(255, 255, 255, 0.25);
  transition: background-color 0.2s ease, border-color 0.2s ease;
}
.login-link:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.45);
}

/* ===== 下排導覽 ===== */
.nav-left,
.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
  overflow: visible;
}

/* Shop 按鈕：跟分類/排序一致 */
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
  transition: background-color 0.2s ease, color 0.2s ease,
    border-color 0.2s ease;
}

/* hover → card 背景黃黃的 */
.shop-btn:hover {
  background: var(--secondary-background-color);
  color: var(--primary-color);
}

/* active → 再稍微壓暗一點點，增加回饋感 */
.shop-btn:active {
  filter: brightness(0.92);
}
.nav-item {
  position: relative;
  background: transparent !important;
  border: none !important;
  padding: 0 !important;
  box-shadow: none !important;
  overflow: visible !important;
}

/* 下拉選單層級修正 */
:deep(.dropdown-menu),
:deep(.menu),
:deep(.options) {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: auto;
  transform: none !important;
  z-index: 5000 !important;
  border-radius: 12px;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.18);
}
.nav-item > * {
  z-index: 1;
}

/* ===== 響應式 ===== */
@media (max-width: 1200px) {
  .brand-logo {
    height: 88px;
  }
  .search {
    max-width: 820px;
  }
}
@media (max-width: 980px) {
  .brand-logo {
    height: 80px;
  }
  .search {
    max-width: 700px;
  }
}
@media (max-width: 820px) {
  .topbar {
    grid-template-columns: auto 1fr;
    grid-template-areas: "brand actions";
    row-gap: 10px;
  }
  .brand {
    grid-area: brand;
  }
  .actions {
    grid-area: actions;
    justify-content: flex-end;
    padding-right: 12px;
  }
  .search {
    grid-area: unset;
    max-width: none;
    margin: 10px 12px 0 12px;
  }

  .bottombar {
    grid-template-columns: 1fr;
    row-gap: 8px;
  }
  .nav-right {
    justify-content: flex-start;
  }
  .shop-btn {
    height: 38px;
    padding: 0 16px;
  }
}
@media (max-width: 480px) {
  .brand-logo {
    height: 72px;
  }
  .search-input {
    font-size: 14px;
    padding: 10px 12px;
  }
  .icon-btn {
    height: 36px;
    width: 36px;
  }
}

/* 顏色略淺一階（可再微調） */
:root {
  --primary-color: #7e4d2e; /* 比現在淺一點 */
  --card-background-color: #f3e1be;
}

/* Header 緊湊化 */
.shell {
  padding: 8px 0 !important;
}
.brand-logo {
  height: 72px !important;
}
.search {
  padding: 4px 8px !important;
}
.search-input {
  padding: 10px 12px !important;
  font-size: 16px !important;
}
.bottombar {
  padding-top: 6px !important;
  padding-bottom: 8px !important;
}
</style>
