package com.example.backend.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.example.backend.common.constants.SaTokenConstant;
import com.example.backend.common.enums.CodeEnum;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.NestedServletException;

/**
 * @Author caiya
 * @Description 全局异常处理
 * @Version 1.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    // 自定义异常拦截
    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e) {
        log.error("businessException===" + e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage(), e.getDescription());
    }

    // 运行时异常拦截
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(BusinessException e) {
        log.error("runtimeException===" + e);
        return Result.error(CodeEnum.SYSTEM_ERROR, e.getMessage(), "");
    }

    // sa-token鉴权异常拦截
    @ExceptionHandler(NotLoginException.class)
    public BaseResponse handlerException(NotLoginException e) {
        log.error("NestedServletException===" + e);
        // 判断登录异常的场景，定制化返回提示
        String type = e.getType();
        switch (type) {
            case NotLoginException.INVALID_TOKEN:
            case NotLoginException.TOKEN_TIMEOUT:
                return Result.error(CodeEnum.AUTH_ERROR, SaTokenConstant.TOKEN_OVERDUE);
            case NotLoginException.BE_REPLACED:
            case NotLoginException.KICK_OUT:
                return Result.error(CodeEnum.AUTH_ERROR, SaTokenConstant.LOGIN_REPLACE);
            default:
                return Result.error(CodeEnum.AUTH_ERROR, SaTokenConstant.NOT_TOKEN);
        }
    }
}
