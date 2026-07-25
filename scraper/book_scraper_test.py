import requests
from bs4 import BeautifulSoup
import json

url = "https://www.eslite.com/product/10012020052683003866002"

headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36"
}

resp = requests.get(url, headers=headers)
soup = BeautifulSoup(resp.text, "html.parser")

# 抓 <script type="application/ld+json">
json_ld = soup.find("script", {"type": "application/ld+json"})
data = json.loads(json_ld.string)

book_data = data[0]

title = book_data.get("name", "")
author = book_data.get("author", {}).get("name", "")
publisher = book_data.get("publisher", {}).get("name", "")
published_date = book_data.get("datePublished", "")
price = book_data.get("offers", {}).get("price", "")
price_currency = book_data.get("offers", {}).get("priceCurrency", "")
category = [x.get("name", "") for x in data[1]["itemListElement"]]
image_url = book_data.get("image", "")
description = book_data.get("description", "")
availability = book_data.get("offers", {}).get("availability", "")

print("書名:", title)
print("作者:", author)
print("出版社:", publisher)
print("出版日期:", published_date)
print("價格:", price, price_currency)
print("類別:", category)
print("圖片:", image_url)
print("描述:", description[:100], "...")
print("庫存:", availability)
