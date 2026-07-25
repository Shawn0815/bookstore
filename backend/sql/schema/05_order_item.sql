-- order_item：訂單品項
-- 依賴 book、`order`，必須在這兩張表之後建立

CREATE TABLE `order_item` (
  `order_item_id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `book_id` int NOT NULL,
  `price` int NOT NULL,
  `quantity` int NOT NULL,
  `amount` int NOT NULL,
  PRIMARY KEY (`order_item_id`),
  KEY `fk_order_item_book` (`book_id`),
  KEY `idx_order_item_order` (`order_id`),
  CONSTRAINT `fk_order_item_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`),
  CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
