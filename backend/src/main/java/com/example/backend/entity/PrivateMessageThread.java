package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信会话（共用表）：kind=1 传承人—平台管理员；kind=2 用户—传承人
 */
@Data
@TableName("private_message_thread")
public class PrivateMessageThread implements Serializable {

    /** 传承人 ↔ 平台管理员 */
    public static final int KIND_PLATFORM = 1;
    /** 前台用户 ↔ 传承人 */
    public static final int KIND_USER_INHERITOR = 2;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer inheritorId;

    /** 1传承人-平台 2用户-传承人 */
    private Integer threadKind;

    /** kind=2 时前台用户 id */
    private Integer frontUserId;

    /** 主题标题 */
    private String subject;

    /**
     * kind=1 时：指定接收该会话的管理员；NULL 表示待认领（管理员均可查看，首次回复时认领给当前管理员）
     */
    private Integer platformTargetAdminId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime inheritorLastReadAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime adminLastReadAt;

    /** kind=2 时用户侧已读 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime userLastReadAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
