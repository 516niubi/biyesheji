package com.example.backend.common.constants;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author caiya
 * @Description 登录相关常量
 * @Version 1.0
 */
public interface LoginConstant {
    String LOGIN_NAME = "loginName";
    String REAL_NAME = "userName";
    String USER_ID = "userId";
    String USER_KEY = "userInfo";
    List<String> backRoles = new ArrayList<String>() {
        {
            add("admin");
            add("user");
        }
    };

    /**
     * Redis存储验证码图片的key
     */
    String VERIFY_IMG_KEY = "verifyImg";
}
