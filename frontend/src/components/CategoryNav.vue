<template>
  <nav class="category-nav">
    <ul class="category-buttons unselected-category-button">
      <!-- 增加所有書及標籤 -->
      <li
        v-if="!$route.query.category"
        class="button category-buttons selected-category-button"
        id="button selected-category-button"
      >
        所有書籍
      </li>
      <li v-else>
        <router-link
          tabindex="1"
          :to="{ name: 'books', query: getQuery({ category: null }) }"
          class="button category-buttons unselected-category-button"
        >
          所有書籍
        </router-link>
      </li>
      <template v-for="category in $store.state.categories">
        <!-- key 為 category 本身，因為從 categories 取出的元素本身就是字串 -->
        <!-- 決定顯示 slected 設定的方式：用 query 判斷路由是否有 category 參數 -->
        <li
          :key="category.category"
          v-if="$route.query.category === category.category"
          class="button category-buttons selected-category-button"
          id="button selected-category-button"
        >
          {{ category.category }}
        </li>
        <li :key="category.category" v-else>
          <!-- 跳轉到 /books?category=category變數 -->
          <router-link
            tabindex="1"
            :to="{
              name: 'books',
              query: getQuery({ category: category.category }),
            }"
            class="button category-buttons unselected-category-button"
          >
            {{ category.category }}
          </router-link>
        </li>
      </template>
    </ul>
  </nav>
</template>

<script>
export default {
  name: "CategoryNav",
  methods: {
    // 保留其他 query 參數（search, sort），只修改 category
    getQuery(newQuery) {
      const filteredQuery = { ...this.$route.query };

      // 遍歷 newQuery，如果值是 null 就刪掉對應的 query
      Object.keys(newQuery).forEach((key) => {
        if (newQuery[key] === null) {
          delete filteredQuery[key];
        } else {
          filteredQuery[key] = newQuery[key];
        }
      });

      // 只有當 page 不是 1 時，才重置
      if (filteredQuery.page && filteredQuery.page !== 1) {
        filteredQuery.page = 1;
      }

      return filteredQuery;
    },
  },
};
</script>

<style scoped>
.category-nav {
  background-color: var(--primary-color);
  border-radius: 10px;
  border: var(--primary-color) 3px solid;
  width: auto; /* 自動寬度 */
  white-space: nowrap; /* 確保 nav 不換行 */
  align-self: flex-start;
  margin-top: 10em;
  margin-left: 1em;
}

.category-buttons {
  text-align: center;
  width: auto;
  border-radius: 10px;
}

.button.unselected-category-button,
.button.unselected-category-button:visited {
  background-color: var(--primary-color);
  color: var(--secondary-background-color);
}

.button.unselected-category-button:hover,
.button.unselected-category-button:active {
  display: block;
  background-color: var(--secondary-background-color);
  color: var(--primary-color);
  border-radius: 10px;
}
.selected-category-button,
.selected-category-button:active {
  display: block;
  background-color: #0017;
  color: var(--secondary-background-color);
  border-radius: 10px;
}
.selected-category-button:hover {
  display: block;
  background-color: var(--secondary-background-color);
  color: var(--primary-color);
  border-radius: 10px;
}
</style>
