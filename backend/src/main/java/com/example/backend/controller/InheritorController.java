package com.example.backend.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.example.backend.common.model.PageResult;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.Inheritor;
import com.example.backend.entity.request.inheritor.InheritorRegisterRequest;
import com.example.backend.entity.request.system.LoginRequest;
import com.example.backend.entity.request.user.UpdatePassRequest;
import com.example.backend.entity.vo.inheritor.InheritorLoginVO;
import com.example.backend.entity.vo.inheritor.InheritorPublicProfileVO;
import com.example.backend.service.IInheritorService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/inheritor")
public class InheritorController {

    @Resource
    private IInheritorService inheritorService;

    @SaIgnore
    @ApiOperation("传承人注册（提交后待管理员审核）")
    @PostMapping("/register")
    public BaseResponse<Boolean> register(@RequestBody InheritorRegisterRequest request) {
        inheritorService.register(request);
        return Result.success(true);
    }

    @SaIgnore
    @ApiOperation("传承人登录（仅审核通过可登录）")
    @PostMapping("/login")
    public BaseResponse<InheritorLoginVO> login(@RequestBody LoginRequest request) {
        return Result.success(inheritorService.login(request));
    }

    @SaIgnore
    @ApiOperation("前台：传承人公开主页资料")
    @GetMapping("/public/profile/{id}")
    public BaseResponse<InheritorPublicProfileVO> publicProfile(@PathVariable("id") Integer id) {
        return Result.success(inheritorService.getPublicProfile(id));
    }

    @SaIgnore
    @ApiOperation("前台：传承人名录分页")
    @GetMapping("/public/page")
    public BaseResponse<PageResult<List<InheritorPublicProfileVO>>> publicPage(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String keyword
    ) {
        return Result.success(inheritorService.publicPage(pageNum, pageSize, keyword));
    }

    @ApiOperation("当前传承人信息")
    @GetMapping("/current")
    public BaseResponse<InheritorLoginVO> current() {
        return Result.success(inheritorService.getCurrentInfo());
    }

    @ApiOperation("更新资料（不含用户名、手机号、证书）")
    @PostMapping("/profile")
    public BaseResponse<Boolean> profile(@RequestBody Inheritor body) {
        return Result.success(inheritorService.updateProfile(body));
    }

    @ApiOperation("修改密码")
    @PostMapping("/updatePass")
    public BaseResponse<Boolean> updatePass(@RequestBody UpdatePassRequest request) {
        Boolean ok = inheritorService.updatePass(request.getUserId(), request.getOldPass(), request.getPassword());
        return Result.success(ok);
    }
}
