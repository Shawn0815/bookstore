package com.shawnyu.springbootmall.dao.impl;

import com.shawnyu.springbootmall.dao.RefreshTokenDao;
import com.shawnyu.springbootmall.model.RefreshToken;
import com.shawnyu.springbootmall.rowmapper.RefreshTokenRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RefreshTokenDaoImpl implements RefreshTokenDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Integer createRefreshToken(Integer userId, String tokenHash, Date expiryDate) {
        String sql = "INSERT INTO refresh_token(user_id, token_hash, expiry_date, revoked, created_date, last_modified_date) " +
                "VALUES (:userId, :tokenHash, :expiryDate, 0, :now, :now)";

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("tokenHash", tokenHash);
        map.put("expiryDate", expiryDate);
        map.put("now", new Date());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);

        return keyHolder.getKey().intValue();
    }

    @Override
    public RefreshToken getByTokenHash(String tokenHash) {
        String sql = "SELECT refresh_token_id, user_id, token_hash, expiry_date, revoked, created_date, last_modified_date " +
                "FROM refresh_token WHERE token_hash = :tokenHash";

        Map<String, Object> map = new HashMap<>();
        map.put("tokenHash", tokenHash);

        List<RefreshToken> refreshTokenList = namedParameterJdbcTemplate.query(sql, map, new RefreshTokenRowMapper());

        if (refreshTokenList.size() <= 0) {
            return null;
        }

        return refreshTokenList.get(0);
    }

    @Override
    public int validateAndRevoke(String tokenHash) {
        // 原子操作：同時檢查「未撤銷且未過期」與「標記為已撤銷」，
        // 避免同一個 refresh token 被併發使用兩次（replay / race condition）
        String sql = "UPDATE refresh_token SET revoked = 1, last_modified_date = :now " +
                "WHERE token_hash = :tokenHash AND revoked = 0 AND expiry_date > :now";

        Map<String, Object> map = new HashMap<>();
        map.put("tokenHash", tokenHash);
        map.put("now", new Date());

        return namedParameterJdbcTemplate.update(sql, map);
    }

    @Override
    public int revokeByTokenHash(String tokenHash) {
        String sql = "UPDATE refresh_token SET revoked = 1, last_modified_date = :now " +
                "WHERE token_hash = :tokenHash AND revoked = 0";

        Map<String, Object> map = new HashMap<>();
        map.put("tokenHash", tokenHash);
        map.put("now", new Date());

        return namedParameterJdbcTemplate.update(sql, map);
    }
}
