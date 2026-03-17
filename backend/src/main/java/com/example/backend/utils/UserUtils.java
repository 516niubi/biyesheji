package com.example.backend.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.example.backend.entity.User;

/**
 * @Author caiya
 * @Description 用户工具类
 * @Version 1.0
 */
public class UserUtils {

    // 通过Sa-Token 获取当前登录用户信息
    public static User getUserInfo() {
        User user = (User) StpUtil.getSession().get("user");
        return user;
    }
}
