package com.shawnyu.springbootmall.model;

import java.util.Date;

public class RefreshToken {

    private Integer refreshTokenId;
    private Integer userId;
    private String tokenHash;
    private Date expiryDate;
    private Boolean revoked;
    private Date createdDate;
    private Date lastModifiedDate;

    public Integer getRefreshTokenId() {
        return refreshTokenId;
    }

    public void setRefreshTokenId(Integer refreshTokenId) {
        this.refreshTokenId = refreshTokenId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Boolean getRevoked() {
        return revoked;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
