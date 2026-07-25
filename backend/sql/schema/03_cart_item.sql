-- cart_item：購物車項目
-- 依賴 user、book，必須在這兩張表之後建立

CREATE TABLE `cart_item` (
  `cart_item_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `book_id` int NOT NULL,
  `quantity` int NOT NULL,
  PRIMARY KEY (`cart_item_id`),
  UNIQUE KEY `uk_cart_user_book` (`user_id`,`book_id`),
  KEY `fk_cart_item_book` (`book_id`),
  CONSTRAINT `fk_cart_item_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`),
  CONSTRAINT `fk_cart_item_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
