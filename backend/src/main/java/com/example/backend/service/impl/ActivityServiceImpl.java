package com.example.backend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.model.PageResult;
import com.example.backend.entity.Activity;
import com.example.backend.entity.vo.activity.ActivityVO;
import com.example.backend.mapper.ActivityMapper;
import com.example.backend.service.IActivityService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动服务实现类
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements IActivityService {
    
    @Override
    public Integer add(Activity request) {
        request.setCreateTime(LocalDateTime.now());
        save(request);
        return request.getId();
    }
    
    @Override
    public Boolean batchAdd(List<Activity> request) {
        return saveBatch(request);
    }
    
    @Override
    public Boolean del(Integer id) {
        return removeById(id);
    }
    
    @Override
    public Boolean batchDel(List<Integer> ids) {
        return removeByIds(ids);
    }
    
    @Override
    public Boolean edit(Integer id, Activity request) {
        request.setId(id);
        return updateById(request);
    }
    
    @Override
    public PageResult<List<ActivityVO>> queryPage(Integer pageNum, Integer pageSize, String title) {
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (StrUtil.isNotBlank(title)) {
            queryWrapper.like("title", title);
        }
        Page<Activity> page = page(new Page<>(pageNum, pageSize), queryWrapper);
        List<ActivityVO> activityVOList = page.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        // 返回结果
        PageResult<List<ActivityVO>> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setSize(page.getSize());
        pageResult.setCurrent(page.getCurrent());
        pageResult.setRecords(activityVOList);
        return pageResult;
    }
    
    @Override
    public List<Activity> getAll() {
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        return list(queryWrapper);
    }
    
    @Override
    public Activity getByIdDetail(Integer id) {
        // 增加活动浏览量
        Activity activity = getById(id);
        if (activity != null) {
            activity.setViewCount(activity.getViewCount() + 1);
            updateById(activity);
        }
        return getById(id);
    }
    
    @Override
    public PageResult<List<ActivityVO>> getHotPage(Integer pageNum, Integer pageSize, String orderBy, String orderType) {
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        
        // 根据排序类型设置排序
        if ("desc".equalsIgnoreCase(orderType)) {
            queryWrapper.orderByDesc(orderBy);
        } else {
            queryWrapper.orderByAsc(orderBy);
        }
        
        Page<Activity> page = page(new Page<>(pageNum, pageSize), queryWrapper);
        List<ActivityVO> activityVOList = page.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        
        // 返回结果
        PageResult<List<ActivityVO>> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setSize(page.getSize());
        pageResult.setCurrent(page.getCurrent());
        pageResult.setRecords(activityVOList);
        return pageResult;
    }
    
    /**
     * 将Activity转换为ActivityVO
     */
    private ActivityVO convertToVO(Activity activity) {
        ActivityVO activityVO = new ActivityVO();
        BeanUtils.copyProperties(activity, activityVO);
        return activityVO;
    }
}