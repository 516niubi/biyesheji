package com.example.backend.entity.request.user;

import lombok.Data;

/**
 * @Author caiya
 * @Description 更新密码
 * @Version 1.0
 */
@Data
public class UpdatePassRequest {
    /** 用户id */
    private Integer userId;
    /** 旧密码 */
    private String oldPass;
    /** 新密码 */
    private String password;
}
