package com.example.backend.service;

import com.example.backend.entity.Video;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.common.model.PageResult;
import com.example.backend.entity.vo.video.VideoVO;

import java.util.List;

/**
 * <p>
 * 宣传视频表 服务类
 * </p>
 *
 * @author 
 * @since 2025-01-20
 */
public interface IVideoService extends IService<Video> {
        /**
        * 新增
        * @param request
        * @return
        */
        Integer add(Video request);

        /**
         * 批量新增
         *
         * @param request
         * @return
         */
        Boolean batchAdd(List<Video> request);

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
        Boolean edit(Integer id, Video request);

        /**
         * 分页条件查询
         * @return
         */
        PageResult<List<VideoVO>> queryPage(Integer pageNum, Integer pageSize, String title);

        /**
         * 查询全部
         * @return
         */
        List<Video> getAll();

        /**
         * 根据id查询
         *
         * @param id
         * @return
         */
        Video getByIdDetail(Integer id);
}