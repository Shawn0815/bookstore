-- book：書籍資料表
-- 沒有外鍵依賴，但 cart_item/order_item 會參照它，所以要在它們之前建立
-- 三個 idx_book_category_* 複合索引是為了 category + 排序(價格/上架時間/銷量) 這個查詢情境而加的

CREATE TABLE `book` (
  `book_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `author` varchar(255) NOT NULL,
  `isbn` varchar(20) NOT NULL,
  `publisher` varchar(255) DEFAULT NULL,
  `published_date` date DEFAULT NULL,
  `price` int NOT NULL,
  `category` varchar(100) NOT NULL,
  `image_url` varchar(500) DEFAULT NULL,
  `description` text,
  `stock` int NOT NULL DEFAULT '0',
  `sales_count` int NOT NULL DEFAULT '0',
  `created_date` datetime NOT NULL,
  `last_modified_date` datetime NOT NULL,
  `original_url` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`book_id`),
  UNIQUE KEY `isbn` (`isbn`),
  KEY `idx_book_category_price` (`category`,`price`,`book_id`),
  KEY `idx_book_category_published_date` (`category`,`published_date`,`book_id`),
  KEY `idx_book_category_sales_count` (`category`,`sales_count`,`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
