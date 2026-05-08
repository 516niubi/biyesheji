package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 非遗传承人（独立账号，与管理员分表）
 */
@Data
@TableName("inheritor")
public class Inheritor implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;

    private String password;

    private String nickName;

    /** 手机号（便于管理员后续短信通知等） */
    private String phone;

    private Integer gender;

    private Integer age;

    private String avatar;

    /** 个人简介 */
    private String profile;

    /**
     * 认证证书等材料，JSON 数组字符串，如 ["/file/...","/file/..."]
     */
    private String certificateUrls;

    /**
     * 0-待审核 1-已通过 2-已拒绝
     */
    private Integer status;

    private String auditRemark;

    private Integer auditorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date auditTime;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
