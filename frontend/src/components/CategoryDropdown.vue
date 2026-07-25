<template>
  <div class="category-dropdown">
    <button class="button categories-button" style="float: left">
      <i class="fas fa-caret-down" style="float: left"></i>
      分類
    </button>
    <ul>
      <!-- 增加所有書籍下拉式選單 -->
      <li>
        <router-link
          tabindex="1"
          :to="{ name: 'books', query: getQuery({ category: null }) }"
          class="category-dropdown unselected-link"
        >
          所有書籍
        </router-link>
      </li>
      <template v-for="category in $store.state.categories">
        <!-- key 改為 category（本身就是字串） -->
        <li :key="category.category">
          <!-- 跳轉到 /books?category=category變數 -->
          <router-link
            tabindex="1"
            :to="{
              name: 'books',
              query: getQuery({ category: category.category }),
            }"
            class="category-dropdown unselected-link"
          >
            {{ category.category }}
          </router-link>
        </li>
      </template>
    </ul>
  </div>
</template>

<script>
export default {
  name: "CategoryDropdown",
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
.categories-button {
  background-color: #b55a30; /* 調整 category 按鈕顏色 */
  color: #ffffff;
  float: left;
  width: 150px;
  height: 40px;
  box-shadow: 0 15px 25px 0 rgba(0, 0, 0, 0.19);
  border-style: none;
  border-radius: 8px;
}

.categories-button:hover {
  background-color: var(--card-background-color);
  color: var(--primary-color);
}

.category-dropdown {
  grid-column: 1/2;
  grid-row: -3/-2;
  text-align: center;
}

.category-dropdown ul {
  /*drop down list*/
  display: none;
  background-color: var(--secondary-background-color);
  color: var(--primary-color);
  box-shadow: 0 8px 16px 0 rgba(0, 0, 0, 0.2);
  z-index: 1;
  position: absolute;
  border-radius: 8px;
}

.category-dropdown.unselected-link {
  display: block;
  padding: 0.25em 0.25em;
  border-radius: 8px;
  color: var(--primary-color);
  background-color: var(--secondary-background-color);
  text-decoration: none;
  margin-top: 1px;
  margin-bottom: 5px; /* 調整 category 下拉式選單的上下間距 */
}

.category-dropdown.unselected-link:hover,
.category-dropdown.unselected-link:active {
  display: block;
  height: 35px;
  font-size: large;
  text-align: center;
  transition: all 250ms;
  color: var(--secondary-background-color);
  background-color: var(--primary-color);
  text-decoration: none;
}

.category-dropdown:hover ul {
  /*different when hover*/
  display: block;
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  min-width: 130px;
  z-index: 1;
  cursor: pointer;
}
</style>
