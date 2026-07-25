package com.shawnyu.springbootmall.service;

public interface RefreshTokenService {

    String issueRefreshToken(Integer userId);

    // 驗證並撤銷（一次性使用），驗證失敗會拋出 401
    Integer validateAndRevoke(String rawRefreshToken);

    void revoke(String rawRefreshToken);
}
