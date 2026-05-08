package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.enums.CodeEnum;
import com.example.backend.common.model.PageResult;
import com.example.backend.entity.Activity;
import com.example.backend.entity.ActivityApplication;
import com.example.backend.entity.User;
import com.example.backend.entity.vo.activityApplication.ActivityApplicationVO;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ActivityApplicationMapper;
import com.example.backend.service.IActivityApplicationService;
import com.example.backend.service.IActivityService;
import com.example.backend.service.IUserService;
import com.example.backend.entity.Inheritor;
import com.example.backend.utils.BackendAuthHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动报名表服务实现类
 */
@Service
public class ActivityApplicationServiceImpl extends ServiceImpl<ActivityApplicationMapper, ActivityApplication> implements IActivityApplicationService {

    @Resource
    private IActivityService activityService;
    
    @Resource
    private IUserService userService;

    @Override
    public Integer add(ActivityApplication activityApplication) {
        activityApplication.setCreateTime(LocalDateTime.now());
        activityApplication.setUpdateTime(LocalDateTime.now());
        save(activityApplication);
        return activityApplication.getId();
    }

    @Override
    public Boolean batchAdd(List<ActivityApplication> activityApplications) {
        LocalDateTime now = LocalDateTime.now();
        activityApplications.forEach(item -> {
            item.setCreateTime(now);
            item.setUpdateTime(now);
        });
        return saveBatch(activityApplications);
    }

    @Override
    public Boolean del(Integer id) {
        BackendAuthHelper.requireAdminOrInheritor();
        ActivityApplication row = getById(id);
        if (row != null) {
            assertApplicationManagedBy(row);
        }
        return removeById(id);
    }

    @Override
    public Boolean batchDel(List<Integer> ids) {
        BackendAuthHelper.requireAdminOrInheritor();
        for (Integer id : ids) {
            ActivityApplication row = getById(id);
            if (row != null) {
                assertApplicationManagedBy(row);
            }
        }
        return removeByIds(ids);
    }

    @Override
    public Boolean edit(Integer id, ActivityApplication activityApplication) {
        ActivityApplication existing = getById(id);
        if (existing != null) {
            BackendAuthHelper.requireAdminOrInheritor();
            assertApplicationManagedBy(existing);
        }
        activityApplication.setId(id);
        activityApplication.setUpdateTime(LocalDateTime.now());
        return updateById(activityApplication);
    }

    private void assertApplicationManagedBy(ActivityApplication application) {
        Inheritor inh = BackendAuthHelper.tryLoginInheritor();
        if (inh == null) {
            return;
        }
        Activity activity = activityService.getById(application.getActivityId());
        if (activity == null || activity.getCreatorId() == null || !activity.getCreatorId().equals(inh.getId())) {
            throw new BusinessException(CodeEnum.AUTH_ERROR, "仅能管理本人活动的报名");
        }
    }

    @Override
    public PageResult<List<ActivityApplicationVO>> queryPage(Integer pageNum, Integer pageSize, String realName, String phone, Integer status, Integer activityCreatorAdminId) {
        IPage<ActivityApplication> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ActivityApplication> queryWrapper = new QueryWrapper<>();

        if (activityCreatorAdminId != null) {
            QueryWrapper<Activity> aq = new QueryWrapper<>();
            aq.eq("creator_id", activityCreatorAdminId);
            List<Activity> acts = activityService.list(aq);
            List<Integer> actIds = acts.stream().map(Activity::getId).collect(Collectors.toList());
            if (actIds.isEmpty()) {
                PageResult<List<ActivityApplicationVO>> empty = new PageResult<>();
                empty.setRecords(Collections.emptyList());
                empty.setTotal(0);
                empty.setCurrent(pageNum);
                empty.setSize(pageSize);
                return empty;
            }
            queryWrapper.in("activity_id", actIds);
        }
        
        if (StringUtils.hasText(realName)) {
            queryWrapper.like("real_name", realName);
        }
        if (StringUtils.hasText(phone)) {
            queryWrapper.like("phone", phone);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByDesc("id");
        page(page, queryWrapper);
        
        List<ActivityApplicationVO> voList = page.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        
        PageResult<List<ActivityApplicationVO>> pageResult = new PageResult<>();
        pageResult.setRecords(voList);
        pageResult.setTotal(page.getTotal());
        pageResult.setCurrent(pageNum);
        pageResult.setSize(pageSize);
        
        return pageResult;
    }

    @Override
    public List<ActivityApplication> getAll() {
        return list();
    }

    @Override
    public ActivityApplication getByIdDetail(Integer id) {
        return getById(id);
    }

    @Override
    public Boolean applyActivity(ActivityApplication activityApplication) {
        // 1. 验证活动是否存在
        Activity activity = activityService.getById(activityApplication.getActivityId());
        if (activity == null) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "活动不存在");
        }
        
