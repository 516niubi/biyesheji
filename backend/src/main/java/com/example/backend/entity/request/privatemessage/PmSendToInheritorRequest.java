package com.example.backend.entity.request.privatemessage;

import lombok.Data;

@Data
public class PmSendToInheritorRequest {
    private Integer inheritorId;
    private String content;
}
