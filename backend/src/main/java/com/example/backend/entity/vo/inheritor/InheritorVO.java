package com.example.backend.entity.vo.inheritor;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class InheritorVO {
    private Integer id;
    private String username;
    private String nickName;
    private String phone;
    private Integer gender;
    private Integer age;
    private String avatar;
    private String profile;
    private String certificateUrls;
    private Integer status;
    private String auditRemark;
    private Integer auditorId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date auditTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
