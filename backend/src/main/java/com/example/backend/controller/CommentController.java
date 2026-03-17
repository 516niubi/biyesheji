package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.Comment;
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
     * 新增评论
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> add(@RequestBody Comment comment) {
        boolean result = commentService.add(comment);
        return Result.success(result);
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
        Page<CommentVO> page = commentService.selectPage(pageNum, pageSize, userName);
        return Result.success(page);
    }
    
    /**
     * 根据文物ID获取所有评论
     */
    @GetMapping("/selectByHeritageId/{heritageId}")
    public BaseResponse<List<CommentVO>> selectByHeritageId(@PathVariable Integer heritageId) {
        List<CommentVO> list = commentService.selectByHeritageId(heritageId);
        return Result.success(list);
    }
}