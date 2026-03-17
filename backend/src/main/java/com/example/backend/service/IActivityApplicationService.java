package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.common.model.PageResult;
import com.example.backend.entity.ActivityApplication;
import com.example.backend.entity.vo.activityApplication.ActivityApplicationVO;

import java.util.List;

/**
 * 活动报名表服务接口
 */
public interface IActivityApplicationService extends IService<ActivityApplication> {

    /**
     * 新增活动报名
     */
    Integer add(ActivityApplication activityApplication);

    /**
     * 批量新增活动报名
     */
    Boolean batchAdd(List<ActivityApplication> activityApplications);

    /**
     * 删除活动报名
     */
    Boolean del(Integer id);

    /**
     * 批量删除活动报名
     */
    Boolean batchDel(List<Integer> ids);

    /**
     * 编辑活动报名
     */
    Boolean edit(Integer id, ActivityApplication activityApplication);

    /**
     * 分页条件查询活动报名
     */
    PageResult<List<ActivityApplicationVO>> queryPage(Integer pageNum, Integer pageSize, String realName, String phone, Integer status);

    /**
     * 查询全部活动报名
     */
    List<ActivityApplication> getAll();

    /**
     * 根据ID查询活动报名详情
     */
    ActivityApplication getByIdDetail(Integer id);
    
    /**
     * 用户报名活动（包含业务逻辑验证）
     */
    Boolean applyActivity(ActivityApplication activityApplication);
    
    /**
     * 审核报名（通过/拒绝）
     */
    Boolean auditApplication(Integer id, Integer status);

    /**
     * 分页查询用户报名信息
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param realName 真实姓名
     * @param userId 用户ID
     * @return 分页结果
     */
    PageResult<List<ActivityApplicationVO>> pageByUserId(Integer pageNum, Integer pageSize, String realName, Integer userId);
}