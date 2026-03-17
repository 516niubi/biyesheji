package com.example.backend.controller;

import com.example.backend.common.model.PageResult;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.vo.ichtype.IchTypeVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import com.example.backend.service.IIchTypeService;
import com.example.backend.entity.IchType;

import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 非遗分类表控制层
 * @Version 1.0
 */
@RestController
@RequestMapping("/ichType")
public class IchTypeController {

    @Resource
    private IIchTypeService ichTypeService;

    @PostMapping("/add")
    public BaseResponse<Integer> add(@RequestBody IchType request) {
        Integer res = ichTypeService.add(request);
        return Result.success(res);
    }

    @PostMapping("/batchAdd")
    public BaseResponse<Boolean> batchAdd(@RequestBody List<IchType> request) {
        Boolean res = ichTypeService.batchAdd(request);
        return Result.success(res);
    }

    @GetMapping("/del")
    public BaseResponse<Boolean> del(@RequestParam Integer id) {
        Boolean res = ichTypeService.del(id);
        return Result.success(res);
    }

    @PostMapping("/batchDel")
    @ApiOperation(value = "批量删除非遗分类")
    public BaseResponse<Boolean> batchDel(@RequestBody List<Integer> ids) {
        Boolean res = ichTypeService.batchDel(ids);
        return Result.success(res);
    }

    @PostMapping("/edit")
    private BaseResponse<Boolean> edit(@RequestParam Integer id, @RequestBody IchType request) {
        Boolean res = ichTypeService.edit(id, request);
        return Result.success(res);
    }

    @GetMapping("/page")
    public BaseResponse<PageResult<List<IchTypeVO>>> page(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String name
    ) {
        PageResult<List<IchTypeVO>> res = ichTypeService.queryPage(pageNum, pageSize, name);
        return Result.success(res);
    }

    @GetMapping("/list")
    public BaseResponse<List<IchType>> all() {
        List<IchType> res = ichTypeService.getAll();
        return Result.success(res);
    }

    @GetMapping("/getById")
    public BaseResponse<IchType> getById(@RequestParam Integer id) {
        IchType res = ichTypeService.getByIdDetail(id);
        return Result.success(res);
    }
}