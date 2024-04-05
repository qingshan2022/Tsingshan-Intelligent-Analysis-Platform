package com.qingshan.qsbi.exception;

import com.qingshan.qsbi.common.ErrorCode;
import lombok.Data;

/**
 * 自定义异常类
 *
 * @author <a href="https://github.com/liqingshan">青山</a>
 * 
 */
@Data
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 165474231423634L;
    /**
     * 错误码
     */
    private int code=500;

    private String message="error";

    /**
     * 错误描述
     */
    private String description;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(int code,String description,String message){
        super(message);// 错误信息
        this.code=code;
        this.description=description;
    }

    public BusinessException(ErrorCode errorCode, String description){
        super(errorCode.getMessage());// 错误信息
        this.description=description;
    }

    public BusinessException(ErrorCode errorCode){
        super(errorCode.getMessage());// 错误信息
        this.code=errorCode.getCode();
        this.description=errorCode.getDescription();
    }
}
