package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.example.backend.common.model.PageResult;
import com.example.backend.utils.BackendAuthHelper;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.vo.culturalheritage.CulturalHeritageVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import com.example.backend.service.ICulturalHeritageService;
import com.example.backend.entity.CulturalHeritage;

import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 非遗文物表控制层
 * @Version 1.0
 */
@RestController
@RequestMapping("/culturalHeritage")
public class CulturalHeritageController {

    @Resource
    private ICulturalHeritageService culturalHeritageService;

    @PostMapping("/add")
    public BaseResponse<Integer> add(@RequestBody CulturalHeritage request) {
        Integer res = culturalHeritageService.add(request);
        return Result.success(res);
    }

    @PostMapping("/batchAdd")
    public BaseResponse<Boolean> batchAdd(@RequestBody List<CulturalHeritage> request) {
        Boolean res = culturalHeritageService.batchAdd(request);
        return Result.success(res);
    }

    @GetMapping("/del")
    public BaseResponse<Boolean> del(@RequestParam Integer id) {
        Boolean res = culturalHeritageService.del(id);
        return Result.success(res);
    }

    @PostMapping("/batchDel")
    public BaseResponse<Boolean> batchDel(@RequestBody List<Integer> ids) {
        Boolean res = culturalHeritageService.batchDel(ids);
        return Result.success(res);
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> edit(@RequestParam Integer id, @RequestBody CulturalHeritage request) {
        Boolean res = culturalHeritageService.edit(id, request);
        return Result.success(res);
    }

    @SaIgnore
    @GetMapping("/page")
    public BaseResponse<PageResult<List<CulturalHeritageVO>>> page(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer creatorId,
            @RequestParam(required = false) Integer categoryId
    ) {
        PageResult<List<CulturalHeritageVO>> res = culturalHeritageService.queryPage(pageNum, pageSize, name, creatorId, categoryId);
        return Result.success(res);
    }

    /**
     * 后台管理分页（需登录管理员或传承人）；传承人仅能看到本人发布的数据
     */
    @GetMapping("/manage/page")
    public BaseResponse<PageResult<List<CulturalHeritageVO>>> managePage(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String name
    ) {
        Integer scope = BackendAuthHelper.inheritorCreatorScopeOrNull();
        PageResult<List<CulturalHeritageVO>> res = culturalHeritageService.queryPage(pageNum, pageSize, name, scope);
        return Result.success(res);
    }

    @SaIgnore
    @GetMapping("/list")
    public BaseResponse<List<CulturalHeritage>> all() {
        List<CulturalHeritage> res = culturalHeritageService.getAll();
        return Result.success(res);
    }

    @SaIgnore
    @GetMapping("/getById")
    public BaseResponse<CulturalHeritage> getById(@RequestParam Integer id) {
        CulturalHeritage res = culturalHeritageService.getByIdDetail(id);
        return Result.success(res);
    }

    @SaIgnore
    @GetMapping("/hot")
    @ApiOperation(value = "获取热门非遗文物")
    public BaseResponse<PageResult<List<CulturalHeritageVO>>> hot(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "8") Integer pageSize,
            @RequestParam(defaultValue = "view_count") String orderBy,
            @RequestParam(defaultValue = "desc") String orderType
    ) {
        PageResult<List<CulturalHeritageVO>> res = culturalHeritageService.getHotPage(pageNum, pageSize, orderBy, orderType);
        return Result.success(res);
    }
}