<template>
  <div class="pagination-container">
    <!-- 上一頁 -->
    <router-link
      :to="getPageLink(currentPage - 1)"
      class="page-link"
      :class="{ disabled: currentPage === 1 }"
    >
      上一頁
    </router-link>

    <!-- 頁碼 + 省略號 -->
    <template v-for="(page, index) in visiblePages">
      <!-- 省略號可點擊 -->
      <span
        v-if="page === 'left-ellipsis'"
        :key="'left-' + index"
        class="ellipsis clickable"
        @click="jumpLeft"
      >
        ...
      </span>
      <span
        v-else-if="page === 'right-ellipsis'"
        :key="'right-' + index"
        class="ellipsis clickable"
        @click="jumpRight"
      >
        ...
      </span>
      <!-- 頁碼 -->
      <router-link
        v-else
        :key="'page-' + page"
        :to="getPageLink(page)"
        class="page-link"
        :class="{ active: page === currentPage }"
      >
        {{ page }}
      </router-link>
    </template>

    <!-- 下一頁 -->
    <router-link
      :to="getPageLink(currentPage + 1)"
      class="page-link"
      :class="{ disabled: currentPage === totalPages }"
    >
      下一頁
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
      const maxVisible = 7;
      const pages = [];

      if (total <= maxVisible) {
        for (let i = 1; i <= total; i++) pages.push(i);
      } else {
        const left = Math.max(2, current - 2);
        const right = Math.min(total - 1, current + 2);

        pages.push(1);

        if (left > 2) {
          pages.push("left-ellipsis");
        }

        for (let i = left; i <= right; i++) {
          pages.push(i);
        }

        if (right < total - 1) {
          pages.push("right-ellipsis");
        }

        pages.push(total);
      }

      return pages;
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
    jumpLeft() {
      const target = Math.max(1, this.currentPage - 3);
      this.$router.push(this.getPageLink(target));
    },
    jumpRight() {
      const target = Math.min(this.totalPages, this.currentPage + 3);
      this.$router.push(this.getPageLink(target));
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

.ellipsis {
  padding: 6px 12px;
  color: #666;
  user-select: none;
}

.ellipsis.clickable {
  cursor: pointer;
}

.ellipsis.clickable:hover {
  background: #eee;
  border-radius: 4px;
}
</style>
