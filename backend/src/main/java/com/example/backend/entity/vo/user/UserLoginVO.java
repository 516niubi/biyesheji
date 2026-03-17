package com.example.backend.entity.vo.user;

import lombok.Data;

/**
 * @Author caiya
 * @Description 脱敏的用户信息
 * @Version 1.0
 */
@Data
public class UserLoginVO {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 账号
     */
    private String username;

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

    private String token;
}
