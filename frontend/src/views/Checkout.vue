<template>
  <div class="checkout-page">
    <!-- 結帳頁面 -->
    <section class="checkout-page-body" >
      <!-- 左側表單 -->
      <form class="checkout-form" @submit.prevent="submitOrder">
        <div>
          <label for="name">姓名</label>
          <input type="text" id="name" v-model.lazy="$v.name.$model" @input="saveForm" />
          <div class="error">
            <span v-if="$v.name.$error">
              <span v-if="!$v.name.required">姓名為必填</span>
              <span v-else-if="!$v.name.minLength">
                姓名至少 {{ $v.name.$params.minLength.min }} 個字
              </span>
              <span v-else-if="!$v.name.maxLength">
                姓名最多 {{ $v.name.$params.maxLength.max }} 個字
              </span>
              <span v-else>格式錯誤</span>
            </span>
          </div>
        </div>

        <div>
          <label for="address">地址</label>
          <input type="text" id="address" v-model.lazy="$v.address.$model" @input="saveForm" />
          <div class="error">
            <span v-if="$v.address.$error">
              <span v-if="!$v.address.required">地址為必填</span>
              <span v-else-if="!$v.address.minLength">
                地址至少 {{ $v.address.$params.minLength.min }} 個字
              </span>
              <span v-else-if="!$v.address.maxLength">
                地址最多 {{ $v.address.$params.maxLength.max }} 個字
              </span>
              <span v-else>格式錯誤</span>
            </span>
          </div>
        </div>

        <div>
          <label for="phone">手機號碼</label>
          <input type="text" id="phone" v-model.lazy="$v.phone.$model" @input="saveForm" />
          <div class="error">
            <span v-if="$v.phone.$error">
              <span v-if="!$v.phone.required">手機號碼為必填</span>
              <span v-else-if="!$v.phone.phone">請輸入有效的手機號碼</span>
            </span>
          </div>
        </div>

        <div>
          <label for="email">電子郵件</label>
          <input type="text" id="email" v-model.lazy="$v.email.$model" @input="saveForm" />
          <div class="error">
            <span v-if="$v.email.$error">
              <span v-if="!$v.email.required">Email 為必填</span>
              <span v-else-if="!$v.email.email">請輸入有效的 Email</span>
            </span>
          </div>
        </div>

        <div>
          <label for="ccNumber">信用卡號</label>
          <input type="text" id="ccNumber" v-model.lazy="$v.ccNumber.$model" @input="saveForm" />
          <div class="error">
            <span v-if="$v.ccNumber.$error">
              <span v-if="!$v.ccNumber.required">信用卡號為必填</span>
              <span v-else-if="!$v.ccNumber.creditCard">請輸入有效的信用卡號</span>
            </span>
          </div>
        </div>

        <div class="form-group-inline">
          <label>到期日</label>
          <select v-model="ccExpiryMonth" @change="saveForm">
            <option v-for="(month, index) in months" :key="index" :value="index + 1">
              {{ month }} ({{ index + 1 }})
            </option>
          </select>
          <select v-model="ccExpiryYear" @change="saveForm">
            <option v-for="(year, index) in 15" :key="index" :value="yearFrom(index)">
              {{ yearFrom(index) }}
            </option>
          </select>
        </div>

        <input
          type="submit"
          name="submit"
          class="submit-button"
          :disabled="checkoutStatus == 'PENDING'"
          value="完成購買"
        />

        <div v-show="checkoutStatus" class="checkoutStatusBox">
          <div v-if="checkoutStatus === 'ERROR'">請修正上方錯誤再試一次。</div>
          <div v-else-if="checkoutStatus === 'PENDING'">處理中...</div>
          <div v-else-if="checkoutStatus === 'OK'">訂單已成立！</div>
          <div v-else>系統發生錯誤，請稍後再試。</div>
        </div>
      </form>

      <!-- 右側：商品內容 -->
      <aside class="purchase-info-box">
        <h3>購物車內容</h3>
        <ul class="cart-summary-list">
          <li v-for="item in cart.cartItemList.filter(i => i.status === 'AVAILABLE')" 
            :key="item.bookId" class="cart-summary-item">
            <img :src="item.imageUrl" :alt="item.title" class="cart-item-image" />
            <div class="cart-item-info">
              <div class="cart-item-title">{{ item.title }}</div>
              <div class="cart-item-quantity">數量: {{ item.quantity }}</div>
              <div class="cart-item-price">NT$ {{ item.amount }}</div>
            </div>
          </li>
        </ul>

        <!-- 移除框框，增加間距 -->
        <div class="cart-summary-spacer"></div>

        <ul>
          <li>小計：<b>{{ cart.total | asDollarsAndCents }}</b></li>
          <li class="total">
            總計：<b>{{ cart.total | asDollarsAndCents }}</b>
          </li>
        </ul>
      </aside>
    </section>
  </div>
