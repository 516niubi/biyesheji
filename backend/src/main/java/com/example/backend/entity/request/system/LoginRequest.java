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
}
