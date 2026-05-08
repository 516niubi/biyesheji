package com.example.backend.entity.vo.privatemessage;

import lombok.Data;

/** 传承人发起平台私信时可选的管理员展示信息 */
@Data
public class PmContactAdminVO {
    private Integer id;
    private String nickName;
    private String username;
    private String avatar;
}
