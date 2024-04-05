package com.qingshan.qsbi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.text.ParseException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import com.qingshan.qsbi.model.entity.User;
import com.qingshan.qsbi.common.BaseResponse;
import com.qingshan.qsbi.model.dto.user.UserQueryRequest;
import com.qingshan.qsbi.model.dto.user.VerifyCodeRegisterRequest;
import com.qingshan.qsbi.model.vo.LoginUserVO;
import com.qingshan.qsbi.model.vo.UserVO;

/**
 * 用户服务
 *
 * @author <a href="https://github.com/qingshan">青山</a>
 * @from 
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录状态保存
     *
     * @param request 网络请求
     * @return {@link BaseResponse}
     */
    BaseResponse userLoginSaving(String email, HttpServletRequest request);

    /**
     * 快速登录
     *
     * @param email 电子邮件
     * @return {@link BaseResponse}
     */
    BaseResponse quickLogin(String email) throws ParseException;

    /**
     * 用户登录
     *
     * @param email 账户
     * @param userPassword    密码
     * @return 返回token
     */
    BaseResponse login(String email, String userPassword);

    /**
     * 通过邮箱注册
     *
     * @param request 请求
     * @return boolean
     */
    boolean register(VerifyCodeRegisterRequest request);



    /////////////////////////////////////////////////////////

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 根据id查询用户
     *
     * @param userId
     * @return
     */
    BaseResponse<UserVO> getUserById(Long userId);
    /**
     * 根据id删除用户
     *
     * @param userId
     * @return
     */
    BaseResponse<Boolean> deleteUserById(Long userId);

    /**
     * 添加用户
     *
     * @param user
     * @return
     */
    BaseResponse addUser(User user);
}
