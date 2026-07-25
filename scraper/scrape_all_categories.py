import subprocess
import time

# === 這裡列出你要爬的分類 URL 與中文名稱 ===
CATEGORY_MAP = {
    "https://www.eslite.com/category/2/28": "文學",
    # "https://www.eslite.com/category/2/61": "人文史哲",
    # "https://www.eslite.com/category/2/80": "商業財經",
    # "https://www.eslite.com/category/2/137": "動漫畫/圖文",
    # "https://www.eslite.com/category/2/45": "輕小說",
    # "https://www.eslite.com/category/2/110": "電腦",
    # "https://www.eslite.com/category/2/119": "自然科普",
    # "https://www.eslite.com/category/2/125": "旅遊",
    # "https://www.eslite.com/category/2/153": "醫學保健",
    # "https://www.eslite.com/category/2/9": "生活風格",
    # "https://www.eslite.com/category/2/141": "飲食",
    # "https://www.eslite.com/category/2/48540": "美術"
}

for url, category_name in CATEGORY_MAP.items():
    print(f"\n開始抓取分類：{category_name} ({url})")

    try:
        # 呼叫 scrape_category.py，並把 URL 與限制數量傳進去
        subprocess.run(
            ["python", "scrape_category.py", category_name, url],
            check=True
        )
    except subprocess.CalledProcessError as e:
        print(f"分類 {category_name} 抓取失敗: {e}")

    # 每個分類之間停一下，避免被網站封鎖
    time.sleep(5)

print("\n================================ 所有分類處理完成 ===================================")