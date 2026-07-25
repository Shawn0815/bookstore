import os
import sys
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from bs4 import BeautifulSoup
import time
import json
import mysql.connector
from datetime import datetime
import re

sys.stdout.reconfigure(encoding='utf-8')

# ===== 設定 MYSQL Driver =====
db_config = {
    "host": "localhost",
    "user": "root",
    "password": "springboot",
    "database": "bookshop"
}

# ===== 設定書籍 url =====
if len(sys.argv) < 2:
    print("請提供書籍 URL")
    sys.exit(1)

BOOK_URL = sys.argv[1]

# BOOK_URL = "https://www.eslite.com/product/1001122732854211"

if not BOOK_URL:
    print("❌ 未提供書籍 URL")
    sys.exit(1)

# ===== 設定 Chrome Driver =====
options = Options()
options.add_argument("--headless=new")  # 新的 headless 模式更安靜
options.add_argument("--disable-gpu")
options.add_argument("--window-size=1920,1080")
options.add_argument("--log-level=3")
options.add_argument("--remote-debugging-port=0")
options.add_experimental_option("excludeSwitches", ["enable-logging"])

service = Service(log_path=os.devnull)  # 重定向 chromedriver log

driver = webdriver.Chrome(service=service, options=options)

driver.get(BOOK_URL)
time.sleep(3)  # 等 JS 載入完成

html = driver.page_source
driver.quit()

soup = BeautifulSoup(html, "html.parser")

# ===== JSON-LD 抓取 =====
json_ld_tag = soup.find("script", {"type": "application/ld+json"})
if not json_ld_tag:
    print("❌ 找不到 JSON-LD \n")
    sys.exit()

try:
    data = json.loads(json_ld_tag.string)
except Exception as e:
    print("❌ JSON 解析錯誤:", e, "\n")
    sys.exit()

book_data = data[0] if isinstance(data, list) else data

# 書名（移除括號副標）
title = re.sub(r"（.*?）|\(.*?\)", "", book_data.get("name", "")).strip()

author = book_data.get("author", {}).get("name", "")
publisher = book_data.get("publisher", {}).get("name", "")
published_date = book_data.get("datePublished", "")

# ===== 出版日期：若 JSON-LD 沒抓到則用 HTML =====
if not published_date:
    try:
        wait = WebDriverWait(driver, 5)
        date_element = wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, "div.publicDate span")))
        if date_element:
            published_date = date_element.text.strip()
    except Exception:
        published_date = ""

if published_date:
    published_date = published_date.replace("/", "-")
    try:
        published_date = datetime.strptime(published_date, "%Y-%m-%d").date()
    except Exception:
        try:
            published_date = datetime.strptime(published_date, "%Y-%m").date()
        except Exception:
            published_date = None
else:
    published_date = None

price = book_data.get("offers", {}).get("price", 0)
availability = book_data.get("offers", {}).get("availability", "")
image_url = book_data.get("image", "")

# ===== description =====
description = ""

# 先用 HTML 控制換行的方式抓
desc_tag = soup.find("div", id="product-page-introduction")
if desc_tag:
    paragraphs = []
    for p in desc_tag.find_all("p"):
        br_texts = []
        for elem in p.descendants:
            if isinstance(elem, str):
                br_texts.append(elem)
            elif elem.name == "br":
                br_texts.append("\n")
        paragraph_text = "".join(br_texts).strip()
        if paragraph_text:
            paragraphs.append(paragraph_text)
    description = "\n\n".join(paragraphs).lstrip("\n")
    description = re.split(r"(退貨須知[:：]|\n退貨須知)", description)[0].strip()

# 如果 HTML 沒抓到，再用 JSON-LD 裡的 description
if not description:
    description = book_data.get("description", "").strip()

# ===== category =====
category = ""
if len(data) > 1 and "itemListElement" in data[1]:
    categories = [x.get("name", "") for x in data[1]["itemListElement"]]
    if len(categories) > 1:
        category = categories[1]
    elif categories:
        category = categories[0]

stock = 10 if "InStock" in availability else 0
sales_count = 0
now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

# ===== 寫入資料庫 =====
try:
    conn = mysql.connector.connect(**db_config)
    cursor = conn.cursor()

    sql = """
    INSERT INTO book (
        title, author, publisher, published_date, price, category,
        image_url, description, stock, sales_count, created_date,
        last_modified_date, original_url
    ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
    ON DUPLICATE KEY UPDATE
        author=VALUES(author),
        publisher=VALUES(publisher),
        published_date=VALUES(published_date),
        price=VALUES(price),
        category=VALUES(category),
        image_url=VALUES(image_url),
        description=VALUES(description),
        stock=VALUES(stock),
        sales_count=VALUES(sales_count),
        last_modified_date=VALUES(last_modified_date),
        original_url=VALUES(original_url)
    """
    values = (
        title, author, publisher, published_date, price, category,
        image_url, description, stock, sales_count, now, now, BOOK_URL
    )

    cursor.execute(sql, values)
    conn.commit()

    if cursor.rowcount == 1:
        print(f"\n✅ 成功存入資料庫: {title}\n")
    else:
        print(f"\n🔄 成功更新資料庫: {title}\n")

except Exception as e:
    print("\n❌ 資料庫存取錯誤:", e, "\n")

finally:
    if cursor:
        cursor.close()
    if conn:
        conn.close()
