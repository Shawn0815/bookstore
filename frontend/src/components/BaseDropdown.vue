<template>
  <div class="filter-dropdown">
    <!-- 主按鈕：顯示 label（例如「分類」「排序」） -->
    <button class="filter-btn"  @click="goDefault">
      <i class="fas fa-caret-down"></i>
      {{ label }}
    </button>

    <!-- 下拉清單：由 props.options 動態生成 -->
    <ul>
      <li v-for="option in options" :key="option.text">
        <!-- 點擊後：用 getQuery 生成新的 URL query，導向 /books -->
        <router-link
          :to="{ name: 'books', query: getQuery(option.query) }"
          class="filter-link"
        >
          {{ option.text }}
        </router-link>
      </li>
    </ul>
  </div>
</template>

<script>
/* eslint-disable no-unused-vars */
import getQueryTemplate from "@/models/useQueryHelper";
/* eslint-enable no-unused-vars */

export default {
  name: "BaseDropdown",
  props: {
    // label：按鈕上的字（例如「分類」「排序」）
    label: { type: String, required: true },
    // options：下拉選項的清單
    // 格式：[{ text: "最新", query: { orderBy: "published_date", sort: "desc" } }]
    options: { type: Array, required: true },
  },
  computed: {
    // activeLabel() {
    //   // 遍歷 options，檢查哪個 query 跟目前路由狀態一致
    //   const current = this.options.find((opt) => {
    //     return Object.keys(opt.query).every((key) => {
    //       return this.$route.query[key] == opt.query[key];
    //     });
    //   });

    //   // 如果有找到，就顯示它的文字，否則用預設 label
    //   return current ? current.text : this.label;
    // },
  },
  methods: {
    getQuery(newQuery) {
      return getQueryTemplate(this.$route, newQuery);
    },
    goDefault() {
      // 點主按鈕 → 清掉這個 dropdown 管的 key
      const keys = this.options[0] && this.options[0].query
        ? Object.keys(this.options[0].query)
        : [];

      const clearQuery = {};
      keys.forEach((k) => (clearQuery[k] = null));

      // 額外：一律清掉 tab
      clearQuery.sortBy = null;
      clearQuery.order = null;

      this.$router.push({
        name: "books",
        query: this.getQuery(clearQuery),
      });
    },
  },
};
</script>

<style>
/* 主按鈕：共用 filter-btn 樣式 */
.filter-btn {
  position: relative;
  background-color: #b55a30;
  color: #fff;
  width: 150px;
  height: 40px;
  border: none;
  border-radius: 8px;
  box-shadow: 0 15px 25px rgba(0, 0, 0, 0.19);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  cursor: pointer;
  position: relative;         /* 讓 ::after 可以定位 */
}

.filter-btn .fa-caret-down {
  position: absolute;
  left: 8px;
  left: 15px;   /* 靠右 */
  top: 20%;      /* 往上移 */
  pointer-events: none;
}

.filter-btn:hover {
  background-color: var(--card-background-color);
  color: var(--primary-color);
}
</style>

<style scoped>
/* 下拉容器 */
.filter-dropdown {
  position: relative;
  display: inline-block;
  text-align: center;
}

/* 下拉清單 */
.filter-dropdown ul {
  display: none; /* 預設隱藏 */
  background-color: var(--secondary-background-color);
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border-radius: 8px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
  min-width: 130px;
  z-index: 1000;
}

/* hover 主按鈕 → 顯示下拉清單 */
.filter-dropdown:hover ul {
  display: block;
}

/* 下拉選項 */
.filter-link {
  display: block;
  padding: 0.25em 0.5em;
  border-radius: 8px;
  text-decoration: none;
  color: var(--primary-color);
  background-color: var(--secondary-background-color);
  margin: 4px 0;
}
.filter-link:hover {
  background: var(--primary-color);
  color: #fff;
  transition: all 200ms ease;
}
</style>
