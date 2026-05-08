package com.example.backend.entity.request.inheritor;

import lombok.Data;

@Data
public class InheritorAuditRequest {
    private Integer id;
    /** 1 通过 2 拒绝 */
    private Integer status;
    private String remark;
}
