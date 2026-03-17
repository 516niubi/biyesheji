package com.example.backend.service;

import com.example.backend.entity.IchType;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.common.model.PageResult;
import com.example.backend.entity.vo.ichtype.IchTypeVO;

import java.util.List;

/**
 * <p>
 * 非遗分类表 服务类
 * </p>
 *
 * @author 
 * @since 2025-01-20
 */
public interface IIchTypeService extends IService<IchType> {
        /**
        * 新增
        * @param request
        * @return
        */
        Integer add(IchType request);

        /**
         * 批量新增
         *
         * @param request
         * @return
         */
        Boolean batchAdd(List<IchType> request);

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
        Boolean edit(Integer id, IchType request);

        /**
         * 分页条件查询
         * @return
         */
        PageResult<List<IchTypeVO>> queryPage(Integer pageNum, Integer pageSize, String name);

        /**
         * 查询全部
         * @return
         */
        List<IchType> getAll();

        /**
         * 根据id查询
         *
         * @param id
         * @return
         */
        IchType getByIdDetail(Integer id);
}