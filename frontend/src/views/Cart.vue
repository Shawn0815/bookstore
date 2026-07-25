<template>
  <div class="cart-table-wrapper">
    <div class="cart-table">
      <ul>
        <li class="cart-heading">
          <div class="cart-heading-book">書籍</div>
          <div class="cart-heading-price">價格</div>
          <div class="cart-heading-quantity">數量</div>
          <div class="cart-heading-subtotal">小計</div>
          <div class="cart-heading-action">操作</div>
        </li>

        <li
          v-for="item in $store.state.cart.cartItemList"
          :key="item.cartItemId"
          class="cart-row-wrapper"
          :class="item.status.toLowerCase()"
        >
          <div class="cart-row">
            
            <!-- 商品資訊 -->
            <router-link
              v-if="item.status === 'AVAILABLE'"
              :to="{ name: 'bookDetail', params: { id: item.bookId } }"
              class="cart-row-link"
            >
              <div class="cart-book-image">
                <img
                  :src="item.imageUrl"
                  :alt="item.title"
                  style="width: 100px; height: 120px; border-radius: 6px;"
                />
              </div>
              <div class="cart-book-title">{{ item.title }}</div>
              <div class="cart-book-price">NT$ {{ item.price.toFixed(2) }}</div>
            </router-link>

            <!-- 庫存達上限 -->
            <div v-else-if="item.status === 'REACHED_LIMIT'" class="cart-row-link reached-limit">
              <router-link
                :to="{ name: 'bookDetail', params: { id: item.bookId } }"
                class="cart-row-link"
              >
                <div class="cart-book-image">
                  <img
                    :src="item.imageUrl"
                    :alt="item.title"
                    style="width: 100px; height: 120px; border-radius: 6px;"
                  />
                </div>
                <div class="cart-book-title">
                  {{ item.title }}
                  <br /> <br />
                  <span class="limit-message">{{ item.message }}</span>
                </div>
                <div class="cart-book-price">NT$ {{ item.price.toFixed(2) }}</div>
              </router-link>
            </div>

            <!-- 庫存不足 -->
            <div v-else-if="item.status === 'OUT_OF_STOCK'" class="cart-row-link">
              <div class="cart-book-image">
                <img
                  :src="item.imageUrl"
                  alt="庫存不足"
                  style="width: 100px; height: 120px; border-radius: 6px;"
                />
              </div>
              <div class="cart-book-title out_of_stock">{{ item.title }} <br/> <br/>{{ item.message }}</div>
              <div class="cart-book-price">NT$ {{ item.price.toFixed(2) }}</div>
            </div>

            <!-- 下架的情況 -->
            <div v-else class="cart-row-link">
              <div class="cart-book-image">
                <img
                  src="https://as1.ftcdn.net/jpg/03/34/83/22/1000_F_334832255_IMxvzYRygjd20VlSaIAFZrQWjozQH6BQ.jpg"
                  alt="已下架"
                  style="width: 100px; height: 120px; border-radius: 6px;"
                />
              </div>
              <div class="cart-book-title discontinued">{{ item.message }}</div>
              <div class="cart-book-price">—</div>
            </div>

            <!-- 數量按鈕 -->
            <div class="cart-book-quantity">
              
              <!-- 減號：只在 DISCONTINUED 禁用 -->
              <button
                class="icon-button dec-button"
                @click.stop="updateCart(item.bookId, item.quantity - 1)"
                :disabled="item.status === 'DISCONTINUED'"
              >
                －
              </button>

              <span class="row-book-quantity">{{ item.quantity }}</span>

              <!-- 加號：只在 AVAILABLE 時啟用 -->
              <button
                class="icon-button inc-button"
                @click.stop="updateCart(item.bookId, item.quantity + 1)"
                :disabled="item.status !== 'AVAILABLE'"
              >
                ＋
              </button>
            </div>

            <!-- 小計 -->
            <div class="subtotal">
              <span v-if="item.status !== 'DISCOTINUED'">NT$ {{ item.amount.toFixed(2) }}</span>
              <span v-else class="disabled">—</span>
            </div>

            <!-- 刪除 -->
            <div class="cart-book-remove">
              <button @click="$store.dispatch('deleteCartItem', item.bookId)" class="remove-button">
                刪除
              </button>
            </div>
          </div>
        </li>

        <div class="row-sep"></div>

        <!-- 總計 -->
        <li class="cart-total">
          <div class="cart-heading-book">總計</div>
          <div class="cart-book-quantity" style="text-align: center">
            {{ $store.state.cart.numberOfItems }}
          </div>
          <div class="cart-book-price total-amount">
            NT$ {{ $store.state.cart.total.toFixed(2) }}
          </div>
        </li>
      </ul>
    </div>

    <!-- 下排按鈕 -->
    <div class="cart-buttons">
      <button class="clear-cart-button" @click="$store.dispatch('clearCart')">
        清空購物車
      </button>
      <router-link :to="{ name: 'books' }">
        <button class="continue-shopping-button">繼續逛書</button>
      </router-link>
      <router-link :to="{ name: 'checkout' }">
        <button 
          class="checkout-button"
          :disabled="$store.state.cart.cartItemList.some(i => i.status === 'OUT_OF_STOCK' || i.status === 'DISCONTINUED')">
          結帳
        </button>
      </router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: "CartTable",
  methods: {
    updateCart(bookId, newQuantity) {
      if (newQuantity < 0) return;
      if (newQuantity === 0) {
        this.$store.dispatch("deleteCartItem", bookId);
      } else {
        this.$store.dispatch("updateCart", { bookId, quantity: newQuantity });
      }
    },
  },
};
</script>

