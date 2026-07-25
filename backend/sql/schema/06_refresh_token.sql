-- refresh_token：Refresh Token 資料表
-- 依賴 user，必須在 user 之後建立
-- token_hash 存的是 raw refresh token 的 SHA-256 雜湊值，不存明文；
-- revoked 標記這支 token 是否已作廢（換新過一次或登出後即撤銷，防止 replay）

CREATE TABLE `refresh_token` (
  `refresh_token_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `token_hash` char(64) NOT NULL COMMENT 'raw refresh token 的 SHA-256 十六進位雜湊值',
  `expiry_date` datetime NOT NULL,
  `revoked` tinyint(1) NOT NULL DEFAULT '0',
  `created_date` datetime NOT NULL,
  `last_modified_date` datetime NOT NULL,
  PRIMARY KEY (`refresh_token_id`),
  UNIQUE KEY `uk_refresh_token_hash` (`token_hash`),
  KEY `idx_refresh_token_user_id` (`user_id`),
  CONSTRAINT `fk_refresh_token_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
