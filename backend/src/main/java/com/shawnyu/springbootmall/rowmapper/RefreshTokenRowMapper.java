package com.shawnyu.springbootmall.rowmapper;

import com.shawnyu.springbootmall.model.RefreshToken;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RefreshTokenRowMapper implements RowMapper<RefreshToken> {

    @Override
    public RefreshToken mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setRefreshTokenId(resultSet.getInt("refresh_token_id"));
        refreshToken.setUserId(resultSet.getInt("user_id"));
        refreshToken.setTokenHash(resultSet.getString("token_hash"));
        refreshToken.setExpiryDate(resultSet.getTimestamp("expiry_date"));
        refreshToken.setRevoked(resultSet.getBoolean("revoked"));
        refreshToken.setCreatedDate(resultSet.getTimestamp("created_date"));
        refreshToken.setLastModifiedDate(resultSet.getTimestamp("last_modified_date"));

        return refreshToken;
    }
}
