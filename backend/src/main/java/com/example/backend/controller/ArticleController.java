package com.example.backend.controller;

import com.example.backend.common.model.PageResult;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.vo.article.ArticleVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import com.example.backend.service.IArticleService;
import com.example.backend.entity.Article;

import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 资讯表控制层
 * @Version 1.0
 */
@RestController
@RequestMapping("/article")
public class ArticleController {

    @Resource
    private IArticleService articleService;

    @PostMapping("/add")
    public BaseResponse<Integer> add(@RequestBody Article request) {
        Integer res = articleService.add(request);
        return Result.success(res);
    }

    @PostMapping("/batchAdd")
    public BaseResponse<Boolean> batchAdd(@RequestBody List<Article> request) {
        Boolean res = articleService.batchAdd(request);
        return Result.success(res);
    }

    @GetMapping("/del")
    public BaseResponse<Boolean> del(@RequestParam Integer id) {
        Boolean res = articleService.del(id);
        return Result.success(res);
    }

    @PostMapping("/batchDel")
    @ApiOperation(value = "批量删除资讯")
    public BaseResponse<Boolean> batchDel(@RequestBody List<Integer> ids) {
        Boolean res = articleService.batchDel(ids);
        return Result.success(res);
    }

    @PostMapping("/edit")
    private BaseResponse<Boolean> edit(@RequestParam Integer id, @RequestBody Article request) {
        Boolean res = articleService.edit(id, request);
        return Result.success(res);
    }

    @GetMapping("/page")
    public BaseResponse<PageResult<List<ArticleVO>>> page(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String intro
    ) {
        PageResult<List<ArticleVO>> res = articleService.queryPage(pageNum, pageSize, title, intro);
        return Result.success(res);
    }

    @GetMapping("/list")
    public BaseResponse<List<Article>> all() {
        List<Article> res = articleService.getAll();
        return Result.success(res);
    }

    @GetMapping("/getById")
    public BaseResponse<Article> getById(@RequestParam Integer id) {
        Article res = articleService.getByIdDetailAndIncreaseViewCount(id);
        return Result.success(res);
    }

    @GetMapping("/hot")
    @ApiOperation(value = "获取热门资讯")
    public BaseResponse<PageResult<List<ArticleVO>>> hot(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "8") Integer pageSize,
            @RequestParam(defaultValue = "view_count") String orderBy,
            @RequestParam(defaultValue = "desc") String orderType
    ) {
        PageResult<List<ArticleVO>> res = articleService.getHotPage(pageNum, pageSize, orderBy, orderType);
        return Result.success(res);
    }
}