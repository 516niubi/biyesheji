package com.example.backend.entity.vo.privatemessage;

import lombok.Data;

/** 传承人端搜索前台用户（发起访客会话） */
@Data
public class FrontUserBriefVO {
    private Integer id;
    private String nickName;
    private String username;
    private String avatar;
}
