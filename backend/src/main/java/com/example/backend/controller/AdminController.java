package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.model.PageResult;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.Admin;
import com.example.backend.entity.request.admin.AddAdminRequest;
import com.example.backend.entity.request.user.UpdatePassRequest;
import com.example.backend.entity.vo.admin.AdminLoginVO;
import com.example.backend.entity.vo.admin.AdminVO;
import com.example.backend.service.IAdminService;
import com.example.backend.utils.BackendAuthHelper;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/admin")
@RestController
public class AdminController {
    @Resource
    private IAdminService adminService;

    @ApiOperation(value = "查询所有管理员接口")
    @GetMapping("/list")
    private BaseResponse<List<Admin>> list() {
        BackendAuthHelper.requireLoginAdmin();
        List<Admin> list = adminService.list();
        return Result.success(list);
    }

    @ApiOperation(value = "分页条件查询所有管理员接口")
    @GetMapping("/page")
    private BaseResponse<PageResult<List<AdminVO>>> page(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String nickName,
            @RequestParam(required = false) String role) {
        BackendAuthHelper.requireLoginAdmin();
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("username", username);
        queryWrapper.like("nick_name", nickName);
        if (role != null && !role.trim().isEmpty()) {
            queryWrapper.eq("role", role);
        }
        queryWrapper.orderByDesc("id");
        Page<Admin> adminPage = adminService.page(new Page<>(pageNum, pageSize), queryWrapper);

        PageResult<List<AdminVO>> pageResult = new PageResult<>();
        pageResult.setCurrent(pageNum);
        pageResult.setTotal(adminPage.getTotal());
        List<AdminVO> list = adminPage.getRecords().stream().map(admin -> {
            AdminVO adminVO = new AdminVO();
            BeanUtils.copyProperties(admin, adminVO);
            return adminVO;
        }).collect(Collectors.toList());
        pageResult.setRecords(list);
        return Result.success(pageResult);
    }

    @ApiOperation("获取当前登录管理员信息")
    @GetMapping("/current")
    private BaseResponse<AdminLoginVO> getCurrentAdminInfo() {
        AdminLoginVO res = adminService.getCurrentAdminInfo();
        return Result.success(res);
    }

    @ApiOperation("新增管理员")
    @PostMapping("/add")
    private BaseResponse<Boolean> add(@RequestBody AddAdminRequest adminRequest) {
        Boolean res = adminService.add(adminRequest);
        return Result.success(res);
    }

    @ApiOperation("删除管理员")
    @GetMapping("/del")
    private BaseResponse<Boolean> del(@RequestParam("id") Integer id) {
        boolean res = adminService.removeById(id);
        return Result.success(res);
    }

    @ApiOperation("批量删除管理员")
    @PostMapping("/batchDel")
    private BaseResponse<Boolean> batchDel(@RequestBody List<Integer> ids) {
        Boolean res = adminService.batchDel(ids);
        return Result.success(res);
    }

    @ApiOperation("编辑管理员")
    @PostMapping("/edit")
    private BaseResponse<Boolean> edit(@RequestBody Admin adminRequest) {
        Boolean res = adminService.edit(adminRequest);
        return Result.success(res);
    }

    @ApiOperation("修改密码")
    @PostMapping("/updatePass")
    private BaseResponse<Boolean> updatePass(@RequestBody UpdatePassRequest request) {
        Boolean res = adminService.updatePass(request);
        return Result.success(res);
    }
}
