import sys
import subprocess
import time
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from bs4 import BeautifulSoup

if len(sys.argv) < 3:
    print("用法: python scrape_category.py <CATEGORY_NAME> <CATEGORY_URL>")
    sys.exit(1)

CATEGORY_NAME = sys.argv[1]
CATEGORY_URL = sys.argv[2]
LIMIT = 10  # 限制抓取書的總數

options = Options()
options.add_argument("--headless")
options.add_argument("--disable-gpu")
options.add_argument("--window-size=1920,1080")

driver = webdriver.Chrome(options=options)

def crawl_book_urls(category_url, limit=None):
    driver.get(category_url)
    time.sleep(3)

    book_urls = set()
    page_number = 1

    while True:
        html = driver.page_source
        soup = BeautifulSoup(html, "html.parser")

        for a_tag in soup.select("a[href^='/product/']"):
            href = a_tag.get("href")
            if href and href.startswith("/product/"):
                full_url = "https://www.eslite.com" + href
                book_urls.add(full_url)

        print(f"\n第 {page_number} 頁抓到 {len(book_urls)} 本書 \n")
        
        if page_number == 1:
            limit = len(book_urls)

        next_page = soup.select_one("a.next")
        if next_page and "href" in next_page.attrs:
            driver.get("https://www.eslite.com" + next_page.attrs["href"])
            page_number += 1
            time.sleep(3)
        else:
            break

        if limit and len(book_urls) >= limit:
            break

    return list(book_urls)[:limit] if limit else list(book_urls)


if __name__ == "__main__":
    urls = crawl_book_urls(CATEGORY_URL, limit=LIMIT)
    print(f"總共抓取 {len(urls)} 本書網址\n")

    new_books_count = 0

    for idx, u in enumerate(urls, start=1):
        print(f"🕸️  正在爬取第 {idx} 本書：{u} ")
        result = subprocess.run(["python", "scrape_book.py", u], capture_output=True, text=True, encoding="utf-8")

        output = result.stdout.strip()
        print(output)
        print(result.stderr)  # 加印錯誤輸出，確保不漏 log

        if "成功存入資料庫" in output:
            new_books_count += 1
            print(f"📖 目前新增書數：{new_books_count} 本 \n")
        elif "成功更新資料庫" in output:
            print(f"📖 目前新增書數：{new_books_count} 本 \n")
        else:
            print(f"📛 發生錯誤，資料未更新/新增 \n")
            print(f"📖 目前新增書數：{new_books_count} 本 \n")

    print(f"📌 最終結果：{CATEGORY_NAME} 分類總共抓取 {len(urls)} 本書，新增 {new_books_count} 本書 \n")
    print(f"=====================================================================================")
    driver.quit()
