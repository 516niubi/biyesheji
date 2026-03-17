package com.example.backend.service;

import com.example.backend.entity.Feedback;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.common.model.PageResult;
import com.example.backend.entity.vo.feedback.FeedbackVO;

import java.util.List;

/**
 * <p>
 * 反馈表 服务类
 * </p>
 *
 * @author
 * @since 2025-01-30
 */
public interface IFeedbackService extends IService<Feedback> {
    /**
     * 新增
     *
     * @param request
     * @return
     */
    Integer add(Feedback request);

    /**
     * 批量新增
     *
     * @param request
     * @return
     */
    Boolean batchAdd(List<Feedback> request);

    /**
     * 删除
     *
     * @param id
     * @return
     */
    Boolean del(Integer id);

    /**
     * 批量删除
     *
     * @param ids 批量ID列表
     * @return 是否成功
     */
    Boolean batchDel(List<Integer> ids);

    /**
     * 编辑
     *
     * @param id      id
     * @param request 请求参数
     * @return
     */
    Boolean edit(Integer id, Feedback request);

    /**
     * 分页条件查询
     *
     * @return
     */
    PageResult<List<FeedbackVO>> queryPage(Integer pageNum, Integer pageSize, String title, Integer status);

    /**
     * 查询全部
     *
     * @return
     */
    List<Feedback> getAll();

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    Feedback getByIdDetail(Integer id);

    /**
     * 根据用户ID分页查询
     * @param pageNum
     * @param pageSize
     * @param title
     * @param status
     * @param userId
     * @return
     */
    PageResult<List<FeedbackVO>> pageByUserId(Integer pageNum, Integer pageSize, String title, Integer status, Integer userId);
}