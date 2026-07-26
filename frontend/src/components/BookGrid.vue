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
  flex: 1; /* 撐滿側欄以外剩下的寬度，塞得下就塞，塞不下就留白 */
  flex-wrap: wrap; /* 滿一行換行 */
  justify-content: center; /* 欄數無法整除時，剩餘空間左右平均分配 */
  row-gap: 0.5em; /* 兩排書之間的垂直間距，縮小一點 */
  column-gap: 1em; /* 同一排書之間的間距 */
  padding: 1em; /* 跟側欄的距離改由 CategoryNav 的 margin-right 負責 */
  align-items: flex-start; /* 避免整列被拉高 */
}

.book-grid li {
  list-style: none;
}
</style>
