package com.example.backend.service.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.backend.common.enums.CodeEnum;
import com.example.backend.entity.vo.system.CaptchaVO;
import com.example.backend.exception.BusinessException;
import com.example.backend.service.ICaptchaService;
import org.springframework.stereotype.Service;

/**
 * 内存缓存验证码（单机部署；集群需改 Redis）
 */
@Service
public class CaptchaServiceImpl implements ICaptchaService {

    private static final long TIMEOUT_MS = 120_000;

    private final TimedCache<String, String> cache = CacheUtil.newTimedCache(TIMEOUT_MS);

    public CaptchaServiceImpl() {
        cache.schedulePrune(30_000);
    }

    @Override
    public CaptchaVO create() {
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 4);
        String code = lineCaptcha.getCode();
        String key = IdUtil.fastSimpleUUID();
        cache.put(key, code.toLowerCase());

        CaptchaVO vo = new CaptchaVO();
        vo.setKey(key);
        vo.setImageBase64(lineCaptcha.getImageBase64());
        return vo;
    }

    @Override
    public void verifyAndConsume(String key, String userInput) {
        if (StrUtil.isBlank(key) || StrUtil.isBlank(userInput)) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "请输入验证码");
        }
        String stored = cache.get(key);
        if (StrUtil.isBlank(stored)) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "验证码已失效，请刷新");
        }
        cache.remove(key);
        if (!stored.equalsIgnoreCase(StrUtil.trim(userInput))) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "验证码错误");
        }
    }
}
