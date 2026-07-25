<template>
  <div id="app">
    <app-header></app-header>

    <!-- 中間的 main 容器，負責撐開剩下空間 -->
    <main class="main-container">
      <router-view id="router-view" :key="$route.fullPath"></router-view>
    </main>

    <app-footer></app-footer>
  </div>
</template>

<script>
import AppHeader from "@/components/AppHeader";
import AppFooter from "@/components/AppFooter";

export default {
  name: "App",
  components: {
    AppHeader,
    AppFooter,
  },
  created: function () {
    this.$store.dispatch("fetchCategories");
    this.$store.dispatch("fetchCart");
  },
};
</script>

<style>
html,
body,
#app {
  height: 100%;
  margin: 0;
}

#app {
  display: flex;
  flex-direction: column;
  min-height: 100vh; /* 保證至少全螢幕高 */
}

/* 原本用 app-header / app-footer 標籤選取器完全選不到東西
   （Vue 會把自訂元件標籤換成該元件模板的根元素），改成用結構選取器 */
#app > header {
  flex-shrink: 0; /* Header 固定高度 */
}

.main-container {
  flex: 1; /* 撐開剩餘空間 */
  display: flex;
  flex-direction: column;
  background-color: var(--secondary-background-color);
}

#app > footer {
  flex-shrink: 0; /* Footer 固定在最下方 */
}
</style>
