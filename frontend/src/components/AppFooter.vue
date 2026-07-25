<template>
  <footer class="container">
    <section class="copyright">
      <p>2025 Book Galaxy by Shawn Yu</p>
    </section>

    <section class="links">
      <router-link to="/about">about</router-link>
      <router-link to="/">contact</router-link>
      <router-link to="/">directions</router-link>
    </section>

    <section class="links social-media-icons">
      <router-link to="/" class="social-media-icons-link"
        ><i class="fab fa-facebook-square"></i
      ></router-link>
      <router-link to="/" class="social-media-icons-link"
        ><i class="fab fa-instagram"></i
      ></router-link>
      <router-link to="/" class="social-media-icons-link"
        ><i class="fab fa-twitter"></i
      ></router-link>
    </section>
  </footer>
</template>

<script>
export default {
  name: "AppFooter",
};
</script>

<style scoped>
footer {
  background: #a0522d;
  display: grid;
  grid-template-columns: 33% 33% 33%;
  gap: 1em;
  padding: 7px 12px; /* 新增 → 控制上下間距 */
  align-items: center; /* 新增 → 讓三欄內容垂直置中 */
}

.copyright {
  color: #dddddd;
  text-align: left;
  margin-left: 30px;
}

.links {
  display: flex;
  justify-content: center;
  font-size: large;
  flex-direction: row;
  gap: 1em;
}

.links a,
.links router-link {
  color: #ffffff;
  text-decoration: none;
}

.links a:hover,
.links router-link:hover {
  color: #ffd700; /* hover 時金色 */
}

.social-media-icons {
  display: flex;
  justify-content: flex-end; /* 修改：避免warning */
  font-size: large;
  flex-direction: row;
  margin-right: 3em;
  gap: 1.5em;
}

.social-media-icons-link {
  font-size: x-large;
  cursor: pointer;
}

.social-media-icons-link:hover {
  color: #ffd700; /* hover 時金色 */
}
</style>
