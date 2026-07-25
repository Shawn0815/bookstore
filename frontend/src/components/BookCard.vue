<template>
  <li class="book-box">
    <div class="book-image">
      <!-- 圖片點擊跳到書籍詳細頁 -->
      <router-link :to="{ name: 'bookDetail', params: { id: book.bookId } }">
        <!-- img 要從 books 裡的 imageUrl 取得 -->
        <img :src="book.imageUrl" :alt="book.title" style="height: 190px" width="148px" />
      </router-link>
    </div>

    <div class="book-title" :class="{ 'small-title': book.title.length > 8 }">
      <router-link :to="{ name: 'bookDetail', params: { id: book.bookId } }">
        {{ book.title }}
      </router-link>
    </div>
    <div class="book-author">
      {{ book.author }}
    </div>
    <div class="book-date">
      {{ book.publishedDate.replace(/-/g, "/") }}
    </div>
    <div class="book-sales-count">
      剩餘：{{ book.stock }}（已售出：{{ book.salesCount }}）
    </div>
    <!-- 改成以 $xxx 來呈現價錢 -->
    <div class="book-price">${{ book.price }}</div>
    <button class="button add-to-cart" @click="addToCart(book)">
      加入購物車
    </button>
    <!-- <button v-if="book.isPublic" class="button read-now">Read Now</button> -->
  </li>
</template>

<script>
export default {
  name: "BookCard",
  props: {
    book: {
      type: Object,
      required: true,
    },
  },
  methods: {
    addToCart(book) {
      this.$store.dispatch("addToCart", book);
    },
    bookImageFileName(book) {
      let bookName = book.title;
      return `${bookName}.png`;
    },
  },
};
</script>

<style scoped>
#book-boxes {
  display: flex;
  flex-wrap: wrap;
  flex-direction: row;
  padding: 1em;
  gap: 1em;
}

.book-box {
  box-sizing: border-box;
  display: grid;
  width: 310px;
  height: 205px;
  grid-template-columns: 52% 48%;
  grid-template-rows: 50px 25px 25px 23px 35px 45px;
  background-color: var(--secondary-background-color);
  padding: 0.4em;
  /* gap: 0.25em; */
  box-shadow: 0 5px 15px 0 rgba(0, 0, 0, 0.19);
  border-radius: 10px;
}

.book-image {
  grid-column: 1/2;
  grid-row: 1/-1;
}

.book-title {
  font-family: "Itim", sans-serif;
  font-size: 17px;
  display: -webkit-box;           /* 讓文字能限制行數 (for WebKit) */
  -webkit-box-orient: vertical;   /* 垂直排列 */
  -webkit-line-clamp: 2;          /* 最多顯示 2 行 */
  overflow: hidden;               /* 超出隱藏 */
  text-overflow: ellipsis;        /* 顯示 ... */
  word-break: break-word;         /* 長單詞換行 */
  
  box-orient: vertical;
  line-clamp: 2;

  grid-column: 2;  /* 右欄 */
  grid-row: 1;     /* 最下面一行 */
}

.book-title.small-title {
  font-size: 16px; /* 書名長就變小字 */
}

.book-title a:link,
.book-title a:visited,
.book-title a:active {
  color: #111;
  font-weight: 500;
}

.book-title a:hover {
  color: #b22222;       /* hover 時用紅色 (或主題色)，增加互動感 */
}

.book-author {
  font-family: "Nunito", sans-serif;
  font-size: 14px;
  color: var(--primary-color);
  display: flex;
  font-style: italic;
  font-weight: bold;
  grid-column: 2;  /* 右欄 */
  grid-row: 2;     /* 最下面一行 */
  overflow: hidden;               /* 超出隱藏 */
  text-overflow: ellipsis;        /* 顯示 ... */
}

.book-date {
  font-family: "Nunito", sans-serif;
  font-size: 14px;
  color: var(--primary-color);
  display: flex;
  font-style: normal;
  font-weight: normal;
  grid-column: 2;  /* 右欄 */
  grid-row: 3;     /* 最下面一行 */
}

.book-sales-count {
  font-family: "Nunito", sans-serif;
  font-size: 12px;
  grid-column: 2;  /* 右欄 */
  grid-row: 4;     /* 最下面一行 */
}

.book-price {
  color: #007a33;
  font-size: 18px;
  font-family: "Nunito", sans-serif;
  font-weight: bold;
  grid-column: 2;  /* 右欄 */
  grid-row: 5;     /* 倒數第二行 */
}

.add-to-cart {
  grid-column: 2;  /* 右欄 */
  grid-row: 6;     /* 最下面一行 */
  align-self: end;
}
</style>
