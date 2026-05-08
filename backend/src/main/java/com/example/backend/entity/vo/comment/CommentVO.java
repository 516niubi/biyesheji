package com.example.backend.entity.vo.comment;

import com.example.backend.entity.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论视图对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentVO extends Comment {

    /**
     * 展示名（用户昵称或传承人昵称）
     */
    private String userName;

    private String avatar;

    /** 文物名称 */
    private String heritageName;

    /** 解析后的图片列表 */
    private List<String> imageUrls = new ArrayList<>();

    /** 是否为传承人身份发言 */
    private Boolean fromInheritor;

    /** 被回复用户昵称（parent 的作者） */
    private String replyToName;

    /** 嵌套回复（树形） */
    private List<CommentVO> children = new ArrayList<>();
}