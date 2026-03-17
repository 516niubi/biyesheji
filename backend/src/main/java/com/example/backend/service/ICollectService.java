package com.example.backend.service;

import com.example.backend.entity.Collect;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.common.model.PageResult;
import com.example.backend.entity.vo.collect.CollectVO;

import java.util.List;

/**
 * <p>
 * 收藏表 服务类
 * </p>
 *
 * @author 
 * @since 2025-01-29
 */
public interface ICollectService extends IService<Collect> {
        /**
        * 新增
        * @param request
        * @return
        */
        Integer add(Collect request);

        /**
         * 批量新增
         *
         * @param request
         * @return
         */
        Boolean batchAdd(List<Collect> request);

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
        Boolean edit(Integer id, Collect request);

        /**
         * 分页条件查询
         * @return
         */
        PageResult<List<CollectVO>> queryPage(Integer pageNum, Integer pageSize, String userName);

        /**
         * 查询全部
         * @return
         */
        List<Collect> getAll();

        /**
         * 根据id查询
         *
         * @param id
         * @return
         */
        Collect getByIdDetail(Integer id);
        
        /**
         * 判断用户是否收藏指定文物
         * @param userId 用户ID
         * @param heritageId 文物ID
         * @return 是否收藏
         */
        Boolean isCollected(Integer userId, Integer heritageId);
        
        /**
         * 分页获取指定用户收藏的文物
         * @param pageNum 页码
         * @param pageSize 每页大小
         * @param userId 用户ID
         * @return 分页结果
         */
        PageResult<List<CollectVO>> queryPageByUserId(Integer pageNum, Integer pageSize, Integer userId);
}