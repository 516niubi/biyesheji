package com.example.backend.service;

import com.example.backend.entity.vo.system.CaptchaVO;

/**
 * 图形验证码
 */
public interface ICaptchaService {

    CaptchaVO create();

    /**
     * 校验并消费（一次性）
     */
    void verifyAndConsume(String key, String userInput);
}
