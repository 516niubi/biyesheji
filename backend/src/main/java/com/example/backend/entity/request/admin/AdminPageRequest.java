package com.example.backend.entity.request.admin;

import lombok.Data;

/**
 * @Author caiya
 * @Description 管理员分页查询参数
 * @Version 1.0
 */
@Data
public class AdminPageRequest {
    private String nickName;
    private String username;
}
