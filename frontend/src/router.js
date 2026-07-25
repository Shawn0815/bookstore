import Vue from "vue";
import Router from "vue-router";
import Home from "./views/Home.vue";
import BookListPage from "./views/BookListPage.vue";
import Cart from "./views/Cart";
import Checkout from "./views/Checkout";
import MyOrder from "./views/MyOrder.vue";
import NotFound from "./views/NotFound.vue";
import BookDetail from "@/views/BookDetail.vue";
import Login from "@/views/Login.vue";

Vue.use(Router);

export default new Router({
  mode: "history",
  base: process.env.BASE_URL,
  routes: [
    {
      path: "/",
      name: "home",
      component: Home,
      alias: ["/home", "/index.html"],
    },
    {
      // 所有書籍都同個路由，差在後面參數（用 query 取得）
      path: "/books",
      name: "books",
      component: BookListPage,
    },
    {
      path: "/books/:id", // 動態路由，id 為書籍編號
      name: "bookDetail",
      component: BookDetail,
    },
    {
      path: "/cart",
      name: "cart",
      component: Cart,
    },
    {
      path: "/login",
      name: "login",
      component: Login,
    },
    {
      path: "*",
      component: NotFound,
    },
    {
      path: "/checkout",
      name: "checkout",
      component: Checkout,
    },
    {
      path: "/myorder/:type",
      name: "myorder",
      component: MyOrder
    },
  ],
});
