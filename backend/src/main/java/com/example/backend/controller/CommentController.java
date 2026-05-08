package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.utils.BackendAuthHelper;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.Comment;
import com.example.backend.entity.request.comment.CommentAddRequest;
import com.example.backend.service.ICommentService;
import com.example.backend.entity.vo.comment.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/comment")
public class CommentController {
    
    @Autowired
    private ICommentService commentService;
    
    /**
     * 新增评论（前台登录用户或传承人；支持图文、回复）
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> add(@RequestBody CommentAddRequest request) {
        boolean result = commentService.addRich(request);
        return Result.success(result);
    }

    /**
     * 我收到的回复（前台用户或传承人账号）
     */
    @GetMapping("/my/replies")
    public BaseResponse<Page<CommentVO>> myReplies(
            @RequestParam Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize) {
        return Result.success(commentService.selectMyRepliesForCurrentLogin(pageNum, pageSize));
    }

    /**
     * 我发表的评论（含回复他人的楼层）
     */
    @GetMapping("/my/posts")
    public BaseResponse<Page<CommentVO>> myPosts(
            @RequestParam Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize) {
        return Result.success(commentService.selectMyPosts(pageNum, pageSize));
    }

    /**
     * 删除本人评论（同时删除其下所有回复）
     */
    @DeleteMapping("/my/{id}")
    public BaseResponse<Boolean> deleteMy(@PathVariable Integer id) {
        return Result.success(commentService.deleteMyComment(id));
    }
    
    /**
     * 删除评论
     */
    @DeleteMapping("/delete/{id}")
    public BaseResponse<Boolean> delete(@PathVariable Integer id) {
        boolean result = commentService.deleteById(id);
        return Result.success(result);
    }
    
    /**
     * 批量删除评论
     */
    @DeleteMapping("/delete/batch")
    public BaseResponse<Boolean> deleteBatch(@RequestBody List<Integer> ids) {
        boolean result = commentService.deleteBatch(ids);
        return Result.success(result);
    }
    
    /**
     * 更新评论
     */
    @PutMapping("/update")
    public BaseResponse<Boolean> update(@RequestBody Comment comment) {
        boolean result = commentService.updateById(comment);
        return Result.success(result);
    }
    
    /**
     * 根据ID查询评论
     */
    @GetMapping("/selectById/{id}")
    public BaseResponse<Comment> selectById(@PathVariable Integer id) {
        Comment comment = commentService.selectById(id);
        return Result.success(comment);
    }
    
    /**
     * 查询所有评论
     */
    @GetMapping("/selectAll")
    public BaseResponse<List<Comment>> selectAll() {
        List<Comment> list = commentService.selectAll();
        return Result.success(list);
    }
    
    /**
     * 分页查询评论
     */
    @GetMapping("/selectPage")
    public BaseResponse<Page<CommentVO>> selectPage(@RequestParam Integer pageNum,
                                                    @RequestParam Integer pageSize,
                                                    @RequestParam(required = false) String userName) {
        Page<CommentVO> page = commentService.selectPage(pageNum, pageSize, userName, null);
        return Result.success(page);
    }

    @GetMapping("/manage/selectPage")
    public BaseResponse<Page<CommentVO>> manageSelectPage(@RequestParam Integer pageNum,
                                                          @RequestParam Integer pageSize,
                                                          @RequestParam(required = false) String userName) {
        Integer scope = BackendAuthHelper.inheritorCreatorScopeOrNull();
        Page<CommentVO> page = commentService.selectPage(pageNum, pageSize, userName, scope);
        return Result.success(page);
    }

    /**
     * 传承人工作室：主楼分页 + 嵌套回复树（前台专用，不影响后台表格）
     */
    @GetMapping("/manage/selectRootTreePage")
    public BaseResponse<Page<CommentVO>> manageSelectRootTreePage(@RequestParam Integer pageNum,
                                                                  @RequestParam Integer pageSize,
                                                                  @RequestParam(required = false) String userName) {
        Integer scope = BackendAuthHelper.inheritorCreatorScopeOrNull();
        Page<CommentVO> page = commentService.selectManageRootTreePage(pageNum, pageSize, userName, scope);
        return Result.success(page);
    }
    
    /**
     * 根据文物ID获取所有评论
     */
    @SaIgnore
    @GetMapping("/selectByHeritageId/{heritageId}")
    public BaseResponse<List<CommentVO>> selectByHeritageId(@PathVariable Integer heritageId) {
        List<CommentVO> list = commentService.selectByHeritageId(heritageId);
        return Result.success(list);
    }
}