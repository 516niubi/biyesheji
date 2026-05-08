package com.example.backend.entity.request.inheritor;

import lombok.Data;

import java.util.List;

/**
 * 管理员编辑已认证传承人
 */
@Data
public class InheritorAdminUpdateRequest {

    private Integer id;
    private String nickName;
    private String phone;
    private Integer gender;
    private Integer age;
    private String avatar;
    private String profile;
    /** 传 null 表示不修改材料；传非空列表则整体替换 */
    private List<String> certificateUrls;
}
