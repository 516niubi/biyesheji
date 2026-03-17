package com.example.backend.service;

import com.example.backend.entity.CulturalHeritage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.common.model.PageResult;
import com.example.backend.entity.vo.culturalheritage.CulturalHeritageVO;

import java.util.List;

/**
 * <p>
 * 非遗文物表 服务类
 * </p>
 *
 * @author 
 * @since 2025-01-29
 */
public interface ICulturalHeritageService extends IService<CulturalHeritage> {
        /**
        * 新增
        * @param request
        * @return
        */
        Integer add(CulturalHeritage request);

        /**
         * 批量新增
         *
         * @param request
         * @return
         */
        Boolean batchAdd(List<CulturalHeritage> request);

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
        Boolean edit(Integer id, CulturalHeritage request);

        /**
         * 分页条件查询
         * @return
         */
        PageResult<List<CulturalHeritageVO>> queryPage(Integer pageNum, Integer pageSize, String name);

        /**
         * 查询全部
         * @return
         */
        List<CulturalHeritage> getAll();

        /**
         * 根据id查询
         *
         * @param id
         * @return
         */
        CulturalHeritage getByIdDetail(Integer id);

        /**
         * 获取热门非遗文物分页数据
         *
         * @param pageNum   页码
         * @param pageSize  页大小
         * @param orderBy   排序字段
         * @param orderType 排序类型
         * @return
         */
        PageResult<List<CulturalHeritageVO>> getHotPage(Integer pageNum, Integer pageSize, String orderBy, String orderType);
}