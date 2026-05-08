package com.example.backend.entity.vo.privatemessage;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PrivateMessageThreadListVO {
    private Integer id;
    /** 1传承人-平台 2用户-传承人 */
    private Integer threadKind;
    /** kind=2 时对方用户 id（传承人视角） */
    private Integer frontUserId;
    /** 列表主标题：对方昵称；传承人端平台会话为「私信」 */
    private String counterpartyName;
    /** 列表用对方头像：访客为用户头像，平台为最近参与管理员头像（无则空） */
    private String counterpartyAvatar;
    private Integer inheritorId;
    private String inheritorNickName;
    private String subject;
    private String lastMessagePreview;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /** 当前查看者是否有未读（接口按角色填充） */
    private Boolean hasUnread;
}
