package com.example.backend.entity.request.user;

import lombok.Data;
/**
 * @Author caiya
 * @Description 新增用户接口请求参数
 * @Version 1.0
 */
@Data
public class AddUserRequest {
    /**
     * 账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 性别（0-男，1-女，2-保密）
     */
    private Integer gender;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 个人介绍
     */
    private String profile;
}
