package com.example.backend.controller;

import com.example.backend.common.model.PageResult;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.ActivityApplication;
import com.example.backend.entity.vo.activityApplication.ActivityApplicationVO;
import com.example.backend.service.IActivityApplicationService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 活动报名表控制层
 * @Version 1.0
 */
@RestController
@RequestMapping("/activityApplication")
public class ActivityApplicationController {

    @Resource
    private IActivityApplicationService activityApplicationService;

    @PostMapping("/add")
    public BaseResponse<Integer> add(@RequestBody ActivityApplication request) {
        Integer res = activityApplicationService.add(request);
        return Result.success(res);
    }

    @PostMapping("/batchAdd")
    public BaseResponse<Boolean> batchAdd(@RequestBody List<ActivityApplication> request) {
        Boolean res = activityApplicationService.batchAdd(request);
        return Result.success(res);
    }

    @GetMapping("/del")
    public BaseResponse<Boolean> del(@RequestParam Integer id) {
        Boolean res = activityApplicationService.del(id);
        return Result.success(res);
    }

    @PostMapping("/batchDel")
    @ApiOperation(value = "批量删除活动报名")
    public BaseResponse<Boolean> batchDel(@RequestBody List<Integer> ids) {
        Boolean res = activityApplicationService.batchDel(ids);
        return Result.success(res);
    }

    @PostMapping("/edit")
    private BaseResponse<Boolean> edit(@RequestParam Integer id, @RequestBody ActivityApplication request) {
        Boolean res = activityApplicationService.edit(id, request);
        return Result.success(res);
    }

    @GetMapping("/page")
    public BaseResponse<PageResult<List<ActivityApplicationVO>>> page(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status
    ) {
        PageResult<List<ActivityApplicationVO>> res = activityApplicationService.queryPage(pageNum, pageSize, realName, phone, status);
        return Result.success(res);
    }

    @GetMapping("/pageByUserId")
    public BaseResponse<PageResult<List<ActivityApplicationVO>>> pageByUserId(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam Integer userId,
            @RequestParam(required = false) String realName
    ) {
        PageResult<List<ActivityApplicationVO>> res = activityApplicationService.pageByUserId(pageNum, pageSize, realName, userId);
        return Result.success(res);
    }


    @GetMapping("/list")
    public BaseResponse<List<ActivityApplication>> all() {
        List<ActivityApplication> res = activityApplicationService.getAll();
        return Result.success(res);
    }

    @GetMapping("/getById")
    public BaseResponse<ActivityApplication> getById(@RequestParam Integer id) {
        ActivityApplication res = activityApplicationService.getByIdDetail(id);
        return Result.success(res);
    }
    
    @PostMapping("/apply")
    @ApiOperation(value = "用户报名活动")
    public BaseResponse<Boolean> applyActivity(@RequestBody ActivityApplication request) {
        Boolean res = activityApplicationService.applyActivity(request);
        return Result.success(res);
    }
    
    @PostMapping("/audit")
    @ApiOperation(value = "审核报名")
    public BaseResponse<Boolean> auditApplication(@RequestParam Integer id, @RequestParam Integer status) {
        Boolean res = activityApplicationService.auditApplication(id, status);
        return Result.success(res);
    }
}