package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论实体类
 */
@Data
@TableName("comment")
public class Comment {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /** 前台用户；传承人登录发言时为 null */
    private Integer userId;

    private Integer heritageId;

    /** 父评论 ID，顶层为 null */
    private Integer parentId;

    private String content;

    /** 图片相对路径 JSON 数组字符串 */
    private String images;

    /** 传承人作者；普通用户留言时为 null */
    private Integer inheritorId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}