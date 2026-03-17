package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.example.backend.common.model.PageResult;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.Banner;
import com.example.backend.service.IBannerService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 轮播图表控制层
 * @Version 1.0
 */
@RestController
@RequestMapping("/banner")
public class BannerController {

    @Resource
    private IBannerService bannerService;

    @PostMapping("/add")
    @ApiOperation(value = "新增轮播图")
    public BaseResponse<Integer> add(@RequestBody Banner request) {
        Integer res = bannerService.add(request);
        return Result.success(res);
    }

    @PostMapping("/batchAdd")
    @ApiOperation(value = "批量新增轮播图")
    public BaseResponse<Boolean> batchAdd(@RequestBody List<Banner> request) {
        Boolean res = bannerService.batchAdd(request);
        return Result.success(res);
    }

    @GetMapping("/del")
    @ApiOperation(value = "删除轮播图")
    public BaseResponse<Boolean> del(@RequestParam Integer id) {
        Boolean res = bannerService.del(id);
        return Result.success(res);
    }

    @PostMapping("/batchDel")
    @ApiOperation(value = "批量删除轮播图")
    public BaseResponse<Boolean> batchDel(@RequestBody List<Integer> ids) {
        Boolean res = bannerService.batchDel(ids);
        return Result.success(res);
    }

    @PostMapping("/edit")
    @ApiOperation(value = "编辑轮播图")
    public BaseResponse<Boolean> edit(@RequestParam Integer id, @RequestBody Banner request) {
        Boolean res = bannerService.edit(id, request);
        return Result.success(res);
    }

    @GetMapping("/page")
    @ApiOperation(value = "分页查询轮播图")
    public BaseResponse<PageResult> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) String title) {
        PageResult res = bannerService.page(pageNum, pageSize, title);
        return Result.success(res);
    }

    @SaIgnore
    @GetMapping("/list")
    @ApiOperation(value = "查询所有轮播图")
    public BaseResponse<List<Banner>> list() {
        List<Banner> res = bannerService.list();
        return Result.success(res);
    }
} 