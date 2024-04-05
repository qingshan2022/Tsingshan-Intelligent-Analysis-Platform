package com.qingshan.qsbi.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.qingshan.qsbi.model.entity.User;
import com.qingshan.qsbi.utils.SqlUtils;
import com.qingshan.qsbi.common.BaseResponse;
import com.qingshan.qsbi.common.ErrorCode;
import com.qingshan.qsbi.utils.ResultUtils;
import com.qingshan.qsbi.constant.CommonConstant;
import com.qingshan.qsbi.exception.BusinessException;
import com.qingshan.qsbi.mapper.UserMapper;
import com.qingshan.qsbi.model.dto.user.UserQueryRequest;
import com.qingshan.qsbi.model.dto.user.VerifyCodeRegisterRequest;
import com.qingshan.qsbi.model.entity.JwtToken;
import com.qingshan.qsbi.model.enums.UserRoleEnum;
import com.qingshan.qsbi.model.vo.LoginUserVO;
import com.qingshan.qsbi.model.vo.UserVO;
import com.qingshan.qsbi.service.JwtTokensService;
import com.qingshan.qsbi.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import static com.qingshan.qsbi.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现
 *
 * @author <a href="https://github.com/qingshan">青山</a>
 * @from 
 */
//@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码，直接那糊涂包，防止被暴力破解
     */
//    public static final String SALT = "qingshan";

    @Resource
    UserMapper userMapper;
    @Resource
    JwtTokensService jwtTokensService;


    @Override
    public BaseResponse userLoginSaving(String email, HttpServletRequest request) {
        // 查询用户是否存在
        User user = query().eq("email", email).one();
        // 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return null;
    }

    //带token的一系列方法
    private User quickRegister(String email) throws ParseException {
        // 封装信息
        User user = new User();
        // 加密用户密码
        String daName = "user-" + UUID.randomUUID().toString().substring(0, 4);
        user.setUserAccount(daName);
        user.setUserName(daName);
        user.setEmail(email);
        String dateString = "2000-01-01";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = dateFormat.parse(dateString);
        user.setBirth(date);
        String daSecret =  BCrypt.hashpw(email);
        user.setUserPassword(daSecret);
        boolean save = save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败!");
        }
        return user;
    }

    @Override
    public BaseResponse quickLogin(String email) throws ParseException {
        //1. 获取的加密密码
        List<User> users = list(new QueryWrapper<User>().eq("email", email));
        User user;
        if (users == null || users.size() == 0) {
            user = quickRegister(email);
        } else {
            user = users.get(0);
        }
        //3. 获取jwt的token并将token写入Redis
        String token = jwtTokensService.generateAccessToken(user);
        String refreshToken = jwtTokensService.generateRefreshToken(user);
        JwtToken jwtToken = new JwtToken(token, refreshToken);
        jwtTokensService.save2Redis(jwtToken, user);
        return ResultUtils.success(token);
    }


    @Override
    public BaseResponse login(String email, String password) {
        try {
            //1. 获取的加密密码
            User user = query().eq("email", email).one();
            String handlerPassword = user.getUserPassword();
            //1.1 检查用户的使用状态
            if (user.getUserRole().equals(UserRoleEnum.BAN.getValue())) {
                return ResultUtils.error(ErrorCode.PARAMS_ERROR, "该用户已被禁用!");
            }
            //2. 查询用户密码是否正确
            boolean checkpw = BCrypt.checkpw(password, handlerPassword);
            if (!checkpw) {
                return ResultUtils.error(ErrorCode.PARAMS_ERROR, "邮箱或密码错误!");
            }
            //3. 获取jwt的token并将token写入Redis
            String token = jwtTokensService.generateAccessToken(user);
            String refreshToken = jwtTokensService.generateRefreshToken(user);
            JwtToken jwtToken = new JwtToken(token, refreshToken);
            jwtTokensService.save2Redis(jwtToken, user);
            // 返回jwtToken
            return ResultUtils.success(token);
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未注册");
        }
    }

    @Override
    public boolean register(VerifyCodeRegisterRequest request) {
        String password = request.getPassword();
        String email = request.getEmail();
        // 封装信息
        User user = new User();
        // 加密用户密码
        String handlerPassword = BCrypt.hashpw(password);
        String userName = "user-" + UUID.randomUUID().toString().substring(0, 4);
        user.setUserAccount(userName);
        user.setUserName(userName);
        user.setUserPassword(handlerPassword);
        user.setEmail(email);
        boolean save = save(user);
        return save;
    }

//////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }



    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public BaseResponse<UserVO> getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        System.out.println(user);
        if (user == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "用户不存在!");
        }
        // 转换成vo 对象
        UserVO UserVO = BeanUtil.copyProperties(user, UserVO.class);
        return ResultUtils.success(UserVO);
    }

    @Override
    public BaseResponse<Boolean> deleteUserById(Long userId) {
        boolean result = remove(new QueryWrapper<User>().eq("user_id", userId));
        return ResultUtils.success(result);
    }

    @Override
    public BaseResponse addUser(User user) {
        String password = user.getUserPassword();
        String handlerPassword = BCrypt.hashpw(password);
        user.setUserPassword(handlerPassword);
        save(user);
        return ResultUtils.success(user.getId());
    }

}
