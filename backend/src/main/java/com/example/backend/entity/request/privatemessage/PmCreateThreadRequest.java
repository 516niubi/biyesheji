package com.example.backend.entity.request.privatemessage;

import lombok.Data;

@Data
public class PmCreateThreadRequest {
    /** 主题，可选，默认「私信」 */
    private String subject;
    /** 首条正文 */
    private String content;
    /** 指定接收管理员 id（可选）；不传则待认领，由首位回复的管理员认领 */
    private Integer platformTargetAdminId;
}
