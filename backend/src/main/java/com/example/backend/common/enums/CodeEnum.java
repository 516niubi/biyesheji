package com.example.backend.common.enums;

/**
 * @Author：caiya
 * @Description：返回状态码枚举类
 * @Version 1.0
 */
public enum CodeEnum {

    SUCCESS(200, "success", ""),

    TOKEN_INVALID_ERROR(401, "无效的token", ""),
    NOT_LOGIN(401, "token验证失败，请重新登录", ""),
    AUTH_ERROR(520, "权限异常", ""),

    PARAMS_ERROR(400, "请求参数错误", ""),
    NULL_ERROR(4001, "请求数据为空", ""),

    SYSTEM_ERROR(50000, "系统内部异常", "")
    ;

    /**
     * 状态码
     */
    private final int code;
    /**
     * 返回信息
     */
    private final String message;
    /**
     * 详细描述
     */
    private final String description;

    CodeEnum(int code, String message, String description) {
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
