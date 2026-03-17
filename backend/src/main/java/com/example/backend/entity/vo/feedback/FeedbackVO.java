package com.example.backend.entity.vo.feedback;

import com.baomidou.mybatisplus.annotation.TableField;
import com.example.backend.entity.Feedback;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author caiya
 * @Description
 * @Version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FeedbackVO extends Feedback {
    @TableField(exist = false)
    private String userName;
}