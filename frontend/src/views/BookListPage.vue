<!-- 把原來的 Category.vue 改名為 BookListPage.vue -->
<!-- 因為它同時包含顯示所有書籍以及特定類別書籍（未來還可以括充） -->

<template>
  <div>
    <div class="book-list-page" ref="bookListPage">
      <category-nav ref="categoryNav"></category-nav>
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

// 書卡寬度（BookCard.vue 的 .book-box）與 .book-grid 的 gap，用來換算一排放得下幾本
const CARD_WIDTH = 310;
const GRID_GAP = 16; // 1em
// .book-grid 的左右 padding（各 1em）與 .category-nav 的 margin-left/margin-right
const GRID_PADDING_LEFT = 16;
const GRID_PADDING_RIGHT = 16;
const NAV_MARGIN_LEFT = 64;
const NAV_MARGIN_RIGHT = 48;
const ROWS = 2; // 固定顯示兩排

export default {
  name: "BookListPage",
  components: {
    PageBar,
    CategoryNav,
    BookGrid,
  },

  data() {
    return {
      limit: 8,
    };
  },

  mounted() {
    window.addEventListener("resize", this.handleResize);

    // 剛掛載時 CSS/版面可能還沒 settle（.book-list-page 量到寬度 0），
    // 用 nextTick + requestAnimationFrame 等瀏覽器畫完一幀後再量測
    this.$nextTick(() => {
      requestAnimationFrame(() => {
        this.limit = this.computeLimit();
        this.fetchBooks();
      });
    });
  },

  beforeDestroy() {
    window.removeEventListener("resize", this.handleResize);
    clearTimeout(this.resizeTimer);
  },

  methods: {
    computeLimit() {
      // .book-list-page 的寬度不受書籍卡片數量影響（flex-grow:1 撐滿父層）
      // .book-grid 本身在還沒有卡片時會塌縮成 0，所以改成用「頁面寬度 - 側欄寬度 - padding」反推
      const pageEl = this.$refs.bookListPage;
      const navEl = this.$refs.categoryNav ? this.$refs.categoryNav.$el : null;

      const pageWidth = pageEl ? pageEl.getBoundingClientRect().width : window.innerWidth;
      const navWidth = navEl ? navEl.getBoundingClientRect().width : 0;

      const availableWidth =
        pageWidth -
        navWidth -
        NAV_MARGIN_LEFT -
        NAV_MARGIN_RIGHT -
        GRID_PADDING_LEFT -
        GRID_PADDING_RIGHT;

      const columnsPerRow = Math.max(
        1,
        Math.floor((availableWidth + GRID_GAP) / (CARD_WIDTH + GRID_GAP))
      );
      return columnsPerRow * ROWS;
    },

    handleResize() {
      clearTimeout(this.resizeTimer);
      this.resizeTimer = setTimeout(() => {
        const newLimit = this.computeLimit();
        if (newLimit !== this.limit) {
          this.limit = newLimit;
          this.fetchBooks();
        }
      }, 200);
    },

    /*fetch books from database*/
    fetchBooks() {
      const self = this;

      // 從 query 中取出所有可能的篩選參數
      const filters = Object.fromEntries(
        Object.entries({
          category: this.$route.query.category,
          search: this.$route.query.search,
          sortBy: this.$route.query.sortBy,
          order: this.$route.query.order,
          page: this.$route.query.page,
          limit: this.limit, // 依視窗寬度算出的每頁本數
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
  },
};
</script>

<style scoped>
.book-list-page {
  background-color: var(--secondary-background-color);
  display: flex;
  flex-direction: row;
  justify-content: center; /* 側欄+書本這個區塊在寬螢幕上置中，不要整塊貼左邊 */
  flex-grow: 1;
}
</style>
