package com.qingshan.qsbi.aop;


import cn.hutool.core.bean.BeanUtil;
import com.qingshan.qsbi.model.entity.User;
import com.qingshan.qsbi.common.ErrorCode;
import com.qingshan.qsbi.constant.JwtConstant;
import com.qingshan.qsbi.constant.RedisConstant;
import com.qingshan.qsbi.exception.BusinessException;
import com.qingshan.qsbi.model.dto.user.UserDTO;
import com.qingshan.qsbi.service.JwtTokensService;
import com.qingshan.qsbi.utils.UserHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * @author adorabled4
 * @className ReFreshTokenInterceptor
 * @date : 2023/05/04/ 17:03
 **/
public class ReFreshTokenInterceptor implements HandlerInterceptor {

    JwtTokensService jwtTokensService;

    StringRedisTemplate stringRedisTemplate;


    public ReFreshTokenInterceptor(StringRedisTemplate stringRedisTemplate, JwtTokensService jwtTokensService){
        this.jwtTokensService=jwtTokensService;
        this.stringRedisTemplate=stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization"); //从请求头中获取JWT access_token
        if(StringUtils.isEmpty(token)){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"missing jwt token");
        }
        try {
            // 解析并验证JWT token是否合法
            boolean isTokenExpired = jwtTokensService.isTokenExpired(token);
            User user = jwtTokensService.validateToken(token);
            if(isTokenExpired){
                // 如果token过期 , 那么需要通过refresh_token生成一个新的access_token
                String refreshTokenKey = RedisConstant.REFRESH_TOKEN_PREFIX+ user.getId();
                String refreshToken = stringRedisTemplate.opsForValue().get(refreshTokenKey);
                if(StringUtils.isEmpty(refreshToken)){
                    throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"missing refresh token");
                }
                if(jwtTokensService.isTokenExpired(refreshToken)){
                    throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"超时, 请重新登录");
                }
                // 生成新的accessToken , 同时保存到redis
                String accessToken = jwtTokensService.generateAccessToken(user);
                String accessTokenKey = RedisConstant.ACCESS_TOKEN_PREFIX +user.getId();
                stringRedisTemplate.opsForValue().set(accessTokenKey,accessToken,
                        JwtConstant.EXPIRATION_TIME, TimeUnit.SECONDS);

                response.setHeader("Authorization",accessToken);
                // 更新token这个动作在用户看来是未知的, 更新完之后需要在ThreadLocal中添加UserDTO
                UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
                UserHolder.setUser(userDTO);
            }else{
                // 如果token没有过期, 那么直接添加用户的数据
                UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
                UserHolder.setUser(userDTO);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token");
        }
    }
}
