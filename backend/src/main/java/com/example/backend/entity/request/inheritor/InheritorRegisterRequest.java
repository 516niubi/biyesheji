package com.example.backend.entity.request.inheritor;

import lombok.Data;

import java.util.List;

@Data
public class InheritorRegisterRequest {

    private String username;
    private String password;
    private String nickName;
    private String phone;
    private Integer gender;
    private Integer age;
    private String avatar;
    /** 个人简介 */
    private String profile;
    /** 认证证书等文件路径列表 */
    private List<String> certificateUrls;

    private String captchaKey;
    private String captcha;
}
