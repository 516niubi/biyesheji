package com.example.backend.entity.vo.notice;

import com.baomidou.mybatisplus.annotation.TableField;
import com.example.backend.entity.Notice;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author caiya
 * @Description
 * @Version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NoticeVO extends Notice {
    @TableField(exist = false)
    private String publishName;
}
