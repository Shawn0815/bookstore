import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import "@/assets/css/global.css";
import store, { CART_STORAGE_KEY, ORDER_DETAIL_STORAGE_KEY } from "./store";
import Vuelidate from "vuelidate";
import TreeView from "vue-json-tree-view";
import tokenChecker from "./models/tokenChecker";

Vue.config.productionTip = false;
Vue.use(Vuelidate);
Vue.use(TreeView);

tokenChecker.startTokenCheck(60000); // 每分鐘檢查一次

new Vue({
  router,
  store,
  render: function (h) {
    return h(App);
  },
  created() {
    const cartString = localStorage.getItem(CART_STORAGE_KEY);
    if (cartString) {
      const shoppingCart = JSON.parse(cartString);
      this.$store.commit("SET_CART", shoppingCart);
    }
    const orderDetailString = sessionStorage.getItem(ORDER_DETAIL_STORAGE_KEY);
    if (orderDetailString) {
      const orderDetailData = JSON.parse(orderDetailString);
      this.$store.commit("SET_ORDER_DETAILS", orderDetailData);
    }
    // From https://flaviocopes.com/how-to-format-number-as-currency-javascript/
    const PriceFormatter = new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
      minimumFractionDigits: 0,
    });

    Vue.filter("asDollarsAndCents", function (cents) {
      return PriceFormatter.format(cents);
    });
  },
}).$mount("#app");
