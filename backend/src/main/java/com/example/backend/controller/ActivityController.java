package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.example.backend.common.model.PageResult;
import com.example.backend.utils.BackendAuthHelper;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.vo.activity.ActivityVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import com.example.backend.service.IActivityService;
import com.example.backend.entity.Activity;

import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 活动表控制层
 * @Version 1.0
 */
@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Resource
    private IActivityService activityService;

    @PostMapping("/add")
    public BaseResponse<Integer> add(@RequestBody Activity request) {
        Integer res = activityService.add(request);
        return Result.success(res);
    }

    @PostMapping("/batchAdd")
    public BaseResponse<Boolean> batchAdd(@RequestBody List<Activity> request) {
        Boolean res = activityService.batchAdd(request);
        return Result.success(res);
    }

    @GetMapping("/del")
    public BaseResponse<Boolean> del(@RequestParam Integer id) {
        Boolean res = activityService.del(id);
        return Result.success(res);
    }

    @PostMapping("/batchDel")
    @ApiOperation(value = "批量删除活动")
    public BaseResponse<Boolean> batchDel(@RequestBody List<Integer> ids) {
        Boolean res = activityService.batchDel(ids);
        return Result.success(res);
    }

    @PostMapping("/edit")
    private BaseResponse<Boolean> edit(@RequestParam Integer id, @RequestBody Activity request) {
        Boolean res = activityService.edit(id, request);
        return Result.success(res);
    }

    @SaIgnore
    @GetMapping("/page")
    public BaseResponse<PageResult<List<ActivityVO>>> page(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer creatorId
    ) {
        PageResult<List<ActivityVO>> res = activityService.queryPage(pageNum, pageSize, title, creatorId);
        return Result.success(res);
    }

    @GetMapping("/manage/page")
    public BaseResponse<PageResult<List<ActivityVO>>> managePage(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String title
    ) {
        Integer scope = BackendAuthHelper.inheritorCreatorScopeOrNull();
        PageResult<List<ActivityVO>> res = activityService.queryPage(pageNum, pageSize, title, scope);
        return Result.success(res);
    }

    @SaIgnore
    @GetMapping("/list")
    public BaseResponse<List<Activity>> all() {
        List<Activity> res = activityService.getAll();
        return Result.success(res);
    }

    @SaIgnore
    @GetMapping("/getById")
    public BaseResponse<Activity> getById(@RequestParam Integer id) {
        Activity res = activityService.getByIdDetail(id);
        return Result.success(res);
    }

    @SaIgnore
    @GetMapping("/hot")
    @ApiOperation(value = "获取热门活动")
    public BaseResponse<PageResult<List<ActivityVO>>> hot(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "8") Integer pageSize,
            @RequestParam(defaultValue = "view_count") String orderBy,
            @RequestParam(defaultValue = "desc") String orderType
    ) {
        PageResult<List<ActivityVO>> res = activityService.getHotPage(pageNum, pageSize, orderBy, orderType);
        return Result.success(res);
    }
}