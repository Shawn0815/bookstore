<template>
  <ul class="book-grid">
    <template v-for="book in $store.state.books">
      <li :key="book.bookId">
        <book-card :book="book"></book-card>
      </li>
    </template>
  </ul>
</template>

<script>
import BookCard from "./BookCard";

export default {
  name: "BookGrid",
  components: {
    BookCard,
  },
};
</script>

<style scoped>
.book-grid {
  display: flex;
  flex-wrap: wrap; /* 滿一行換行 */
  gap: 1em; /* 卡片間距 */
  padding: 1em 1em 1em 3em; /* 左側偏移 */
  margin-top: 3em;
  align-items: flex-start; /* 避免整列被拉高 */
}

.book-grid li {
  list-style: none;
}
</style>
