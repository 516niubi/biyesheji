package com.example.backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.backend.common.enums.CodeEnum;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.vo.admin.AdminLoginVO;
import com.example.backend.exception.BusinessException;
import com.example.backend.entity.request.system.LoginRequest;
import com.example.backend.entity.request.system.RegisterRequest;
import com.example.backend.entity.vo.user.UserLoginVO;
import com.example.backend.service.ICaptchaService;
import com.example.backend.service.IUserService;
import com.example.backend.service.impl.AdminServiceImpl;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author caiya
 * @Description 系统接口控制层
 * @Version 1.0
 */
@RestController
@Slf4j
public class SystemController {

    @Resource
    private IUserService userService;
    @Resource
    private AdminServiceImpl adminService;
    @Resource
    private ICaptchaService captchaService;

    @ApiOperation("测试接口")
    @GetMapping("/test")
    private String test() {
        return "hello word";
    }

    /**
     * 用户登录
     * @param request 请求体
     * @return token
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    private BaseResponse<UserLoginVO> login(@RequestBody LoginRequest request) {
        if (ObjectUtil.isNull(request)) throw new BusinessException(CodeEnum.NULL_ERROR);
        captchaService.verifyAndConsume(request.getCaptchaKey(), request.getCaptcha());
        UserLoginVO res = userService.login(request);
        return Result.success(res);
    }

    /**
     * 用户登录
     * @param request 请求体
     * @return token
     */
    @ApiOperation("管理员登录")
    @PostMapping("/admin/login")
    private BaseResponse<AdminLoginVO> adminLogin(@RequestBody LoginRequest request) {
        if (ObjectUtil.isNull(request)) throw new BusinessException(CodeEnum.NULL_ERROR);
        captchaService.verifyAndConsume(request.getCaptchaKey(), request.getCaptcha());
        AdminLoginVO res = adminService.login(request);
        return Result.success(res);
    }

    /**
     * 用户注册
     * @param request 请求体
     * @return 受影响行数
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    private BaseResponse<Integer> register(@RequestBody RegisterRequest request) {
        if (ObjectUtil.isNull(request)) throw new BusinessException(CodeEnum.NULL_ERROR);
        Integer res = userService.register(request);
        return Result.success(res);
    }

    /**
     * 用户退出登录
     * @return 成功结果
     */
    @ApiOperation("用户退出登录")
    @PostMapping("/logout")
    private BaseResponse<String> logout() {
        StpUtil.logout();
        return Result.success("");
    }
}
