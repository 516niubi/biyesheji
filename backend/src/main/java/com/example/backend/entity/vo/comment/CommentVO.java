package com.example.backend.entity.vo.comment;

import com.example.backend.entity.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论视图对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentVO extends Comment {
    
    /**
     * 用户名
     */
    private String userName;

    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 文物名称
     */
    private String heritageName;
}