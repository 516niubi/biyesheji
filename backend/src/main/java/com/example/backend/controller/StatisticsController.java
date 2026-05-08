package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.*;
import com.example.backend.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Description 统计控制层
 * @Version 1.0
 */
@Api(tags = "统计管理")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Resource
    private ICulturalHeritageService culturalHeritageService;

    @Resource
    private IIchTypeService ichTypeService;
    
    @Resource
    private IVideoService videoService;
    
    @Resource
    private IUserService userService;
    
    @Resource
    private IActivityService activityService;

    @Resource
    private IInheritorService inheritorService;

    /** 传承人审核通过状态，与 InheritorServiceImpl 一致 */
    private static final int INHERITOR_STATUS_APPROVED = 1;

    /**
     * 获取系统重要数据量统计
     */
    @ApiOperation("获取系统重要数据量统计")
    @GetMapping("/overview")
    public BaseResponse<Map<String, Object>> getOverviewStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        // 非遗数量
        long heritageCount = culturalHeritageService.count();
        result.put("heritageCount", heritageCount);
        
        // 宣传视频数量
        long videoCount = videoService.count();
        result.put("videoCount", videoCount);
        
        // 用户数量
        long userCount = userService.count();
        result.put("userCount", userCount);
        
        // 活动数量
        long activityCount = activityService.count();
        result.put("activityCount", activityCount);

        // 非遗传承人（审核通过）
        QueryWrapper<Inheritor> inheritorApproved = new QueryWrapper<>();
        inheritorApproved.eq("status", INHERITOR_STATUS_APPROVED);
        long inheritorCount = inheritorService.count(inheritorApproved);
        result.put("inheritorCount", inheritorCount);
        
        return Result.success(result);
    }

    /**
     * 折线图数据接口 - 最近7天新增数据趋势
     */
    @ApiOperation("获取折线图数据 - 最近7天新增趋势")
    @GetMapping("/line-chart")
    public BaseResponse<Map<String, Object>> getLineChartData() {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> heritageData = new ArrayList<>();
        List<Integer> videoData = new ArrayList<>();
        List<Integer> userData = new ArrayList<>();
        List<Integer> activityData = new ArrayList<>();
        List<Integer> inheritorData = new ArrayList<>();
        
        // 获取最近7天的日期
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.format(formatter));
            
            String dateStr = date.toString();
            
            // 查询当天新增的非遗数量
            QueryWrapper<CulturalHeritage> heritageWrapper = new QueryWrapper<>();
            heritageWrapper.apply("DATE(create_time) = '" + dateStr + "'");
            int heritageCount = (int) culturalHeritageService.count(heritageWrapper);
            heritageData.add(heritageCount);
            
            // 查询当天新增的视频数量
            QueryWrapper<Video> videoWrapper = new QueryWrapper<>();
            videoWrapper.apply("DATE(create_time) = '" + dateStr + "'");
            int videoCount = (int) videoService.count(videoWrapper);
            videoData.add(videoCount);
            
            // 查询当天新增的用户数量
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.apply("DATE(create_time) = '" + dateStr + "'");
            int userCount = (int) userService.count(userWrapper);
            userData.add(userCount);
            
            // 查询当天新增的活动数量
            QueryWrapper<Activity> activityWrapper = new QueryWrapper<>();
            activityWrapper.apply("DATE(create_time) = '" + dateStr + "'");
            int activityCount = (int) activityService.count(activityWrapper);
            activityData.add(activityCount);

            QueryWrapper<Inheritor> inheritorWrapper = new QueryWrapper<>();
            inheritorWrapper.apply("DATE(create_time) = '" + dateStr + "'");
            int inheritorDayCount = (int) inheritorService.count(inheritorWrapper);
            inheritorData.add(inheritorDayCount);
        }
        
        result.put("dates", dates);
        result.put("heritageData", heritageData);
        result.put("videoData", videoData);
        result.put("userData", userData);
        result.put("activityData", activityData);
        result.put("inheritorData", inheritorData);
        
        return Result.success(result);
    }

    /**
     * 柱状图数据接口 - 各类型数据对比
     */
    @ApiOperation("获取柱状图数据 - 各类型数据对比")
    @GetMapping("/bar-chart")
    public BaseResponse<Map<String, Object>> getBarChartData() {
        Map<String, Object> result = new HashMap<>();
        List<String> categories = Arrays.asList("非遗文物", "宣传视频", "注册用户", "活动数量", "非遗传承人");
        List<Integer> data = new ArrayList<>();
        
        // 获取各类型数据总数
        data.add((int) culturalHeritageService.count());
        data.add((int) videoService.count());
        data.add((int) userService.count());
        data.add((int) activityService.count());
        QueryWrapper<Inheritor> inheritorBar = new QueryWrapper<>();
        inheritorBar.eq("status", INHERITOR_STATUS_APPROVED);
        data.add((int) inheritorService.count(inheritorBar));
        
        result.put("categories", categories);
        result.put("data", data);
        
        return Result.success(result);
    }

    /**
     * 饼图数据接口 - 每个分类每个非遗的占比
     */
    @ApiOperation("获取饼图数据 - 每个分类每个非遗的占比")
    @GetMapping("/pie-chart")
    public BaseResponse<List<Map<String, Object>>> getPieChartData() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 查询所有非遗文物及其分类
        List<CulturalHeritage> heritageList = culturalHeritageService.list();

        // 获取所有分类
        List<IchType> allType = ichTypeService.getAll();
        Map<Integer, String> typeMap = new HashMap<>();
        for (IchType type : allType) {
            typeMap.put(type.getId(), type.getName());
        }

        // 按分类统计每个非遗的占比
        Map<Integer, List<CulturalHeritage>> categoryMap = new HashMap<>();
        for (CulturalHeritage heritage : heritageList) {
            Integer category = heritage.getCategoryId() != null ? heritage.getCategoryId() : -1;
            if (category == -1) {
                continue;
            }
            if (!categoryMap.containsKey(category)) {
                ArrayList<CulturalHeritage> list = new ArrayList<>();
                categoryMap.put(category, list);
            }
            categoryMap.get(category).add(heritage);
        }
        
        // 为每个分类构建饼图数据
        for (Map.Entry<Integer, List<CulturalHeritage>> entry : categoryMap.entrySet()) {
            Integer category = entry.getKey();
            if (category == -1) {
                continue;
            }
            List<CulturalHeritage> categoryHeritages = entry.getValue();
            int categoryTotal = categoryHeritages.size();
            Map<String, Object> heritageData = new HashMap<>();
            heritageData.put("name", typeMap.get(category));
            heritageData.put("value", categoryTotal);
            result.add(heritageData);
        }
        
        return Result.success(result);
    }

    /**
     * 热门非遗文物统计 - 按浏览量排序
     */
    @ApiOperation("获取热门非遗文物统计")
    @GetMapping("/hot-heritage")
    public BaseResponse<Map<String, Object>> getHotHeritageData() {
        Map<String, Object> result = new HashMap<>();
        
        // 查询浏览量前10的非遗文物
        QueryWrapper<CulturalHeritage> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("view_count").last("LIMIT 10");
        List<CulturalHeritage> hotHeritageList = culturalHeritageService.list(queryWrapper);
        
        List<String> names = new ArrayList<>();
        List<Integer> viewCounts = new ArrayList<>();
        
        for (CulturalHeritage heritage : hotHeritageList) {
            names.add(heritage.getName());
            viewCounts.add(heritage.getViewCount() != null ? heritage.getViewCount() : 0);
        }
        
        result.put("names", names);
        result.put("viewCounts", viewCounts);
        
        return Result.success(result);
    }

    /**
     * 用户性别分布统计
     */
    @ApiOperation("获取用户性别分布统计")
    @GetMapping("/gender-distribution")
    public BaseResponse<List<Map<String, Object>>> getGenderDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 统计男性用户数量
        QueryWrapper<User> maleWrapper = new QueryWrapper<>();
        maleWrapper.eq("gender", 0);
        long maleCount = userService.count(maleWrapper);
        
        // 统计女性用户数量
        QueryWrapper<User> femaleWrapper = new QueryWrapper<>();
        femaleWrapper.eq("gender", 1);
        long femaleCount = userService.count(femaleWrapper);
        
        // 统计保密用户数量
        QueryWrapper<User> secretWrapper = new QueryWrapper<>();
        secretWrapper.eq("gender", 2);
        long secretCount = userService.count(secretWrapper);
        
        // 构建结果数据
        Map<String, Object> maleItem = new HashMap<>();
        maleItem.put("name", "男性");
        maleItem.put("value", maleCount);
        result.add(maleItem);
        
        Map<String, Object> femaleItem = new HashMap<>();
        femaleItem.put("name", "女性");
        femaleItem.put("value", femaleCount);
        result.add(femaleItem);
        
        Map<String, Object> secretItem = new HashMap<>();
        secretItem.put("name", "保密");
        secretItem.put("value", secretCount);
        result.add(secretItem);
        
        return Result.success(result);
    }
}