package com.example.backend.entity.vo.culturalheritage;

import com.baomidou.mybatisplus.annotation.TableField;
import com.example.backend.entity.CulturalHeritage;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author 
 * @Description 非遗文物视图对象
 * @Version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CulturalHeritageVO extends CulturalHeritage {
    
    @TableField(exist = false)
    private String categoryName;
    
}