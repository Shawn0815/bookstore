<!-- 把原來的 Category.vue 改名為 BookListPage.vue -->
<!-- 因為它同時包含顯示所有書籍以及特定類別書籍（未來還可以括充） -->

<template>
  <div>
    <div class="book-list-page">
      <category-nav></category-nav>
      <book-grid></book-grid>
    </div>

    <page-bar
      :current-page="$store.state.currentPage"
      :total-pages="$store.state.totalPages"
    />
  </div>
</template>

<script>
import CategoryNav from "@/components/CategoryNav";
import BookGrid from "@/components/BookGrid";
import PageBar from "@/components/PageBar";

export default {
  name: "BookListPage",
  components: {
    PageBar,
    CategoryNav,
    BookGrid,
  },

  /*fetch books from database*/
  created: function () {
    const self = this;

    // 從 query 中取出所有可能的篩選參數
    const filters = Object.fromEntries(
      Object.entries({
        category: this.$route.query.category,
        search: this.$route.query.search,
        sortBy: this.$route.query.sortBy,
        order: this.$route.query.order,
        page: this.$route.query.page, // 新增
        limit: this.$route.query.limit, // 新增
      }).filter(([, v]) => v !== undefined && v !== "")
    );

    if (filters) {
      // 如果有篩選條件
      this.$store.dispatch("fetchBooksByFilter", filters).catch(function () {
        self.$router.push("/404"); //'/404' triggers NotFound
      });
    } else {
      // 如果沒有參數
      this.$store.dispatch("fetchAllBooks").catch(function () {
        self.$router.push("/404"); //'/404' triggers NotFound
      });
    }
  },
};
</script>

<style scoped>
.book-list-page {
  background-color: var(--secondary-background-color);
  display: flex;
  flex-direction: row;
  flex-grow: 1;
}
</style>
