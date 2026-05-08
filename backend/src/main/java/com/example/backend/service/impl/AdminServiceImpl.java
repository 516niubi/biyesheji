package com.example.backend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.constants.LoginConstant;
import com.example.backend.common.enums.CodeEnum;
import com.example.backend.common.enums.GenderEnum;
import com.example.backend.entity.Admin;
import com.example.backend.entity.request.system.LoginRequest;
import com.example.backend.entity.request.admin.AddAdminRequest;
import com.example.backend.entity.request.user.UpdatePassRequest;
import com.example.backend.entity.vo.admin.AdminLoginVO;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.AdminMapper;
import com.example.backend.service.IAdminService;
import com.example.backend.utils.BackendAuthHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin>
        implements IAdminService {

    @Resource
    private AdminMapper adminMapper;

    @Override
    public AdminLoginVO login(LoginRequest request) {
        if (!"admin".equals(request.getRole())) {
            throw new BusinessException(CodeEnum.AUTH_ERROR, "请使用「管理员」登录；传承人请使用传承人登录入口");
        }
        String username = request.getUsername();
        String password = request.getPassword();
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("role", "admin");
        Admin admin = adminMapper.selectOne(queryWrapper);
        if (ObjectUtil.isNull(admin)) {
            throw new BusinessException(CodeEnum.NULL_ERROR, "账号错误");
        }
        if (!password.equals(admin.getPassword())) {
            throw new BusinessException(CodeEnum.NULL_ERROR, "密码错误");
        }
        StpUtil.login(admin.getId());
        String token = StpUtil.getTokenValue();
        StpUtil.getSession().set(LoginConstant.USER_ID, admin.getId());
        StpUtil.getSession().set(LoginConstant.USER_KEY, admin);
        AdminLoginVO adminLoginVO = new AdminLoginVO();
        BeanUtils.copyProperties(admin, adminLoginVO);
        adminLoginVO.setToken(token);
        return adminLoginVO;
    }

    @Override
    public AdminLoginVO getCurrentAdminInfo() {
        BackendAuthHelper.requireLoginAdmin();
        Integer adminId = (Integer) StpUtil.getSession().get(LoginConstant.USER_ID);
        Admin admin = adminMapper.selectById(adminId);
        AdminLoginVO adminLoginVO = new AdminLoginVO();
        BeanUtils.copyProperties(admin, adminLoginVO);
        return adminLoginVO;
    }

    @Override
    public Boolean add(AddAdminRequest adminRequest) {
        String username = adminRequest.getUsername();
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        Admin admin = adminMapper.selectOne(queryWrapper);
        if (ObjectUtil.isNotNull(admin)) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "账号重复");
        }
        GenderEnum gender = GenderEnum.getGender(adminRequest.getGender());
        if (gender == null) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "性别参数错误");
        }
        String defaultPassword = "123";
        if (adminRequest.getPassword() == null) {
            adminRequest.setPassword(defaultPassword);
        }
        Admin saveAdmin = new Admin();
        BeanUtils.copyProperties(adminRequest, saveAdmin);
        saveAdmin.setRole("admin");
        return this.save(saveAdmin);
    }

    @Override
    public Boolean edit(Admin adminRequest) {
        GenderEnum gender = GenderEnum.getGender(adminRequest.getGender());
        if (gender == null) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "性别参数错误");
        }
        if (adminRequest.getRole() != null
                && !"admin".equals(adminRequest.getRole())) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "角色参数错误");
        }
        return this.updateById(adminRequest);
    }

    @Override
    public Boolean batchDel(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR);
        }
        return removeByIds(ids);
    }

    @Override
    public Boolean updatePass(UpdatePassRequest request) {
        Admin session = BackendAuthHelper.requireLoginAdmin();
        String password = request.getPassword();
        String oldPass = request.getOldPass();
        Integer adminId = request.getUserId();
        if (!session.getId().equals(adminId)) {
            throw new BusinessException(CodeEnum.AUTH_ERROR, "仅能修改本人密码");
        }
        Admin admin = adminMapper.selectById(adminId);
        if (!oldPass.equals(admin.getPassword())) {
            throw new BusinessException(CodeEnum.NULL_ERROR, "旧密码错误");
        }
        if (password.equals(admin.getPassword())) {
            throw new BusinessException(CodeEnum.NULL_ERROR, "与原密码相同");
        }
        admin.setPassword(password);
        return this.updateById(admin);
    }
}
