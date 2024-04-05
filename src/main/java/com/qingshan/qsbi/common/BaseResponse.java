package com.qingshan.qsbi.common;

import java.io.Serializable;
import lombok.Data;

/**
 * 通用返回类
 *
 * @param <T>
 * @author <a href="https://github.com/liqingshan">青山</a>
 * 
 */
@Data
public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;

    private T data;

    private String message;

    private String description;


    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;

    }

    public BaseResponse(int code, T data, String description) {
        if(code!=200){
            new BaseResponse<>(code, data, "error", description);
        }
        new BaseResponse<>(code, data, "ok", description);
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage(), errorCode.getDescription());
    }

    public BaseResponse(ErrorCode errorCode, String description) {
        this(errorCode.getCode(), null, errorCode.getMessage(), description);
    }


    public BaseResponse(ErrorCode errorCode, String message, String description) {
        this(errorCode.getCode(), null, message, description);
    }

    /**
     * 正常返回
     * @param data
     * @param <T>
     * @return
     */
    public <T> BaseResponse<T> success(T data) {
        return new BaseResponse<T>(200, data, "ok", "");
    }
}
