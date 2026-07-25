<template>
  <div class="order-page">
    <!-- 返回當前訂單 -->
    <router-link
      v-if="!isCurrent"
      :to="{ name: 'myorder', params: { type: 'current' } }"
      class="order-switch-button return-current-order-link"
    >
      查看最新訂單 🖐
    </router-link>

    <!-- 當前訂單 -->
    <section v-if="isCurrent" class="non-empty-order-page">
      <h2 class="page-title">當前訂單</h2>

      <div v-if="$store.state.currentOrder">
        <div class="order-card">
          <div class="order-header">
            <h3>訂單 #{{$store.state.currentOrder.orderId}}</h3>
            <span class="order-date">{{ formatDate($store.state.currentOrder.createdDate) }}</span>
          </div>

          <div class="order-items">
            <div
              v-for="item in $store.state.currentOrder.orderItemList"
              :key="item.bookId"
              class="order-item"
              @click="$router.push({ name: 'bookDetail', params: { id: item.bookId } })"
            >
              <img :src="item.imageUrl" :alt="item.title" style="width:80px; height:auto" />
              <div class="item-details">
                <div class="item-title">{{ item.title }}</div>
                <div class="item-author">{{ item.author }}</div>
                <div class="item-price">NT$ {{ item.price.toFixed(2) }}</div>
                <div class="item-quantity">× {{ item.quantity }}</div>
              </div>
            </div>
          </div>

          <div class="order-footer">
            <p class="order-total">總金額：NT$ {{ $store.state.currentOrder.totalAmount.toFixed(2) }}</p>
          </div>
        </div>
      </div>

      <div v-else class="empty-orders">
        <p>目前沒有訂單</p>
        <router-link :to="{ name: 'books' }">
          <button class="button continue-shop-buttons">去逛逛書籍</button>
        </router-link>
      </div>

      <div class="order-buttons">
        <router-link :to="{ name: 'myorder', params: { type: 'history' } }" class="order-switch-button return-current-order-link">
          查看歷史訂單 🖐
        </router-link>
      </div>
    </section>

    <!-- 歷史訂單 -->
    <section v-else class="non-empty-order-page">
      <h2 class="page-title">我的訂單</h2>

      <div v-if="$store.state.myOrders.length === 0 && !loading" class="empty-orders">
        <p>目前沒有訂單</p>
        <router-link :to="{ name: 'books' }">
          <button class="button continue-shop-buttons">去逛逛書籍</button>
        </router-link>
      </div>

      <div v-for="order in $store.state.myOrders" :key="order.orderId" class="order-card">
        <div class="order-header">
          <h3>訂單 #{{ order.orderId }}</h3>
          <span class="order-date">{{ formatDate(order.createdDate) }}</span>
        </div>

        <div class="order-items">
          <div
            v-for="item in order.orderItemList"
            :key="item.bookId"
            class="order-item"
            @click="$router.push({ name: 'bookDetail', params: { id: item.bookId } })"
          >
            <img :src="item.imageUrl" :alt="item.title" style="width: 80px; height: auto" />
            <div class="item-details">
              <div class="item-title">{{ item.title }}</div>
              <div class="item-author">{{ item.author }}</div>
              <div class="item-price">NT$ {{ item.price.toFixed(2) }}</div>
              <div class="item-quantity">× {{ item.quantity }}</div>
            </div>
          </div>
        </div>

        <div class="order-footer">
          <p class="order-total">總金額：NT$ {{ order.totalAmount.toFixed(2) }}</p>
        </div>
      </div>

      <div v-if="loading" class="loading">載入中...</div>
      <div v-if="!$store.state.myOrdersHasMore && $store.state.myOrders.length > 0" class="end-message">
        沒有更多訂單了
      </div>
    </section>
  </div>
</template>

<script>
export default {
  data() {
    return {
      loading: false
    };
  },
  computed: {
    isCurrent() {
      return this.$route.params.type === "current";
    }
  },
  watch: {
    "$route"(to) {
      if (to.params.type === "current") {
        this.$store.dispatch("fetchCurrentOrder");
      } else {
        this.loadOrders();
      }
    }
  },
  mounted() {
    if (this.isCurrent) {
      this.$store.dispatch("fetchCurrentOrder");
    } else {
      this.loadOrders();
    }
    window.addEventListener("scroll", this.handleScroll);
  },
  beforeUnmount() {
    window.removeEventListener("scroll", this.handleScroll);
  },
  methods: {
    async loadOrders() {
      if (this.loading || !this.$store.state.myOrdersHasMore) return;
      this.loading = true;
      await this.$store.dispatch("fetchMyOrders", this.$store.state.myOrdersPage);
      this.loading = false;
    },
    handleScroll() {
      if (this.isCurrent) return;
      const nearBottom = window.innerHeight + window.scrollY >= document.body.offsetHeight - 50;
      if (nearBottom) {
        this.loadOrders();
      }
    },
    formatDate(dateStr) {
      const d = new Date(dateStr);
      return isNaN(d) ? dateStr : d.toLocaleString();
    }
  }
};
</script>

<style scoped>
.order-page {
  padding: 2em;
  background-color: var(--secondary-background-color);
}

.non-empty-order-page {
  display: flex;
  flex-direction: column;
  gap: 1em;
}

.order-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0px 3px 8px rgba(0, 0, 0, 0.1);
  width: 70%;
  margin-left: auto;
  margin-right: auto;
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  align-items: center;
  background: #f9f9f9;
  border-radius: 8px;
  padding: 10px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.order-item:hover {
  background: #f0f0f0;
}

.item-details {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  flex: 1;
  margin-left: 12px;
}

.item-title {
  font-weight: bold;
}

.item-author {
  font-size: 0.9em;
  color: gray;
}

.item-price {
  font-weight: bold;
  color: var(--secondary-color);
  text-align: right;
}

.item-quantity {
  font-size: 0.9em;
}

.order-footer {
  text-align: right;
  font-weight: bold;
  margin-top: 14px;
}

.page-title {
  font-size: 1.5em;
  font-weight: bold;
}

.loading,
.end-message,
.empty-orders {
  text-align: center;
  padding: 15px;
  color: gray;
}

.continue-shop-buttons {
  background-color: #2aa77a;
  color: white;
  padding: 0.5em 1em;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.continue-shop-buttons:hover {
  background-color: #1f8e63;
}

.order-switch-button {
  background-color: #2aa77a;
  color: white;
  font-weight: bold;
  padding: 10px 15px;
  border-radius: 50px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  text-decoration: none;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
}

.order-switch-button:hover {
  background-color: #1f8e63;
}

.return-current-order-link {
  position: fixed;
  bottom: 60px;
  right: 20px;
}
</style>
