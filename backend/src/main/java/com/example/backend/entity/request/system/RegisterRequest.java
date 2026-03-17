package com.example.backend.entity.request.system;

import lombok.Data;

/**
 * @Author caiya
 * @Description 用户注册参数请求体
 * @Version 1.0
 */
@Data
public class RegisterRequest {
    private String username;
    private String password;
}
