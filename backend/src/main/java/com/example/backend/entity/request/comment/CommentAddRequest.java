package com.example.backend.entity.request.comment;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 前台发表评论（支持图文、回复）
 */
@Data
public class CommentAddRequest {

    private Integer heritageId;

    /** 回复哪一条评论；不传或 null 表示顶层评论 */
    private Integer parentId;

    private String content;

    /** 上传后的相对路径列表，与 content 至少填一类 */
    private List<String> images = new ArrayList<>();
}
