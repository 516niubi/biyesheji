package com.example.backend.entity.request.privatemessage;

import lombok.Data;

@Data
public class PmReplyRequest {
    private Integer threadId;
    private String content;
}
