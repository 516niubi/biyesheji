package com.example.backend.entity.vo.inheritor;

import lombok.Data;

/**
 * 前台展示的传承人公开信息（不含敏感字段）
 */
@Data
public class InheritorPublicProfileVO {

    private Integer id;
    private String nickName;
    private String avatar;
    private String profile;
}
