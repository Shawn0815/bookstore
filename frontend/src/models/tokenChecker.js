// tokenChecker.js
import ApiService from "@/services/ApiService";
import store from "@/store";
import router from "@/router";

function startTokenCheck(intervalMs = 600000) {
  console.log("Token Checker 已啟動，每", intervalMs / 1000, "秒檢查一次");

  return setInterval(async () => {
    const token = localStorage.getItem("token");
    if (!token) return;

    try {
      console.log("正在檢查 Token...");
      await ApiService.checkToken(); // 後端驗證 Token
    } catch (error) {
      console.log("Token 過期或無效，強制登出", error.message);
      store.commit("LOGOUT");
      router.push({ name: "login" });
    }
  }, intervalMs);
}

export default { startTokenCheck };
