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
import com.example.backend.entity.Inheritor;
import com.example.backend.common.enums.CodeEnum;
import com.example.backend.exception.BusinessException;
import com.example.backend.utils.BackendAuthHelper;
import com.example.backend.utils.PublisherNameResolver;
import com.example.backend.utils.PublisherNameResolver.PublisherView;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动服务实现类
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements IActivityService {

    @Resource
    private PublisherNameResolver publisherNameResolver;

    @Override
    public Integer add(Activity request) {
        BackendAuthHelper.requireAdminOrInheritor();
        Inheritor inh = BackendAuthHelper.tryLoginInheritor();
        if (inh != null) {
            request.setCreatorId(inh.getId());
        } else {
            request.setCreatorId(null);
        }
        request.setCreateTime(LocalDateTime.now());
        save(request);
        return request.getId();
    }
    
    @Override
    public Boolean batchAdd(List<Activity> request) {
        BackendAuthHelper.requireAdminOrInheritor();
        Inheritor inh = BackendAuthHelper.tryLoginInheritor();
        LocalDateTime now = LocalDateTime.now();
        for (Activity a : request) {
            if (inh != null) {
                a.setCreatorId(inh.getId());
            } else {
                a.setCreatorId(null);
            }
            if (a.getCreateTime() == null) {
                a.setCreateTime(now);
            }
        }
        return saveBatch(request);
    }
    
    @Override
    public Boolean del(Integer id) {
        Activity row = getById(id);
        if (row == null) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "数据不存在");
        }
        BackendAuthHelper.requireAdminOrInheritor();
        assertActivityOwned(row);
        return removeById(id);
    }
    
    @Override
    public Boolean batchDel(List<Integer> ids) {
        BackendAuthHelper.requireAdminOrInheritor();
        for (Integer id : ids) {
            Activity row = getById(id);
            if (row != null) {
                assertActivityOwned(row);
            }
        }
        return removeByIds(ids);
    }
    
    @Override
    public Boolean edit(Integer id, Activity request) {
        Activity existing = getById(id);
        if (existing == null) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "数据不存在");
        }
        BackendAuthHelper.requireAdminOrInheritor();
        assertActivityOwned(existing);
        request.setId(id);
        request.setCreatorId(existing.getCreatorId());
        return updateById(request);
    }
    
    private void assertActivityOwned(Activity row) {
        Inheritor inh = BackendAuthHelper.tryLoginInheritor();
        if (inh == null) {
            return;
        }
        if (row.getCreatorId() == null || !row.getCreatorId().equals(inh.getId())) {
            throw new BusinessException(CodeEnum.AUTH_ERROR, "仅能管理本人发布的活动");
        }
    }

    @Override
    public PageResult<List<ActivityVO>> queryPage(Integer pageNum, Integer pageSize, String title, Integer creatorIdFilter) {
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (StrUtil.isNotBlank(title)) {
            queryWrapper.like("title", title);
        }
        if (creatorIdFilter != null) {
            queryWrapper.eq("creator_id", creatorIdFilter);
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
        Activity activity = getById(id);
        if (activity != null) {
            int vc = activity.getViewCount() == null ? 0 : activity.getViewCount();
            activity.setViewCount(vc + 1);
            updateById(activity);
            PublisherView pv = publisherNameResolver.resolveView(activity.getCreatorId());
            activity.setPublisherName(pv.getName());
            activity.setPublisherAvatar(pv.getAvatar());
        }
        return activity;
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
        PublisherView pv = publisherNameResolver.resolveView(activity.getCreatorId());
        activityVO.setPublisherName(pv.getName());
        activityVO.setPublisherAvatar(pv.getAvatar());
        return activityVO;
    }
}