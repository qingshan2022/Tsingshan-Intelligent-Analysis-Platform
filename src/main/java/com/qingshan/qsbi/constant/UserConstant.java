package com.qingshan.qsbi.constant;

/**
 * 用户常量
 *
 * @author <a href="https://github.com/liqingshan">青山</a>
 * 
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    // endregion


    public static final String PASSWORD_SALT = "dhxSalt";

    public static final int USER_PAGE_SIZE = 10;



    /**
     * 用户名正则 允许包含字母、数字、下划线和中文字符，长度在4到16个字符之间。
     */
    public static final String USER_NAME_REGEX = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]{4,16}$";

    /**
     * 帐户正则 允许包含字母、数字和下划线，长度在8到20个字符之间。
     */
    public static final String USER_ACCOUNT_REGEX = "^[a-zA-Z0-9_]{8,20}$";

    /**
     * 密码正则 包含至少一个数字、一个小写字母、一个大写字母，并且长度至少为8个字符。
     */
    public static final String PASSWORD_REGEX="^(?=.*[a-zA-Z])(?=.*\\d).{6,18}$";

    /**
     * 电话正则
     */
    public static final String PHONE_REGEX = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$";

    /**
     * 邮箱正则
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
}
