package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.model.PageResult;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.User;
import com.example.backend.entity.request.user.AddUserRequest;
import com.example.backend.entity.request.user.UpdatePassRequest;
import com.example.backend.entity.vo.user.UserLoginVO;
import com.example.backend.entity.vo.user.UserVO;
import com.example.backend.service.IUserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author：caiya
 * @Description 用户控制层
 * @Version 1.0
 */
@RequestMapping("/user")
@RestController
public class UserController {
    @Resource
    private IUserService userService;

    @ApiOperation(value = "查询所有用户接口")
    @GetMapping("/list")
    private BaseResponse<List<User>> list() {
        List<User> list = userService.list();
        return Result.success(list);
    }

    @ApiOperation(value = "分页条件查询所有用户接口")
    @GetMapping("/page")
    private BaseResponse<PageResult<List<UserVO>>> page(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String nickName) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("username", username);
        queryWrapper.like("nick_name", nickName);
        queryWrapper.orderByDesc("id");
        Page<User> userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);

        PageResult<List<UserVO>> pageResult = new PageResult<>();
        pageResult.setCurrent(pageNum);
        pageResult.setTotal(userPage.getTotal());
        List<UserVO> list = userPage.getRecords().stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        pageResult.setRecords(list);
        return Result.success(pageResult);
    }


    @ApiOperation("获取当前登录用户信息")
    @GetMapping("/current")
    private BaseResponse<UserLoginVO> getCurrentUserInfo() {
        UserLoginVO res = userService.getCurrentUserInfo();
        return Result.success(res);
    }

    @ApiOperation("新增用户")
    @PostMapping("/add")
    private BaseResponse<Boolean> add(@RequestBody AddUserRequest userRequest) {
        Boolean res = userService.add(userRequest);
        return Result.success(res);
    }

    @ApiOperation("删除用户")
    @GetMapping("/del")
    private BaseResponse<Boolean> del(@RequestParam("id") Integer id) {
        boolean res = userService.removeById(id);
        return Result.success(res);
    }
    
    @ApiOperation("批量删除用户")
    @PostMapping("/batchDel")
    private BaseResponse<Boolean> batchDel(@RequestBody List<Integer> ids) {
        Boolean res = userService.batchDel(ids);
        return Result.success(res);
    }

    @ApiOperation("编辑用户")
    @PostMapping("/edit")
    private BaseResponse<Boolean> edit(@RequestBody User userRequest) {
        Boolean res = userService.edit(userRequest);
        return Result.success(res);
    }

    @ApiOperation("修改密码")
    @PostMapping("/updatePass")
    private BaseResponse<Boolean> updatePass(@RequestBody UpdatePassRequest request) {
        Boolean res = userService.updatePass(request);
        return Result.success(res);
    }
}
