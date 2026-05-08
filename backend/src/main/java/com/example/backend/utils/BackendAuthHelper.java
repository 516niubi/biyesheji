package com.example.backend.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.example.backend.common.constants.LoginConstant;
import com.example.backend.common.enums.CodeEnum;
import com.example.backend.entity.Admin;
import com.example.backend.entity.Inheritor;
import com.example.backend.entity.User;
import com.example.backend.exception.BusinessException;

/**
 * 后台鉴权：管理员（admin 表）与非遗传承人（inheritor 表）
 */
public final class BackendAuthHelper {

    private BackendAuthHelper() {
    }

    public static Object tryLoginPrincipal() {
        try {
            if (!StpUtil.isLogin()) {
                return null;
            }
            return StpUtil.getSession().get(LoginConstant.USER_KEY);
        } catch (Exception e) {
            return null;
        }
    }

    public static Admin tryLoginAdmin() {
        Object o = tryLoginPrincipal();
        return o instanceof Admin ? (Admin) o : null;
    }

    public static Inheritor tryLoginInheritor() {
        Object o = tryLoginPrincipal();
        return o instanceof Inheritor ? (Inheritor) o : null;
    }

    /** 前台登录的普通用户（Session 中为 {@link User}） */
    public static User tryLoginFrontUser() {
        Object o = tryLoginPrincipal();
        return o instanceof User ? (User) o : null;
    }

    public static Admin requireLoginAdmin() {
        Admin admin = tryLoginAdmin();
        if (admin == null) {
            throw new BusinessException(CodeEnum.AUTH_ERROR, "请使用管理员账号登录");
        }
        return admin;
    }

    public static Inheritor requireLoginInheritor() {
        Inheritor inheritor = tryLoginInheritor();
        if (inheritor == null) {
            throw new BusinessException(CodeEnum.AUTH_ERROR, "请使用传承人账号登录");
        }
        return inheritor;
    }

    /** 管理端接口：管理员或已登录传承人 */
    public static void requireAdminOrInheritor() {
        if (tryLoginAdmin() == null && tryLoginInheritor() == null) {
            throw new BusinessException(CodeEnum.AUTH_ERROR, "请使用后台账号登录");
        }
    }

    public static boolean isInheritorSession() {
        return tryLoginInheritor() != null;
    }

    /**
     * 传承人登录时返回自身 id；管理员返回 null（不限定数据范围）
     */
    public static Integer inheritorCreatorScopeOrNull() {
        requireAdminOrInheritor();
        Inheritor inh = tryLoginInheritor();
        return inh != null ? inh.getId() : null;
    }
}
