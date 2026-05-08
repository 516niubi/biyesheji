package com.example.backend.controller;

import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.vo.system.CaptchaVO;
import com.example.backend.service.ICaptchaService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class CaptchaController {

    @Resource
    private ICaptchaService captchaService;

    @ApiOperation("获取登录验证码")
    @GetMapping("/captcha")
    public BaseResponse<CaptchaVO> captcha() {
        return Result.success(captchaService.create());
    }
}
