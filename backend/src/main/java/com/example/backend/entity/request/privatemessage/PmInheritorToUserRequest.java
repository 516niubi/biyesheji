package com.example.backend.entity.request.privatemessage;

import lombok.Data;

@Data
public class PmInheritorToUserRequest {
    private Integer frontUserId;
    private String content;
}
