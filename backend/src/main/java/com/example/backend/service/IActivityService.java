package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.common.model.PageResult;
import com.example.backend.entity.Activity;
import com.example.backend.entity.vo.activity.ActivityVO;

import java.util.List;

/**
 * 活动服务接口
 */
public interface IActivityService extends IService<Activity> {
    
    /**
     * 新增
     * @param request
     * @return
     */
    Integer add(Activity request);
    
    /**
     * 批量新增
     *
     * @param request
     * @return
     */
    Boolean batchAdd(List<Activity> request);
    
    /**
     * 删除
     * @param id
     * @return
     */
    Boolean del(Integer id);
    
    /**
     * 批量删除
     * @param ids 批量ID列表
     * @return 是否成功
     */
    Boolean batchDel(List<Integer> ids);
    
    /**
     * 编辑
     * @param id id
     * @param request 请求参数
     * @return
     */
    Boolean edit(Integer id, Activity request);
    
    /**
     * 分页条件查询
     * @return
     */
    PageResult<List<ActivityVO>> queryPage(Integer pageNum, Integer pageSize, String title);
    
    /**
     * 查询全部
     * @return
     */
    List<Activity> getAll();
    
    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    Activity getByIdDetail(Integer id);
    
    /**
     * 获取热门活动分页数据
     * @param pageNum 页码
     * @param pageSize 页大小
     * @param orderBy 排序字段
     * @param orderType 排序类型
     * @return 分页结果
     */
    PageResult<List<ActivityVO>> getHotPage(Integer pageNum, Integer pageSize, String orderBy, String orderType);
}