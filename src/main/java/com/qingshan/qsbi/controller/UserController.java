package com.qingshan.qsbi.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingshan.qsbi.annotation.AuthCheck;
import com.qingshan.qsbi.annotation.SysLog;
import com.qingshan.qsbi.common.BaseResponse;
import com.qingshan.qsbi.common.DeleteRequest;
import com.qingshan.qsbi.common.ErrorCode;
import com.qingshan.qsbi.manager.OssManager;
import com.qingshan.qsbi.model.dto.FileUploadResult;
import com.qingshan.qsbi.utils.ResultUtils;
import com.qingshan.qsbi.constant.RedisConstant;
import com.qingshan.qsbi.constant.UserConstant;
import com.qingshan.qsbi.exception.BusinessException;
import com.qingshan.qsbi.exception.ThrowUtils;
import com.qingshan.qsbi.model.dto.user.*;
import com.qingshan.qsbi.model.entity.User;
import com.qingshan.qsbi.model.enums.FileUploadBizEnum;
import com.qingshan.qsbi.model.enums.MailEnum;
import com.qingshan.qsbi.model.vo.UserVO;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.qingshan.qsbi.service.MessageService;
import com.qingshan.qsbi.service.UserService;
import com.qingshan.qsbi.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * 用户接口
 *
 * @author <a href="https://github.com/liqingshan">青山</a>
 *
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    MessageService messageService;

    @Resource
    OssManager ossManager;
//带有token的一套新的登录方式
    // region 登录相关
    @PostMapping("/login/email")
    @Operation(summary="用户登录-email")
    public BaseResponse login(@Valid @RequestBody LoginEmailRequest param, HttpServletRequest request) {
        if (param == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String email = param.getEmail();
        String password = param.getPassword();
        if (password == null || email == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "用户名或密码不能为空!");
        }

        BaseResponse userLoginSaving = userService.userLoginSaving(email, request);
        return userService.login(email, password);
    }

    @PostMapping("/login/email/quick")
    @Operation(summary="验证码快速登录-email")
    public BaseResponse quickLogin(@Valid @RequestBody QuickLoginEmailRequest param) throws ParseException {
        if (param == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String email = param.getEmail();
        String code = param.getCode();
        verifyCode(code, email);
        return userService.quickLogin(email);
    }
    @PostMapping("/register/email")
    @Operation(summary="用户注册-email")
    public BaseResponse registerByEmail(@Valid @RequestBody VerifyCodeRegisterRequest request) {
        if (request == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String email = request.getEmail();
        String code = request.getCode();
        String codeKey = RedisConstant.VERIFY_CODE_KEY + email;
        String realCodeValue = stringRedisTemplate.opsForValue().get(codeKey);
        String[] split = realCodeValue.split("-");
        String verifyCode = split[1];
        if (!verifyCode.equals(code)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "验证码错误!");
        }
        userService.register(request);
        return ResultUtils.success("注册成功!");
    }

    /**
     * 发送验证码
     *
     * @param email 电子邮件
     * @return {@link BaseResponse}<{@link String}>
     */
    @GetMapping("/send/code")
    public BaseResponse<String> sendVerifyCode(@RequestParam("email") String email) {
        // 校验
        if (!email.matches(UserConstant.EMAIL_REGEX)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "邮箱格式非法!");
        }
        Long cnt = userService.query().eq("email", email).count();
        if (cnt != 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "该邮箱已被使用!");
        }
        // 发送验证码
        String code = RandomUtil.randomNumbers(6);
        String codeKey = RedisConstant.VERIFY_CODE_KEY + email;
        // 校验是否允许发送
        verifyCodeSend(email);
        // 发送
        messageService.sendCode(email, code, MailEnum.VERIFY_CODE);
        // 保存验证码的有效期
        long now = new Date().getTime() / 1000;
        String codeVal = now + "-" + code;
        // 存储
        stringRedisTemplate.opsForValue().set(codeKey, codeVal, RedisConstant.VERIFY_CODE_TTL, TimeUnit.SECONDS);
        return ResultUtils.success("发送成功!");
    }


    //根据id增删查用户
    @GetMapping("/{id}")
    @Operation(summary="通过用户id获取用户信息")
    public BaseResponse<UserVO> getUserById(@PathVariable("id") Long userId) {
        if (userId == null || userId < 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        return userService.getUserById(userId);
    }

    @DeleteMapping("/{id}")
    @Operation(summary="通过ID删除用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUserById(@PathVariable("id") Long userId) {
        if (userId == null || userId < 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        return userService.deleteUserById(userId);
    }


    @PostMapping("/update/New")
    @Operation(summary="更新用户")
    @SysLog("更新用户")
    public BaseResponse updateUserInfo(@RequestPart(value = "file", required = false) MultipartFile multipartFile,
                                       UserUpdateRequestNew param) {
        if (multipartFile != null) {
            // 执行更新用户图像操作
            FileUploadResult result = ossManager.uploadImage(multipartFile);
            ThrowUtils.throwIf(result.getStatus().equals("error"), ErrorCode.SYSTEM_ERROR, "上传头像失败!");
            String url = result.getName();
            param.setUserAvatar(url);
        }
        if (param == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User userEntity = BeanUtil.copyProperties(param, User.class);
        UserDTO user = UserHolder.getUser();
        userEntity.setId(user.getId());
        boolean b = userService.updateById(userEntity);
        ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR, "更新用户信息失败!");
        return ResultUtils.success("更新成功!");
    }



    /**
     * 校验头像的文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }

    @GetMapping("/current")
    @Operation(summary="获取当前用户信息")
    public BaseResponse<UserVO> currentUser() {
        UserDTO user = UserHolder.getUser();
        User userEntity = userService.getById(user.getId());
        UserVO UserVO = BeanUtil.copyProperties(userEntity, UserVO.class);
        return ResultUtils.success(UserVO);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////



    /**
     * 验证是否可以允许发送验证码
     *
     * @param email 电子邮件
     * @return boolean
     */
    boolean verifyCodeSend(String email) {
        String codeKey = RedisConstant.VERIFY_CODE_KEY + email;
        String oldCode = stringRedisTemplate.opsForValue().get(codeKey);
        // 判断是否之前发送过
        if (StringUtils.isNotBlank(oldCode)) {
            String[] split = oldCode.split("-");
            long time = Long.parseLong(split[0]);
            // 如果两次发送的间隔小于 60s => reject
            if (new Date().getTime() / 1000 - time < 60) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "请稍后发送验证码!");
            }
        }
        return true;
    }

    private void verifyCode(String code, String email) {
        String codeKey = RedisConstant.VERIFY_CODE_KEY + email;
        String realCodeValue = stringRedisTemplate.opsForValue().get(codeKey);
        if (StringUtils.isBlank(realCodeValue)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请重新发送验证码!");
        }
        String[] split = realCodeValue.split("-");
        String verifyCode = split[1];
        if (!verifyCode.equals(code)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "验证码错误!");
        }
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }


    // endregion

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @Operation(summary="添加用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码 Qq1843211535
        String defaultPassword = "Qq1843211535";
        String encryptPassword = BCrypt.hashpw(defaultPassword);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }



    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                    HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }


    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
}
