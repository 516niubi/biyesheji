package com.example.backend.controller;

import com.example.backend.common.model.PageResult;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.vo.feedback.FeedbackVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import com.example.backend.service.IFeedbackService;
import com.example.backend.entity.Feedback;

import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 反馈表控制层
 * @Version 1.0
 */
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Resource
    private IFeedbackService feedbackService;

    @PostMapping("/add")
    public BaseResponse<Integer> add(@RequestBody Feedback request) {
        Integer res = feedbackService.add(request);
        return Result.success(res);
    }

    @PostMapping("/batchAdd")
    public BaseResponse<Boolean> batchAdd(@RequestBody List<Feedback> request) {
        Boolean res = feedbackService.batchAdd(request);
        return Result.success(res);
    }

    @GetMapping("/del")
    public BaseResponse<Boolean> del(@RequestParam Integer id) {
        Boolean res = feedbackService.del(id);
        return Result.success(res);
    }

    @PostMapping("/batchDel")
    @ApiOperation(value = "批量删除反馈")
    public BaseResponse<Boolean> batchDel(@RequestBody List<Integer> ids) {
        Boolean res = feedbackService.batchDel(ids);
        return Result.success(res);
    }

    @PostMapping("/edit")
    private BaseResponse<Boolean> edit(@RequestParam Integer id, @RequestBody Feedback request) {
        Boolean res = feedbackService.edit(id, request);
        return Result.success(res);
    }

    @GetMapping("/page")
    public BaseResponse<PageResult<List<FeedbackVO>>> page(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status
    ) {
        PageResult<List<FeedbackVO>> res = feedbackService.queryPage(pageNum, pageSize, title, status);
        return Result.success(res);
    }

    @GetMapping("/pageByUserId")
    public BaseResponse<PageResult<List<FeedbackVO>>> pageByUserId(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer userId
    ) {
        PageResult<List<FeedbackVO>> res = feedbackService.pageByUserId(pageNum, pageSize, title, status, userId);
        return Result.success(res);
    }

    @GetMapping("/list")
    public BaseResponse<List<Feedback>> all() {
        List<Feedback> res = feedbackService.getAll();
        return Result.success(res);
    }

    @GetMapping("/getById")
    public BaseResponse<Feedback> getById(@RequestParam Integer id) {
        Feedback res = feedbackService.getByIdDetail(id);
        return Result.success(res);
    }
}