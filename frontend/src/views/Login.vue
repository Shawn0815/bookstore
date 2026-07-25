<template>
  <div class="login-container">
    <div class="login-card">
      <h2>{{ isLogin ? "登入" : "註冊" }}</h2>

      <form @submit.prevent="handleSubmit" novalidate>
        <!-- 名字欄位 -->
        <div class="form-group" v-if="!isLogin">
          <label>姓名</label>
          <input
            type="text"
            v-model="form.name"
            placeholder="請輸入姓名"
            required
          />
          <small v-if="nameError" class="error-text">{{ nameError }}</small>
        </div>
        
        <!-- 帳號欄位 -->
        <div class="form-group">
          <label>帳號</label>
          <input
            type="email"
            v-model="form.email"
            placeholder="請輸入 Email"
            @input="validateEmail"
            required
          />
          <small v-if="emailError" class="error-text">{{ emailError }}</small>
        </div>

        <!-- 密碼欄位 -->
        <div class="form-group">
          <label>密碼</label>
          <input
            type="password"
            v-model="form.password"
            placeholder="請輸入密碼"
            @input="validatePassword"
            required
          />
          <small v-if="passwordError" class="error-text">{{ passwordError }}</small>
        </div>

        <!-- 後端錯誤訊息 -->
        <small v-if="serverError" class="error-text">{{ serverError }}</small>

        <button type="submit" class="btn">
          {{ isLogin ? "登入" : "註冊" }}
        </button>
      </form>

      <!-- 切換登入/註冊 -->
      <p class="toggle-text">
        {{ isLogin ? "還沒有帳號？" : "已經有帳號？" }}
        <a href="#" @click.prevent="toggleMode">
          {{ isLogin ? "去註冊" : "去登入" }}
        </a>
      </p>
    </div>
  </div>
</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      isLogin: true,
      form: { email: "", password: "" },
      nameError: "",
      emailError: "",
      passwordError: "",
      serverError: "", // 後端錯誤訊息
    };
  },
  methods: {
    toggleMode() {
      this.isLogin = !this.isLogin;
      this.form = { email: "", password: "" };
      this.emailError = "";
      this.passwordError = "";
      this.generalError = "";
    },

    validateName() {
      this.nameError = this.form.name.trim() === "" ? "姓名不可為空白" : "";
    },

    validateEmail() {
      if (!this.form.email) {
        this.emailError = "帳號不可為空白";
      } else {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        this.emailError = !emailRegex.test(this.form.email) ? "請輸入正確的 Email 格式" : "";
      }
    },

    validatePassword() {
      if (!this.form.password) {
        this.passwordError = "密碼不可為空白";
      } else {
        const password = this.form.password;
        const lengthValid = password.length >= 8;
        const upperValid = /[A-Z]/.test(password);
        const lowerValid = /[a-z]/.test(password);
        const digitValid = /[0-9]/.test(password);

        if (!lengthValid) {
          this.passwordError = "密碼至少 8 碼";
        } else if (!upperValid || !lowerValid || !digitValid) {
          this.passwordError = "密碼必須包含大小寫字母與數字";
        } else {
          this.passwordError = "";
        }
      }
    },

    handleSubmit() {
      if (!this.isLogin) this.validateName();
      this.validateEmail();
      this.validatePassword();

      if (this.nameError || this.emailError || this.passwordError) {
        this.generalError = "請修正表單錯誤後再送出";
        return;
      }

      if (this.isLogin) {
        this.$store.dispatch("login", {
          email: this.form.email,
          password: this.form.password,
        })
          .then(() => {
            alert("登入成功");
            this.$router.push("/");
          })
          .catch((err) => {
            console.error(err);
            this.generalError = err.message || "登入失敗";
          });
      } else {
        this.$store.dispatch("register", {
          name: this.form.name,
          email: this.form.email,
          password: this.form.password,
        })
          .then(() => {
            alert("註冊成功");
            this.$router.push("/");
          })
          .catch((err) => {
            console.error(err);
            this.generalError = err.message || "註冊失敗";
          });
      }
    },
  },
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  padding: 2rem;
  margin-top: 100px;
}

.login-card {
  background: white;
  padding: 2rem;
  border-radius: 10px;
  width: 320px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  font-weight: bold;
  margin-bottom: 0.3rem;
}

input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 5px;
}

.btn {
  width: 100%;
  padding: 0.7rem;
  background-color: #667eea;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 1rem;
  cursor: pointer;
}

.btn:hover {
  background-color: #5a67d8;
}

.toggle-text {
  text-align: center;
  margin-top: 1rem;
}

.toggle-text a {
  color: #667eea;
  text-decoration: none;
  font-weight: bold;
}

.toggle-text a:hover {
  text-decoration: underline;
}

.error-text {
  color: red;
  font-size: 0.85rem;
  margin-top: 0.3rem;
}
</style>
