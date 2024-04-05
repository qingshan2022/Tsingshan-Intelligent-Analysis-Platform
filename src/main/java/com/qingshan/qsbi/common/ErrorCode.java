package com.qingshan.qsbi.common;

/**
 * 自定义错误码
 *
 * @author <a href="https://github.com/liqingshan">青山</a>
 *
 */
public enum ErrorCode {

    SUCCESS(200, "ok", "请求成功"),
    PARAMS_ERROR(40000, "error", "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "error", "未登录"),
    NO_AUTH_ERROR(40101, "error", "无权限"),
    TOO_MANY_REQUEST(42900,"error", "请求过多"),
    NOT_FOUND_ERROR(40400, "error", "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "error", "禁止访问"),
    SYSTEM_ERROR(50000, "error", "系统内部异常"),
    OPERATION_ERROR(50001, "error", "操作失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    /**
     * 描述信息
     */
    private final String description;
    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
