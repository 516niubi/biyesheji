package com.example.backend.entity.request.user;

import lombok.Data;

/**
 * @Author caiya
 * @Description 用户分页查询参数
 * @Version 1.0
 */
@Data
public class UserPageRequest {
    private String nickName;
    private String username;
}
