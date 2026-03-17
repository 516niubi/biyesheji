package com.example.backend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.constants.LoginConstant;
import com.example.backend.common.enums.CodeEnum;
import com.example.backend.common.enums.GenderEnum;
import com.example.backend.entity.Admin;
import com.example.backend.entity.Admin;
import com.example.backend.entity.User;
import com.example.backend.entity.request.system.LoginRequest;
import com.example.backend.entity.request.system.RegisterRequest;
import com.example.backend.entity.request.admin.AddAdminRequest;
import com.example.backend.entity.request.user.UpdatePassRequest;
import com.example.backend.entity.vo.admin.AdminLoginVO;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.AdminMapper;
import com.example.backend.mapper.AdminMapper;
import com.example.backend.service.IAdminService;
import com.example.backend.service.IAdminService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Administrator
 * @description 针对表【sys_admin】的数据库操作Service实现
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin>
        implements IAdminService {

    @Resource
    private AdminMapper adminMapper;

    /**
     * 管理员登录
     *
     * @param request 请求体
     * @return token
     */
    @Override
    public AdminLoginVO login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        // 根据账号查询管理员
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("role", request.getRole());
        Admin admin = adminMapper.selectOne(queryWrapper);
        // 判断管理员是否存在
        if (ObjectUtil.isNull(admin))
            throw new BusinessException(CodeEnum.NULL_ERROR, "账号错误");
        // 判断密码是否正确
        if (!password.equals(admin.getPassword()))
            throw new BusinessException(CodeEnum.NULL_ERROR, "密码错误");
        // 登录成功，返回token
        StpUtil.login(admin.getId());
        // 获取token
        String token = StpUtil.getTokenValue();
        // 将管理员信息存储到session中
        StpUtil.getSession().set(LoginConstant.USER_ID, admin.getId());
        StpUtil.getSession().set(LoginConstant.USER_KEY, admin);
        // 脱敏管理员信息
        AdminLoginVO adminLoginVO = new AdminLoginVO();
        BeanUtils.copyProperties(admin, adminLoginVO);
        adminLoginVO.setToken(token);
        return adminLoginVO;
    }

    /**
     * 获取当前登录管理员的信息
     *
     * @return 脱敏后管理员的信息
     */
    @Override
    public AdminLoginVO getCurrentAdminInfo() {
        // 从session中获取管理员id
        Integer adminId = (Integer) StpUtil.getSession().get(LoginConstant.USER_ID);
        // 根据id查询管理员信息
        Admin admin = adminMapper.selectById(adminId);
        // 管理员信息脱敏
        AdminLoginVO adminLoginVO = new AdminLoginVO();
        BeanUtils.copyProperties(admin, adminLoginVO);
        return adminLoginVO;
    }

    /**
     * 新增管理员
     *
     * @param adminRequest 请求参数
     * @return 管理员id
     */
    @Override
    public Boolean add(AddAdminRequest adminRequest) {
        // 校验参数
        String username = adminRequest.getUsername();
        // 账号不能重名
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        Admin admin = adminMapper.selectOne(queryWrapper);
        if (ObjectUtil.isNotNull(admin))
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "账号重复");

        // 性别不能随便设置
        GenderEnum gender = GenderEnum.getGender(adminRequest.getGender());
        if (gender == null)
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "性别参数错误");

        // 默认密码
        String defaultPassword = "123";
        if (adminRequest.getPassword() == null)
            adminRequest.setPassword(defaultPassword);

        // 数据库操作
        Admin saveAdmin = new Admin();
        saveAdmin.setRole("admin");
        BeanUtils.copyProperties(adminRequest, saveAdmin);
        return this.save(saveAdmin);
    }

    /**
     * 编辑管理员信息
     *
     * @param adminRequest 请求参数
     * @return 成功结果
     */
    @Override
    public Boolean edit(Admin adminRequest) {
        // 性别不能随便设置
        GenderEnum gender = GenderEnum.getGender(adminRequest.getGender());
        if (gender == null)
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "性别参数错误");

        // 数据库操作
        BeanUtils.copyProperties(adminRequest, adminRequest);
        return this.updateById(adminRequest);
    }

    /**
     * 批量删除管理员
     *
     * @param ids 管理员ID列表
     * @return 是否成功
     */
    @Override
    public Boolean batchDel(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR);
        }
        return removeByIds(ids);
    }

    @Override
    public Boolean updatePass(UpdatePassRequest request) {
        // 新密码
        String password = request.getPassword();
        // 旧密码
        String oldPass = request.getOldPass();
        // 管理员id
        Integer adminId = request.getUserId();
        // 根据id查询管理员信息
        Admin admin = adminMapper.selectById(adminId);
        // 校验旧密码是否正确
        if (!oldPass.equals(admin.getPassword()))
            throw new BusinessException(CodeEnum.NULL_ERROR, "旧密码错误");
        // 判断 新旧密码 是否一致
        if (password.equals(admin.getPassword()))
            throw new BusinessException(CodeEnum.NULL_ERROR, "与原密码相同");
        // 数据库操作
        admin.setPassword(password);
        return this.updateById(admin);
    }
}




