-- order：訂單主表
-- 依賴 user；注意 `order` 是 MySQL 保留字，建表與查詢時都要用反引號包起來
-- 依賴 user，必須在 user 之後建立

CREATE TABLE `order` (
  `order_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `total_amount` int NOT NULL,
  `created_date` datetime NOT NULL,
  `last_modified_date` datetime NOT NULL,
  PRIMARY KEY (`order_id`),
  KEY `idx_order_user_created` (`user_id`,`created_date`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
