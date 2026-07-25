<template>
  <div class="sort-dropdown">
    <button
      class="button sort-button"
      style="float: left"
      @click="goDefaultSort"
    >
      <i class="fas fa-caret-down" style="float: left"></i>
      {{ currentSortText }}
    </button>
    <ul>
      <li>
        <router-link
          tabindex="1"
          :to="{
            name: 'books',
            query: getQuery({ orderBy: 'published_date', sort: 'desc' }),
          }"
          class="sort-dropdown unselected-link"
        >
          最新（預設）
        </router-link>
      </li>
      <li>
        <router-link
          tabindex="1"
          :to="{
            name: 'books',
            query: getQuery({ orderBy: 'published_date', sort: 'asc' }),
          }"
          class="sort-dropdown unselected-link"
        >
          最舊
        </router-link>
      </li>
      <li>
        <router-link
          tabindex="1"
          :to="{
            name: 'books',
            query: getQuery({ orderBy: 'price', sort: 'asc' }),
          }"
          class="sort-dropdown unselected-link"
        >
          價格 ↑
        </router-link>
      </li>
      <li>
        <router-link
          tabindex="1"
          :to="{
            name: 'books',
            query: getQuery({ orderBy: 'price', sort: 'desc' }),
          }"
          class="sort-dropdown unselected-link"
        >
          價格 ↓
        </router-link>
      </li>
      <li>
        <router-link
          tabindex="1"
          :to="{
            name: 'books',
            query: getQuery({ orderBy: 'stock', sort: 'asc' }),
          }"
          class="sort-dropdown unselected-link"
        >
          庫存 ↑
        </router-link>
      </li>
      <li>
        <router-link
          tabindex="1"
          :to="{
            name: 'books',
            query: getQuery({ orderBy: 'stock', sort: 'desc' }),
          }"
          class="sort-dropdown unselected-link"
        >
          庫存 ↓
        </router-link>
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  name: "SortDropdown",
  computed: {
    currentSortText() {
      const { orderBy, sort } = this.$route.query;
      if (!orderBy) return "排序"; // 預設文字
      if (orderBy === "published_date" && sort === "desc")
        return "最新（預設）";
      if (orderBy === "published_date" && sort === "asc") return "最舊";
      if (orderBy === "price" && sort === "asc") return "價格 ↑";
      if (orderBy === "price" && sort === "desc") return "價格 ↓";
      if (orderBy === "stock" && sort === "asc") return "庫存 ↑";
      if (orderBy === "stock" && sort === "desc") return "庫存 ↓";
      return "排序";
    },
  },
  methods: {
    getQuery(newQuery) {
      const filteredQuery = { ...this.$route.query };

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
    goDefaultSort() {
      // 導航回預設排序
      this.$router.push({
        name: "books",
        query: this.getQuery({ orderBy: null, sort: null }),
      });
    },
  },
};
</script>

<style scoped>
.sort-button {
  background-color: #b55a30;
  color: #ffffff;
  float: left;
  width: 150px;
  height: 40px;
  box-shadow: 0 15px 25px 0 rgba(0, 0, 0, 0.19);
  border-style: none;
  border-radius: 8px;
}

.sort-button:hover {
  background-color: var(--card-background-color);
  color: var(--primary-color);
}

.sort-dropdown {
  grid-column: 1/2;
  grid-row: -3/-2;
  text-align: center;
}

.sort-dropdown ul {
  display: none;
  background-color: var(--secondary-background-color);
  color: var(--primary-color);
  box-shadow: 0 8px 16px 0 rgba(0, 0, 0, 0.2);
  z-index: 1;
  position: absolute;
  border-radius: 8px;
}

.sort-dropdown.unselected-link {
  display: block;
  padding: 0.25em 0.25em;
  border-radius: 8px;
  color: var(--primary-color);
  background-color: var(--secondary-background-color);
  text-decoration: none;
  margin-top: 1px;
  margin-bottom: 5px;
}

.sort-dropdown.unselected-link:hover,
.sort-dropdown.unselected-link:active {
  display: block;
  height: 35px;
  font-size: large;
  text-align: center;
  transition: all 250ms;
  color: var(--secondary-background-color);
  background-color: var(--primary-color);
  text-decoration: none;
}

.sort-dropdown:hover ul {
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
