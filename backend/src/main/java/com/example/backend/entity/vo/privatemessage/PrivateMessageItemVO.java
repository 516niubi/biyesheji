package com.example.backend.entity.vo.privatemessage;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PrivateMessageItemVO {
    private Long id;
    private Integer threadId;
    /** 0 传承人 1 管理员 */
    private Integer senderRole;
    private String senderName;
    /** 头像相对路径（前台拼接域名） */
    private String senderAvatar;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
