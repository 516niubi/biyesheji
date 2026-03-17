package com.example.backend.common.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Author caiya
 * @Description sa-token常量封装类
 * @Version 1.0
 */
public interface SaTokenConstant {

    /**
     * 需要校验的路由数组
     */
    List<String> allRouters = Collections.singletonList("/**");

    /**
     * 无需校验的路由数组
     */
    List<String> excludePathPatterns = Arrays.asList(
            "/test",
            "/login",
            "/admin/login",
            "/backend/login",
            "/register",
            "/logout",
            "/getVerifyImg",
            "/swagger-resources/**",
            "/webjars/**",
            "/v2/**",
            "/doc.html",
            "/file/**",
            "**/swagger-ui.html",
            "/swagger-ui.html/**"
    );

    public static final String NOT_TOKEN = "您未登录，请登录！";

    public static final String TOKEN_OVERDUE = "登录已失效，请重新登录！";

    public static final String LOGIN_REPLACE = "您的账号已在别处登录！";

    public static final String PERMISSION_ERROR = "您没有权限！";
}
