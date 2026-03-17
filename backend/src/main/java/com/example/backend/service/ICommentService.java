package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.Comment;
import com.example.backend.entity.vo.comment.CommentVO;

import java.util.List;

/**
 * 评论服务接口
 */
public interface ICommentService extends IService<Comment> {
    
    /**
     * 新增评论
     * @param comment 评论信息
     * @return 是否成功
     */
    boolean add(Comment comment);
    
    /**
     * 删除评论
     * @param id 评论ID
     * @return 是否成功
     */
    boolean deleteById(Integer id);
    
    /**
     * 批量删除评论
     * @param ids 评论ID列表
     * @return 是否成功
     */
    boolean deleteBatch(List<Integer> ids);
    
    /**
     * 更新评论
     * @param comment 评论信息
     * @return 是否成功
     */
    boolean updateById(Comment comment);
    
    /**
     * 根据ID查询评论
     * @param id 评论ID
     * @return 评论信息
     */
    Comment selectById(Integer id);
    
    /**
     * 查询所有评论
     * @return 评论列表
     */
    List<Comment> selectAll();
    
    /**
     * 分页查询评论
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userName 用户名（模糊查询）
     * @return 分页结果
     */
    Page<CommentVO> selectPage(Integer pageNum, Integer pageSize, String userName);
    
    /**
     * 根据文物ID获取所有评论
     * @param heritageId 文物ID
     * @return 评论列表
     */
    List<CommentVO> selectByHeritageId(Integer heritageId);
}