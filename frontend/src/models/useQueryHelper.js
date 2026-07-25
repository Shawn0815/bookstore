// 共用工具：處理路由的 query 參數
// 把重複邏輯（CategoryDropdown、SortDropdown 的 getQuery）抽出來

export default function getQueryTemplate(route, newQuery) {
  // 複製當前 URL 的 query 參數
  const filteredQuery = { ...route.query };

  // 處理 newQuery 的 key-value
  Object.keys(newQuery).forEach((key) => {
    if (newQuery[key] === null || newQuery[key] === "") {
      // 如果值是 null 或空字串 → 從 URL 移除這個參數
      delete filteredQuery[key];
    } else {
      // 否則 → 更新 / 新增
      filteredQuery[key] = newQuery[key];
    }
  });

  // 換分類或排序時 → 頁碼要回到第一頁
  if (filteredQuery.page && filteredQuery.page !== 1) {
    filteredQuery.page = 1;
  }

  return filteredQuery;
}
