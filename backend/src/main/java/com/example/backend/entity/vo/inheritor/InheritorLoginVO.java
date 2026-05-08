package com.example.backend.entity.vo.inheritor;

import lombok.Data;

@Data
public class InheritorLoginVO {
    private Integer id;
    private String username;
    private String nickName;
    private String phone;
    private String avatar;
    private String profile;
    /** 固定 inheritor，供前端展示 */
    private String role;
    private String token;
}
