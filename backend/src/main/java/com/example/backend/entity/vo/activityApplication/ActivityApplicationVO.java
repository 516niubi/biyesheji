package com.example.backend.entity.vo.activityApplication;

import com.baomidou.mybatisplus.annotation.TableField;
import com.example.backend.entity.ActivityApplication;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 活动报名表VO类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityApplicationVO extends ActivityApplication {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 活动标题
     */
    private String activityTitle;

    /** 活动封面（相对路径） */
    @TableField(exist = false)
    private String activityCoverImage;
    
    /**
     * 状态文本
     */
    private String statusText;
}