package com.example.backend.exception;

import com.example.backend.common.enums.CodeEnum;

/**
 * @Author caiya
 * @Description 自定义业务异常类
 * @Version 1.0
 */
public class BusinessException extends RuntimeException {
    private final int code;
    private final String description;

    public BusinessException(int code, String message, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(CodeEnum errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(CodeEnum errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
