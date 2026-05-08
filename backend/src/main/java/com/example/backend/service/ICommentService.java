package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.Comment;
import com.example.backend.entity.request.comment.CommentAddRequest;
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

    /** 前台登录用户或传承人发文（图文、回复） */
    boolean addRich(CommentAddRequest request);
    
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
     * @param heritageCreatorAdminId 非空时仅查询挂在该传承人文物下的评论
     */
    Page<CommentVO> selectPage(Integer pageNum, Integer pageSize, String userName, Integer heritageCreatorAdminId);

    /**
     * 传承人工作室：分页仅主楼（parent_id 为空），每条记录携带嵌套子回复
     */
    Page<CommentVO> selectManageRootTreePage(Integer pageNum, Integer pageSize, String userName, Integer heritageCreatorAdminId);

    default Page<CommentVO> selectPage(Integer pageNum, Integer pageSize, String userName) {
        return selectPage(pageNum, pageSize, userName, null);
    }
    
    /**
     * 根据文物ID获取所有评论
     * @param heritageId 文物ID
     * @return 评论列表
     */
    List<CommentVO> selectByHeritageId(Integer heritageId);

    /** 前台用户：别人回复了我发表的评论 */
    Page<CommentVO> selectRepliesToUser(Integer userId, Integer pageNum, Integer pageSize);

    /** 传承人账号：别人回复了我以传承人身份发表的评论 */
    Page<CommentVO> selectRepliesToInheritor(Integer inheritorId, Integer pageNum, Integer pageSize);

    /** 当前登录用户/传承人：我发表过的评论（含回复） */
    Page<CommentVO> selectMyPosts(Integer pageNum, Integer pageSize);

    /** 前台用户或传承人删除本人评论（含子回复） */
    boolean deleteMyComment(Integer id);

    /** 当前登录账号收到的回复（兼容 JWT 下 Session 无法还原 User 对象） */
    Page<CommentVO> selectMyRepliesForCurrentLogin(Integer pageNum, Integer pageSize);
}