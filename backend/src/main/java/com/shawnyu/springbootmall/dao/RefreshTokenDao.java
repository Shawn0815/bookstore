package com.shawnyu.springbootmall.dao;

import com.shawnyu.springbootmall.model.RefreshToken;

import java.util.Date;

public interface RefreshTokenDao {

    Integer createRefreshToken(Integer userId, String tokenHash, Date expiryDate);

    RefreshToken getByTokenHash(String tokenHash);

    // 原子操作：檢查未撤銷且未過期，同時標記為已撤銷，回傳影響筆數
    int validateAndRevoke(String tokenHash);

    int revokeByTokenHash(String tokenHash);
}
