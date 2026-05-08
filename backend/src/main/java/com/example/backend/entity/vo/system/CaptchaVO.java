package com.example.backend.entity.vo.system;

import lombok.Data;

/**
 * 登录图形验证码
 */
@Data
public class CaptchaVO {
    /** 前端回传校验用 */
    private String key;
    /** PNG Base64（不含 data: 前缀） */
    private String imageBase64;
}