        // 2. 验证活动时间是否有效
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(activity.getEndTime())) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "活动已结束，无法报名");
        }
        if (now.isBefore(activity.getStartTime().minusDays(1))) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "活动尚未开始报名");
        }
        
        // 3. 验证用户是否已经报名
        QueryWrapper<ActivityApplication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", activityApplication.getUserId())
                   .eq("activity_id", activityApplication.getActivityId());
        ActivityApplication existApplication = getOne(queryWrapper);
        if (existApplication != null) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "您已经报名过该活动");
        }
        
        // 4. 验证活动人数是否已满
        QueryWrapper<ActivityApplication> countWrapper = new QueryWrapper<>();
        countWrapper.eq("activity_id", activityApplication.getActivityId())
                   .eq("status", 1); // 只统计通过审核的报名
        long approvedCount = count(countWrapper);
        if (approvedCount >= activity.getMaxPeople()) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR,  "活动报名人数已满");
        }
        
        // 5. 设置默认状态和时间
        activityApplication.setStatus(0); // 待审核
        activityApplication.setCreateTime(now);
        activityApplication.setUpdateTime(now);
        
        return save(activityApplication);
    }

    @Override
    public Boolean auditApplication(Integer id, Integer status) {
        ActivityApplication application = getById(id);
        if (application == null) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR,  "报名记录不存在");
        }
        BackendAuthHelper.requireAdminOrInheritor();
        assertApplicationManagedBy(application);

        // 如果是通过审核，需要再次验证人数限制
        if (status == 1) {
            Activity activity = activityService.getById(application.getActivityId());
            QueryWrapper<ActivityApplication> countWrapper = new QueryWrapper<>();
            countWrapper.eq("activity_id", application.getActivityId())
                       .eq("status", 1);
            long approvedCount = count(countWrapper);
            if (approvedCount >= activity.getMaxPeople()) {
                throw new BusinessException(CodeEnum.PARAMS_ERROR,  "活动报名人数已满，无法通过审核");
            }
        }
        
        application.setStatus(status);
        application.setUpdateTime(LocalDateTime.now());
        return updateById(application);
    }

    @Override
    public PageResult<List<ActivityApplicationVO>> pageByUserId(Integer pageNum, Integer pageSize, String realName, Integer userId) {
        IPage<ActivityApplication> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ActivityApplication> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(realName)) {
            queryWrapper.like("real_name", realName);
        }
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");
        page(page, queryWrapper);

        List<ActivityApplicationVO> voList = page.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());

        PageResult<List<ActivityApplicationVO>> pageResult = new PageResult<>();
        pageResult.setRecords(voList);
        pageResult.setTotal(page.getTotal());
        pageResult.setCurrent(pageNum);
        pageResult.setSize(pageSize);

        return pageResult;
    }

    /**
     * 转换为VO对象
     */
    private ActivityApplicationVO convertToVO(ActivityApplication activityApplication) {
        ActivityApplicationVO vo = new ActivityApplicationVO();
        BeanUtils.copyProperties(activityApplication, vo);
        
        // 获取用户名
        if (activityApplication.getUserId() != null) {
            User user = userService.getById(activityApplication.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }
        }
        
        // 获取活动标题
        if (activityApplication.getActivityId() != null) {
            Activity activity = activityService.getById(activityApplication.getActivityId());
            if (activity != null) {
                vo.setActivityTitle(activity.getTitle());
                vo.setActivityCoverImage(activity.getCoverImage());
            }
        }
        
        // 设置状态文本
        switch (activityApplication.getStatus()) {
            case 0:
                vo.setStatusText("待审核");
                break;
            case 1:
                vo.setStatusText("通过");
                break;
            case 2:
                vo.setStatusText("拒绝");
                break;
            default:
                vo.setStatusText("未知");
        }
        
        return vo;
    }
}