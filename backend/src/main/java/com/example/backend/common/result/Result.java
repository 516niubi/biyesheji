package com.example.backend.common.result;

import com.example.backend.common.enums.CodeEnum;

/**
 * @Author caiya
 * @Description 通用返回结果
 * @Version 1.0
 */
public class Result {
    /**
     * 返回成功请求
     * @param data 请求结果
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T>  success(T data) {
        return new BaseResponse<>(200,  data, "success", "");
    }

    /**
     * 返回错误请求
     * @param codeEnum
     * @return
     */
    public static BaseResponse error(CodeEnum codeEnum) {
        return new BaseResponse(codeEnum);
    }

    /**
     * 返回错误请求
     * @param codeEnum
     * @return
     */
    public static BaseResponse error(CodeEnum codeEnum, String description) {
        return new BaseResponse(codeEnum.getCode(), null, codeEnum.getMessage(), description);
    }

    /**
     * 返回错误请求
     * @param codeEnum
     * @return
     */
    public static BaseResponse error(CodeEnum codeEnum, String message, String description) {
        return new BaseResponse(codeEnum.getCode(), null, message, description);
    }

    /**
     * 返回错误请求
     *
     * @return
     */
    public static BaseResponse error(int code, String message, String description) {
        return new BaseResponse(code, null, message, description);
    }
}
