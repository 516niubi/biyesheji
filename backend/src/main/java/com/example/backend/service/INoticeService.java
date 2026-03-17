package com.example.backend.service;

import com.example.backend.entity.Notice;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.common.model.PageResult;
import com.example.backend.entity.vo.notice.NoticeVO;

import java.util.List;

/**
 * <p>
 * 公告表 服务类
 * </p>
 *
 * @author 
 * @since 2025-05-04
 */
public interface INoticeService extends IService<Notice> {
        /**
        * 新增
        * @param request
        * @return
        */
        Integer add(Notice request);

        /**
         * 批量新增
         *
         * @param request
         * @return
         */
        Boolean batchAdd(List<Notice> request);

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
        Boolean edit(Integer id, Notice request);

        /**
         * 分页条件查询
         * @return
         */
        PageResult<List<NoticeVO>> queryPage(Integer pageNum, Integer pageSize, String title);

        /**
         * 查询全部
         * @return
         */
        List<Notice> getAll();

        /**
         * 根据id查询
         *
         * @param id
         * @return
         */
        Notice getByIdDetail(Integer id);
}
