package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.common.model.PageResult;
import com.example.backend.entity.Banner;

import java.util.List;

/**
 * <p>
 * 轮播图表 服务类
 * </p>
 *
 * @author
 */
public interface IBannerService extends IService<Banner> {

    /**
     * 新增
     *
     * @param request
     * @return
     */
    Integer add(Banner request);

    /**
     * 批量新增
     *
     * @param request
     * @return
     */
    Boolean batchAdd(List<Banner> request);

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
     * @param ids
     * @return
     */
    Boolean batchDel(List<Integer> ids);

    /**
     * 编辑
     *
     * @param id
     * @param request
     * @return
     */
    Boolean edit(Integer id, Banner request);

    /**
     * 分页查询
     *
     * @param pageNum
     * @param pageSize
     * @param title
     * @return
     */
    PageResult page(Integer pageNum, Integer pageSize, String title);

    /**
     * 查询所有轮播图
     *
     * @return
     */
    List<Banner> list();
} 