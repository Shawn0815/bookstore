from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from bs4 import BeautifulSoup
import time

# ==== 設定 ====
category_url = "https://www.eslite.com/category/2/28"  # 文學分類頁 URL
options = Options()
options.add_argument("--headless")
options.add_argument("--disable-gpu")
options.add_argument("--window-size=1920,1080")

driver = webdriver.Chrome(options=options)
driver.get(category_url)

book_urls = set()
page_number = 1

while True:
    time.sleep(3)  # 等待 JavaScript 渲染
    html = driver.page_source
    soup = BeautifulSoup(html, "html.parser")

    # 抓取書籍連結
    books = soup.select("a[href*='/product/']")
    for book in books:
        link = book.get("href")
        if link and link.startswith("/product/"):
            book_urls.add("https://www.eslite.com" + link)

    print(f"第 {page_number} 頁抓到 {len(books)} 本書")

    # 嘗試翻頁
    next_button = soup.select_one("a.next")
    if next_button and "disabled" not in next_button.get("class", []):
        page_number += 1
        next_page_url = "https://www.eslite.com" + next_button.get("href")
        driver.get(next_page_url)
    else:
        break

driver.quit()

print(f"總共找到 {len(book_urls)} 本書")
with open("book_urls.txt", "w", encoding="utf-8") as f:
    for url in book_urls:
        f.write(url + "\n")

print("書籍 URL 已儲存到 book_urls.txt")
