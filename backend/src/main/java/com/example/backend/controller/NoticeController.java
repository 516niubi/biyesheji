package com.example.backend.controller;

import com.example.backend.common.model.PageResult;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.vo.notice.NoticeVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import com.example.backend.service.INoticeService;
import com.example.backend.entity.Notice;

import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 公告表控制层
 * @Version 1.0
 */
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Resource
    private INoticeService noticeService;

    @PostMapping("/add")
    public BaseResponse<Integer> add(@RequestBody Notice request) {
        Integer res = noticeService.add(request);
        return Result.success(res);
    }

    @PostMapping("/batchAdd")
    public BaseResponse<Boolean> batchAdd(@RequestBody List<Notice> request) {
        Boolean res = noticeService.batchAdd(request);
        return Result.success(res);
    }

    @GetMapping("/del")
    public BaseResponse<Boolean> del(@RequestParam Integer id) {
        Boolean res = noticeService.del(id);
        return Result.success(res);
    }

    @PostMapping("/batchDel")
    @ApiOperation(value = "批量删除公告")
    public BaseResponse<Boolean> batchDel(@RequestBody List<Integer> ids) {
        Boolean res = noticeService.batchDel(ids);
        return Result.success(res);
    }

    @PostMapping("/edit")
    private BaseResponse<Boolean> edit(@RequestParam Integer id, @RequestBody Notice request) {
        Boolean res = noticeService.edit(id, request);
        return Result.success(res);
    }

    @GetMapping("/page")
    public BaseResponse<PageResult<List<NoticeVO>>> page(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String title
    ) {
        PageResult<List<NoticeVO>> res = noticeService.queryPage(pageNum, pageSize, title);
        return Result.success(res);
    }

    @GetMapping("/list")
    public BaseResponse<List<Notice>> all() {
        List<Notice> res = noticeService.getAll();
        return Result.success(res);
    }

    @GetMapping("/getById")
    public BaseResponse<Notice> getById(@RequestParam Integer id) {
        Notice res = noticeService.getByIdDetail(id);
        return Result.success(res);
    }
}

