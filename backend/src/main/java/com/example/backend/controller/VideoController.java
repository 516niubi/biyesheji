package com.example.backend.controller;

import com.example.backend.common.model.PageResult;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.vo.video.VideoVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import com.example.backend.service.IVideoService;
import com.example.backend.entity.Video;

import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 宣传视频表控制层
 * @Version 1.0
 */
@RestController
@RequestMapping("/video")
public class VideoController {

    @Resource
    private IVideoService videoService;

    @PostMapping("/add")
    public BaseResponse<Integer> add(@RequestBody Video request) {
        Integer res = videoService.add(request);
        return Result.success(res);
    }

    @PostMapping("/batchAdd")
    public BaseResponse<Boolean> batchAdd(@RequestBody List<Video> request) {
        Boolean res = videoService.batchAdd(request);
        return Result.success(res);
    }

    @GetMapping("/del")
    public BaseResponse<Boolean> del(@RequestParam Integer id) {
        Boolean res = videoService.del(id);
        return Result.success(res);
    }

    @PostMapping("/batchDel")
    @ApiOperation(value = "批量删除宣传视频")
    public BaseResponse<Boolean> batchDel(@RequestBody List<Integer> ids) {
        Boolean res = videoService.batchDel(ids);
        return Result.success(res);
    }

    @PostMapping("/edit")
    private BaseResponse<Boolean> edit(@RequestParam Integer id, @RequestBody Video request) {
        Boolean res = videoService.edit(id, request);
        return Result.success(res);
    }

    @GetMapping("/page")
    public BaseResponse<PageResult<List<VideoVO>>> page(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String title
    ) {
        PageResult<List<VideoVO>> res = videoService.queryPage(pageNum, pageSize, title);
        return Result.success(res);
    }

    @GetMapping("/list")
    public BaseResponse<List<Video>> all() {
        List<Video> res = videoService.getAll();
        return Result.success(res);
    }

    @GetMapping("/getById")
    public BaseResponse<Video> getById(@RequestParam Integer id) {
        Video res = videoService.getByIdDetail(id);
        return Result.success(res);
    }
}