package com.qingshan.qsbi.service.impl;


import com.qingshan.qsbi.model.entity.User;
import com.qingshan.qsbi.model.entity.JwtToken;
import com.qingshan.qsbi.service.JwtTokensService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.qingshan.qsbi.constant.JwtConstant.EXPIRATION_TIME;
import static com.qingshan.qsbi.constant.JwtConstant.SECRET_KEY;
import static com.qingshan.qsbi.constant.RedisConstant.ACCESS_TOKEN_PREFIX;
import static com.qingshan.qsbi.constant.RedisConstant.REFRESH_TOKEN_PREFIX;
/**
 * @author adorabled4
 * @className JwtTokensServiceImpl
 * @date : 2023/05/04/ 16:48
 **/
@Service
public class JwtTokensServiceImpl implements JwtTokensService {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public String generateAccessToken(User user) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(user.getId()));
        claims.put("userAvatar", user.getUserAvatar());
        claims.put("userRole", user.getUserRole());
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION_TIME * 1000);
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();

        String key = ACCESS_TOKEN_PREFIX + user.getId();
        stringRedisTemplate.opsForValue().set(key, token, EXPIRATION_TIME, TimeUnit.SECONDS);

        return token;
    }

    @Override
    public String generateRefreshToken(User user) {
        Claims claims = Jwts.claims().setSubject(user.getId().toString());
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION_TIME * 1000);
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();

        String key = REFRESH_TOKEN_PREFIX + user.getId();
        stringRedisTemplate.opsForValue().set(key, token, EXPIRATION_TIME, TimeUnit.SECONDS);
        return token;
    }


    @Override
    public User validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
            String userName = claims.getSubject();
            String userId = getUserIdFromToken(token);
            String userAvatar = (String) claims.get("userAvatar") ;
            String userRole =  (String) claims.get("userRole") ;
            String key = ACCESS_TOKEN_PREFIX + userId;
            String storedToken = stringRedisTemplate.opsForValue().get(key);

            if (storedToken != null && storedToken.equals(token)) {
                // 如果Redis中存储的令牌与传入的令牌匹配，则验证通过
                return new User(Long.parseLong(userId), userName ,userAvatar,userRole);
            }
        } catch (Exception e) {
            // 解析过程中发生异常，验证失败
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    @Override
    public void revokeToken(User user) {
        String accessKey = ACCESS_TOKEN_PREFIX + user.getId();
        String refreshKey = REFRESH_TOKEN_PREFIX + user.getId();
        stringRedisTemplate.delete(accessKey);
        stringRedisTemplate.delete(refreshKey);
    }



    @Override
    public void cleanExpiredTokens() {
        stringRedisTemplate.keys("*").forEach(key -> {
            if (key.startsWith(ACCESS_TOKEN_PREFIX) || key.startsWith(REFRESH_TOKEN_PREFIX)) {
                String token = stringRedisTemplate.opsForValue().get(key);
                if (token != null && isTokenExpired(token)) {
                    stringRedisTemplate.delete(key);
                }
            }
        });
    }

    @Override
    public boolean isTokenExpired(String token) {
        Date expirationDate = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expirationDate.before(new Date());
    }


    @Override
    public void save2Redis(JwtToken jwtToken, User user) {
        String token = jwtToken.getToken();
        String refreshToken =  jwtToken.getRefreshToken();
        String accessKey = ACCESS_TOKEN_PREFIX + user.getId();
        String refreshKey = REFRESH_TOKEN_PREFIX + user.getId();
        stringRedisTemplate.opsForValue().set(accessKey,token,EXPIRATION_TIME, TimeUnit.SECONDS);
        stringRedisTemplate.opsForValue().set(refreshKey,refreshToken,EXPIRATION_TIME, TimeUnit.SECONDS);
    }
}

