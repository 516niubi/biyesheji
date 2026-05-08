package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信消息（单条）
 */
@Data
@TableName("private_message")
public class PrivateMessage implements Serializable {

    /** 传承人发送 */
    public static final int ROLE_INHERITOR = 0;
    /** 管理员发送 */
    public static final int ROLE_ADMIN = 1;
    /** 前台注册用户发送 */
    public static final int ROLE_USER = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer threadId;

    /** 0传承人 1管理员 2前台用户 */
    private Integer senderRole;

    private Integer senderId;

    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
