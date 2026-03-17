package com.example.backend.controller;

import com.example.backend.common.model.PageResult;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.vo.collect.CollectVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import com.example.backend.service.ICollectService;
import com.example.backend.entity.Collect;

import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 收藏表控制层
 * @Version 1.0
 */
@RestController
@RequestMapping("/collect")
public class CollectController {

    @Resource
    private ICollectService collectService;

    @PostMapping("/add")
    public BaseResponse<Integer> add(@RequestBody Collect request) {
        Integer res = collectService.add(request);
        return Result.success(res);
    }

    @PostMapping("/batchAdd")
    public BaseResponse<Boolean> batchAdd(@RequestBody List<Collect> request) {
        Boolean res = collectService.batchAdd(request);
        return Result.success(res);
    }

    @GetMapping("/del")
    public BaseResponse<Boolean> del(@RequestParam("id") Integer id) {
        Boolean res = collectService.del(id);
        return Result.success(res);
    }

    @PostMapping("/batchDel")
    @ApiOperation(value = "批量删除收藏")
    public BaseResponse<Boolean> batchDel(@RequestBody List<Integer> ids) {
        Boolean res = collectService.batchDel(ids);
        return Result.success(res);
    }

    @PostMapping("/edit")
    private BaseResponse<Boolean> edit(@RequestParam Integer id, @RequestBody Collect request) {
        Boolean res = collectService.edit(id, request);
        return Result.success(res);
    }

    @GetMapping("/page")
    public BaseResponse<PageResult<List<CollectVO>>> page(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String userName
    ) {
        PageResult<List<CollectVO>> res = collectService.queryPage(pageNum, pageSize, userName);
        return Result.success(res);
    }

    @GetMapping("/list")
    public BaseResponse<List<Collect>> all() {
        List<Collect> res = collectService.getAll();
        return Result.success(res);
    }

    @GetMapping("/getById")
    public BaseResponse<Collect> getById(@RequestParam Integer id) {
        Collect res = collectService.getByIdDetail(id);
        return Result.success(res);
    }
    
    @GetMapping("/isCollected")
    @ApiOperation(value = "判断用户是否收藏指定文物")
    public BaseResponse<Boolean> isCollected(@RequestParam Integer userId, @RequestParam Integer heritageId) {
        Boolean res = collectService.isCollected(userId, heritageId);
        return Result.success(res);
    }
    
    @GetMapping("/pageByUserId")
    @ApiOperation(value = "分页获取指定用户收藏的文物")
    public BaseResponse<PageResult<List<CollectVO>>> pageByUserId(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam Integer userId
    ) {
        PageResult<List<CollectVO>> res = collectService.queryPageByUserId(pageNum, pageSize, userId);
        return Result.success(res);
    }
}