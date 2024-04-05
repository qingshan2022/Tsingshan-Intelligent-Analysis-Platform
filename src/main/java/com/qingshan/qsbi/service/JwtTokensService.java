package com.qingshan.qsbi.service;


import com.qingshan.qsbi.model.entity.JwtToken;
import com.qingshan.qsbi.model.entity.User;

public interface JwtTokensService  {


    /**
     * 生成JWT访问token
     * @param user
     * @return
     */
    String generateAccessToken(User user);


    /**
     * 生成refreshToken
     * @param user
     * @return
     */
    String generateRefreshToken(User user);


    /**
     * 验证token
     *
     * @param token
     * @return
     */
    User validateToken(String token);

    /**
     * 获取令牌中的用户id
     * @param token
     * @return
     */
    String getUserIdFromToken(String token);

    /**
     * 撤销JWT令牌
     * @param user
     */
    public void revokeToken(User user) ;


    /**
     * 验证token是否过期
     * @param token
     * @return
     */
    boolean isTokenExpired(String token);

    /**
     * 清除过期的令牌
     */
    void cleanExpiredTokens();

    /**
     * 保存token到redis
     * @param jwtToken
     * @param user
     */
    void save2Redis(JwtToken jwtToken, User user);
}

