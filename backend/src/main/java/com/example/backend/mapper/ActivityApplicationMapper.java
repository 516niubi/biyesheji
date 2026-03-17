package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.ActivityApplication;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动报名表Mapper接口
 */
@Mapper
public interface ActivityApplicationMapper extends BaseMapper<ActivityApplication> {

}