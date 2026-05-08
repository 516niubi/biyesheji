package com.example.backend.entity.vo.collect;

import com.baomidou.mybatisplus.annotation.TableField;
import com.example.backend.entity.Collect;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author caiya
 * @Description
 * @Version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CollectVO extends Collect {
    @TableField(exist = false)
    private String userName;
    
    @TableField(exist = false)
    private String heritageName;

    /** 文物封面（相对路径，前端需拼接资源地址） */
    @TableField(exist = false)
    private String heritageCoverImage;

    @TableField(exist = false)
    private String heritageIntro;

    @TableField(exist = false)
    private String heritageCategoryName;
}