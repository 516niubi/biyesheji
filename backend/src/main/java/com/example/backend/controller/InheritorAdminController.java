package com.example.backend.controller;

import com.example.backend.common.model.PageResult;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.Admin;
import com.example.backend.entity.request.inheritor.InheritorAdminUpdateRequest;
import com.example.backend.entity.request.inheritor.InheritorAuditRequest;
import com.example.backend.entity.vo.inheritor.InheritorVO;
import com.example.backend.service.IInheritorService;
import com.example.backend.utils.BackendAuthHelper;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 管理员审核传承人
 */
@RestController
@RequestMapping("/inheritor/admin")
public class InheritorAdminController {

    @Resource
    private IInheritorService inheritorService;

    @GetMapping("/page")
    @ApiOperation("分页查询传承人注册/审核列表")
    public BaseResponse<PageResult<List<InheritorVO>>> page(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String nickName
    ) {
        PageResult<List<InheritorVO>> res = inheritorService.pageForAudit(pageNum, pageSize, username, phone, status, nickName);
        return Result.success(res);
    }

    @PostMapping("/update")
    @ApiOperation("管理员编辑已认证传承人")
    public BaseResponse<Boolean> adminUpdate(@RequestBody InheritorAdminUpdateRequest body) {
        BackendAuthHelper.requireLoginAdmin();
        return Result.success(inheritorService.adminUpdate(body));
    }

    @GetMapping("/del")
    @ApiOperation("删除已认证传承人")
    public BaseResponse<Boolean> adminDel(@RequestParam Integer id) {
        BackendAuthHelper.requireLoginAdmin();
        return Result.success(inheritorService.adminDelete(id));
    }

    @PostMapping("/batchDel")
    @ApiOperation("批量删除已认证传承人")
    public BaseResponse<Boolean> adminBatchDel(@RequestBody List<Integer> ids) {
        BackendAuthHelper.requireLoginAdmin();
        return Result.success(inheritorService.adminBatchDelete(ids));
    }

    @PostMapping("/audit")
    @ApiOperation("审核传承人：status 1通过 2拒绝")
    public BaseResponse<Boolean> audit(@RequestBody InheritorAuditRequest body) {
        Admin admin = BackendAuthHelper.requireLoginAdmin();
        Boolean ok = inheritorService.audit(body.getId(), body.getStatus(), body.getRemark(), admin.getId());
        return Result.success(ok);
    }
}
