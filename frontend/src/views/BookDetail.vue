<template>
  <div class="book-detail-container">
    <!-- 左側：圖片 + 作者資訊 -->
    <div class="book-left">
      <a :href="book.originalUrl" target="_blank" rel="noopener noreferrer">
        <img :src="book.imageUrl" alt="Book Cover" style="height: 330px" />
      </a>
      
      <div class="book-info">
        <h1>{{ book.title }}</h1>
        <p><strong>作者：</strong>{{ book.author }}</p>
        <p><strong>出版社：</strong>{{ book.publisher }}</p>
        <p><strong>出版日期：</strong>{{ formatDate(book.publishedDate) }}</p>
      </div>
    </div>

    <!-- 右側：內容簡介 + 購物區 -->
    <div class="book-right">
      <div class="book-description-section">
        <h2 class="description-title">內容簡介</h2>
        <div class="book-description">{{ book.description }}</div>
      </div>

      <div class="purchase-box">
        <p class="purchase-label">購買電子書</p>
        <p class="purchase-price">您的價格 ${{ book.price }} 元</p>
        <div class="quantity-selector">
          <button @click="decreaseQuantity">-</button>
          <input type="number" v-model.number="quantity" />
          <button @click="increaseQuantity">+</button>
        </div>
        <button class="add-cart-button" @click="addToCart">新增至購物車</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      book: {},
      quantity: 1,
    };
  },
  created() {
    const id = this.$route.params.id;
    fetch(`http://localhost:8080/books/${id}`)
      .then((res) => res.json())
      .then((data) => {
        this.book = data;
      });
  },
  methods: {
    formatDate(date) {
      if (!date) return "";
      return new Date(date).toLocaleDateString();
    },
    increaseQuantity() {
      this.quantity++;
    },
    decreaseQuantity() {
      if (this.quantity > 1) this.quantity--;
    },
    addToCart() {
      console.log(`加入購物車: ${this.book.title} x ${this.quantity}`);
      this.$store.dispatch("addToCart", this.book);
    },
  },
};
</script>

<style scoped>
.book-detail-container {
  display: flex;
  gap: 100px;
  padding: 20px 40px;
}

/* 左側 */
.book-left {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  min-width: 400px;
}

.book-left img {
  width: 250px;
  height: auto;
  border-radius: 8px;
  margin-bottom: 20px;
}

.book-info h1 {
  margin: 0 0 10px 0;
  font-size: 24px;
  max-width: 250px;       /* 跟圖片同寬 */
  word-wrap: break-word;  /* 自動換行 */
  overflow-wrap: break-word;
  line-height: 1.6; /* 行距，1.0 是緊貼，1.6 較寬鬆 */
}

.book-info p {
  margin: 4px 0;
  font-size: 16px;
}

/* 右側 */
.book-right {
  display: flex;
  gap: 250px; /* 調整間隔，跟圖片與簡介間距一致 */
  flex: 1;
  align-items: flex-start; /* 上方對齊 */
}

/* 內容簡介區 */
.book-description-section {
  flex: 1; /* 自動撐開 */
}

.description-title {
  font-size: 24px;
  margin-top: 0;
  line-height: 1.6;
  margin-bottom: 30px;
}

.book-description {
  font-size: 16px;
  line-height: 1.6;
  white-space: pre-line;
  margin-bottom: 30px;
}

/* 購物區 */
.purchase-box {
  border: 1px solid #ccc;
  padding: 15px;
  border-radius: 8px;
  width: 250px; /* 固定寬度 */
  flex-shrink: 0; /* 不被拉伸 */
}

.purchase-label {
  font-weight: bold;
  margin: 0 0 10px 0;
}

.purchase-price {
  color: #b55a30;
  font-weight: bold;
  margin: 0 0 15px 0;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 10px;
}

.quantity-selector button {
  width: 30px;
  height: 30px;
  font-size: 18px;
  cursor: pointer;
}

.quantity-selector input {
  width: 40px;
  height: 30px;
  text-align: center;
  font-size: 18px;
  border-radius: 4px;
  border: 1px solid #ccc;
}

.add-cart-button {
  width: 100%;
  background-color: #b55a30;
  color: white;
  border: none;
  padding: 8px 0;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}

.add-cart-button:hover {
  background-color: #a04828;
}
</style>