<style scoped>
.cart-table-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center; /* 置中 */
  padding: 25px;
}

.cart-table {
  display: grid;
  /* 讓第二欄 (書名) 佔據所有剩餘空間 */
  grid-template-columns: max-content 1fr max-content 100px max-content 100px;
  
  row-gap: 1em;
  background-color: #fafafa;
  padding: 15px;
  margin-top: 10px;
  border-radius: 10px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 900px; /* 如果您想讓表格填滿螢幕，請記得移除這個限制！ */
}

ul,
li {
  display: contents;
}

.cart-row-wrapper {
  background: white;
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.cart-row {
  display: contents;
}

.cart-row-link {
  display: contents;
  text-decoration: none;
  color: inherit;
}

.row-sep {
  grid-column: 1 / -1;
  background-color: #ddd;
  height: 2px;
  margin-top: 10px;
}

.cart-heading {
  background-color: #6f4e37;
  color: white;
  border-radius: 8px;
}

.cart-heading > * {
  background-color: #6f4e37;
  color: white;
}

.cart-heading-book {
  grid-column: 1 / 3;
  padding: 0 1em;
}

.cart-heading-price {
  grid-column: 3 / 4;
  padding: 0 1em;
  text-align: center;
}

.cart-heading-quantity {
  grid-column: 4 / 5;
  padding: 0 1em;
  text-align: center;
}

.cart-heading-subtotal {
  grid-column: 5 / 6;
  padding: 0 1em;
  text-align: center;
}

.cart-heading-action {
  grid-column: 6 / 6;
  padding: 0 1em;
  text-align: center;
}

.cart-book-image {
  padding: 0 1em;
}

.cart-book-title {
  padding: 0 1em;
  font-weight: 500;
}

.cart-book-price {
  grid-column: 3 / 4;
  text-align: right;
  padding: 0 1em;
}

.cart-book-quantity {
  padding: 0 1em;
  display: flex;
  align-items: flex-start;
  justify-content: center;
}

.subtotal {
  text-align: right;
  padding: 0 1em;
  font-weight: bold;
}

.cart-book-remove {
  grid-column: 6 / 7;
  text-align: center;
}

.remove-button {
  background: transparent;
  border: none;
  color: gray;
  cursor: pointer;
  font-weight: bold;
}

.remove-button:hover {
  text-decoration: underline;
}

.icon-button {
  color: #6f4e37;
  font-size: 1rem;
  background-color: transparent;
  border: none;
  cursor: pointer;
}

.icon-button:hover {
  color: #4b2e2a;
}

.inc-button,
.dec-button {
  border-radius: 50%;
  padding: 2px 6px;
  margin: 0 4px;
  background: #f7f7f7;
}

.inc-button:hover,
.dec-button:hover {
  background: #e0d4c3;
}

.cart-total {
  font-weight: bold;
}

.cart-total .cart-heading-book {
  grid-column: 1 / 3;
}

.cart-total .cart-book-quantity {
  grid-column: 4 / 5;
}

.cart-total .cart-book-price {
  grid-column: 5 / 6;
}

.total-amount {
  color: red;
}

.cart-heading-book,
.cart-book-title {
  font-size: 1rem;
}

.cart-book-price,
.subtotal {
  font-size: 0.95rem;
}

/* 下排按鈕 */
.cart-buttons {
  display: flex;
  justify-content: flex-start; /* 靠左 */
  gap: 12px;
  margin-top: 30px;
  width: 100%;
  max-width: 900px; /* 對齊表格寬度 */
}

.continue-shopping-button,
.clear-cart-button,
.checkout-button {
  padding: 10px 18px;
  border: none;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
}

.checkout-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.continue-shopping-button {
  background-color: #d2b48c;
  color: white;
}

.clear-cart-button {
  background-color: #a67c52;
  color: white;
}

.checkout-button {
  background-color: #6f4e37;
  color: white;
}

.cart-row-wrapper.available { opacity: 1; }
.cart-row-wrapper.out_of_stock { opacity: 0.6; color: gray; }
.cart-row-wrapper.reached_limit {opacity: 1; }
.cart-row-wrapper.discontinued { opacity: 0.4; color: red; text-decoration: line-through; }
.limit-message { color: #e67e22; font-weight: 600; }

.disabled {
  color: #999;
  font-style: italic;
}
</style>
