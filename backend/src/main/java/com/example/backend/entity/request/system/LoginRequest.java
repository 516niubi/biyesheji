package com.example.backend.entity.request.system;

import lombok.Data;

/**
 * @Author caiya
 * @Description 用户登录请求体
 * @Version 1.0
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
    private String role;
    /** 与 /captcha 返回的 key 对应 */
    private String captchaKey;
    /** 用户输入的验证码 */
    private String captcha;
}