</template>

<script>
import {
  required,
  email,
  minLength,
  maxLength,
} from "vuelidate/lib/validators";

import isCreditCard from "validator/lib/isCreditCard";
import isMobilePhone from "validator/lib/isMobilePhone";

const phone = (value) => isMobilePhone(value, "zh-TW");
const creditCard = (value) => isCreditCard(value);

export default {
  data() {
    const form = this.$store.state.checkoutForm || {};
    return {
      name: form.name || "",
      address: form.address || "",
      phone: form.phone || "",
      email: form.email || "",
      ccNumber: form.ccNumber || "",
      ccExpiryMonth: form.ccExpiryMonth || new Date().getMonth() + 1,
      ccExpiryYear: form.ccExpiryYear || new Date().getFullYear(),
      checkoutStatus: "",
    };
  },

  validations: {
    name: { required, minLength: minLength(4), maxLength: maxLength(45) },
    phone: { required, phone },
    ccNumber: { required, creditCard },
    address: { required, minLength: minLength(4), maxLength: maxLength(45) },
    email: { required, email },
  },
  computed: {
    cart() {
      return this.$store.state.cart;
    },
    months() {
      return [
        "1月", "2月", "3月", "4月", "5月", "6月",
        "7月", "8月", "9月", "10月", "11月", "12月"
      ];
    },
  },
  filters: {
    asDollarsAndCents(value) {
      return "NT$ " + parseFloat(value).toFixed(2);
    },
  },
  methods: {
    submitOrder() {
      if (!this.$store.state.user) {
        alert("請先登入後再進行結帳");
        this.$router.push({ name: "login" });
        return;
      }

      this.$v.$touch();
      if (this.$v.$invalid) {
        this.checkoutStatus = "ERROR";
        return;
      }

      this.checkoutStatus = "PENDING";

      const customerForm = {
        name: this.name,
        address: this.address,
        phone: this.phone,
        email: this.email,
        ccNumber: this.ccNumber,
        ccExpiryMonth: this.ccExpiryMonth,
        ccExpiryYear: this.ccExpiryYear
      };

      this.$store.dispatch("placeOrder", customerForm)
      .then(() => {
        this.checkoutStatus = "OK";
        this.$router.push({ name: "myorder", params: { type: "current" } });
      })
      .catch((err) => {
        console.error(err);
        this.checkoutStatus = "SERVER_ERROR";
      });
    },
    
    yearFrom(index) {
      return new Date().getFullYear() + index;
    },

    saveForm() {
      const form = {
        name: this.name,
        address: this.address,
        phone: this.phone,
        email: this.email,
        ccNumber: this.ccNumber,
        ccExpiryMonth: this.ccExpiryMonth,
        ccExpiryYear: this.ccExpiryYear
      };
      this.$store.dispatch("saveCheckoutForm", form);
    }
  },
};
</script>

<style scoped>
.checkout-page-body {
  display: grid;
  grid-template-columns: 1fr;
  gap: 2rem;
  padding: 1.5rem;
  max-width: 800px;
  margin: auto;
  margin-top: 20px;
  background: var(--secondary-background-color);
  border-radius: 10px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

@media (min-width: 900px) {
  .checkout-page-body {
    grid-template-columns: 2fr 1fr;
  }
}

form > div {
  display: flex;
  flex-direction: column;
}

label {
  font-weight: bold;
  margin-bottom: 0.3rem;
  color: var(--primary-color);
}

input,
select {
  padding: 0.6rem;
  border: 2px solid var(--primary-color);
  border-radius: 6px;
}

.error {
  min-height: 1.2em;
  color: red;
  font-size: 0.85rem;
}

.purchase-info-box {
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
  margin-top: 10px; /* 往下 */
}

.cart-summary-list {
  list-style: none;
  padding: 0;
  margin-top: 20px;
}

.cart-summary-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.cart-summary-spacer {
  height: 30px; /* 調整間距大小，可改成你喜歡的數值 */
}

.cart-item-image {
  width: 60px;
  height: 80px;
  /* object-fit: cover; */
  border-radius: 6px;
  margin-right: 10px;
}

.cart-item-info {
  display: flex;
  flex-direction: column;
}

.cart-item-title {
  font-weight: bold;
  font-size: 0.95rem;
}

.cart-item-quantity,
.cart-item-price {
  font-size: 0.85rem;
  color: #555;
}

hr {
  margin: 10px 0;
}

.total {
  font-weight: bold;
  font-size: 1.1rem;
}


.submit-button {
  padding: 0.8rem;
  border: none;
  border-radius: 8px;
  margin-top: 8px;
  background: var(--secondary-color);
  color: var(--secondary-background-color);
  font-weight: bold;
  cursor: pointer;
}

</style>
