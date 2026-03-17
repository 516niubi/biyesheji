package com.example.backend.entity.vo.activity;

import com.example.backend.entity.Activity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 活动视图对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityVO extends Activity {
    // 可以在这里添加额外的字段，如果需要的话
}