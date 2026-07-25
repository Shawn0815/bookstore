<template>
  <!-- 一般按鈕 -->
  <router-link
    v-if="!option.toggle"
    :to="{ name: 'books', query: getQuery(option.query) }"
    class="filter-btn tab-btn single-btn"
    :class="{ active: isActive }"
  >
    {{ option.text }}
  </router-link>

  <!-- Toggle 按鈕 -->
  <button
    v-else
    class="filter-btn tab-btn single-btn"
    :class="{ active: isActive }"
    @click="toggleOrder"
  >
    {{ displayText }}
  </button>
</template>

<script>
/* eslint-disable no-unused-vars */
import getQueryTemplate from "@/models/useQueryHelper";
/* eslint-enable no-unused-vars */

export default {
  name: "BaseButton",
  props: {
    option: { type: Object, required: true },
  },
  computed: {
    isActive() {
      const sortBy = this.$route.query.sortBy;

      // 如果在 /books 且 query 沒東西，"最新" 按鈕顯示高亮
      if (this.$route.name === "books" && 
          this.$route.query.sortBy === undefined && 
          this.option.query.sortBy === 'published_date') {
        return true;
      }

      // 否則就根據 query 判斷
      return sortBy === this.option.query.sortBy;
    },
    displayText() {
      const isBooksPage = this.$route.name === "books";
      const sortBy = this.$route.query.sortBy;
      const order = this.$route.query.order;


      // 情境 1：不是 active → 顯示預設文字
      if (!this.isActive) {
        return this.option.text;
      }

      // 情境 2：在 /books 且沒有任何 sortBy → 顯示預設文字
      if (isBooksPage && sortBy === undefined) {
        return this.option.text;
      }

      // 情境 3：active 的 tab，依照 sortBy + order 決定文字
      if (sortBy === "published_date") {
        return order === "asc" ? "最舊" : "最新";
      }
      if (sortBy === "sales_count") {
        return order === "asc" ? "最不暢銷" : "最暢銷";
      }
      if (sortBy === "price") {
        return order === "asc" ? "價格 ▲" : "價格 ▼";
      }

      // fallback（理論上不會走到）
      return this.option.text;
    },
  },
  methods: {
    getQuery(newQuery) {
      return getQueryTemplate(this.$route, newQuery);
    },
    toggleOrder() {
      const sortByBefore = this.$route.query.sortBy; // 改變前的 sortBy
      const sortBy = this.option.query.sortBy; // 要改變成的 sortBy
      
      const defaultOrder = sortBy === "price" ? "asc" : "desc"; // 初始排序
      const currentOrder = this.$route.query.order;

      // 第一次按這個按紐
      if (sortBy != sortByBefore) {
        this.$router.push({
          name: "books",
          query: this.getQuery({ sortBy: sortBy, order: defaultOrder }),
        });
      } else {
        const next = currentOrder === "desc" ? "asc" : "desc";
        this.$router.push({
          name: "books",
          query: this.getQuery({ sortBy: sortBy, order: next }),
        });
      }
    },
  },
};
</script>


<style scoped>
.single-btn {
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  padding: 6px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

/* ===== Tab 按鈕 ===== */
.tab-btn {
  background: rgba(255, 255, 255, 0.15); /* 半透明白 */
  color: #fff; 
  border: none;
}

/* Hover 效果 */
.tab-btn:hover {
  background: var(--card-background-color);
  color: var(--primary-color);
}

/* Active 狀態 */
.tab-btn.active {
  background: #fff;
  color: var(--primary-color);
  border: none;
  font-weight: 700;
}
</style>
