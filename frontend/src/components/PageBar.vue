<template>
  <div class="pagination-container">
    <!-- 回第一頁 -->
    <router-link
      :to="getPageLink(1)"
      class="page-link"
      :class="{ disabled: currentPage === 1 }"
    >
      第一頁
    </router-link>

    <!-- 頁碼：固定 5 個，跟著目前頁碼滑動 -->
    <router-link
      v-for="page in visiblePages"
      :key="'page-' + page"
      :to="getPageLink(page)"
      class="page-link"
      :class="{ active: page === currentPage }"
    >
      {{ page }}
    </router-link>

    <!-- 回最後一頁 -->
    <router-link
      :to="getPageLink(totalPages)"
      class="page-link"
      :class="{ disabled: currentPage === totalPages }"
    >
      最後一頁
    </router-link>
  </div>
</template>

<script>
import { mapState } from "vuex";

export default {
  name: "PageBar",
  computed: {
    ...mapState(["currentPage", "totalPages"]),

    visiblePages() {
      const total = this.totalPages;
      const current = this.currentPage;
      const windowSize = Math.min(5, total); // 固定顯示 5 個頁碼，跟著目前頁碼滑動

      let start = current - 2;
      start = Math.max(1, start);
      start = Math.min(start, total - windowSize + 1);

      return Array.from({ length: windowSize }, (_, i) => start + i);
    },
  },
  methods: {
    getPageLink(pageNumber) {
      if (pageNumber < 1) pageNumber = 1;
      if (pageNumber > this.totalPages) pageNumber = this.totalPages;

      return {
        path: this.$route.path,
        query: {
          ...this.$route.query,
          page: pageNumber,
        },
      };
    },
  },
};
</script>

<style scoped>
.pagination-container {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 6px;
  background-color: var(--secondary-background-color);
  padding: 6px 8px;
  border-radius: 8px;
}

.page-link {
  padding: 6px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  text-decoration: none;
  color: #333;
  min-width: 36px;
  text-align: center;
  background-color: white;
  transition: background-color 0.2s, color 0.2s;
}

.page-link.active {
  background-color: #8b4513;
  color: white;
  font-weight: bold;
}

.page-link.disabled {
  pointer-events: none;
  opacity: 0.5;
}
</style>
