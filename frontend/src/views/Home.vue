<template>
  <section class="hero">
    <img
      src="@/assets/images/site/bookstore.jpg"
      alt="Bookstore"
      class="hero-img"
    />
    <div class="hero-text">
      <h1>
        <router-link :to="{ name: 'books' }">
          Start your journey with us
        </router-link>
      </h1>
    </div>
  </section>
</template>

<script>
export default { name: "Home" };
</script>

<style scoped>
.hero {
  position: relative;
  flex: 1; /* 填滿 header 和 footer 之間的空間 */
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

/* 大圖鋪滿 Hero 區域 */
.hero-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 0;
}

/* 漸層 */
.hero::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(rgba(0, 0, 0, 0.1), rgba(0, 0, 0, 0.1));
  z-index: 5;
}

/* 文字置中 */
.hero-text {
  position: absolute;
  top: 25%; /* 上移一點 */
  left: 50%;
  transform: translateX(-50%);
  text-align: center;

  font-size: 2rem;
  font-weight: 400;
  font-family: "Georgia", serif;
  color: #fff;

  /* 讓文字有陰影，避免背景太亮時消失 */
  text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.8);
  z-index: 10; /* 確保蓋在圖片上 */
}

.hero-text h1 {
  font-family: "Crimson Text", serif; /* 文青書香感字體 */
  font-size: 3.5rem;
  font-style: italic;
  font-weight: 600;
  color: #fdfdfd;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.5);
}

.hero-text h1 a {
  text-decoration: none; /* 移除底線 */
  color: inherit; /* 跟 h1 一樣的顏色 */
}
</style>
