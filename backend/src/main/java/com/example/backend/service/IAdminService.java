package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.Admin;
import com.example.backend.entity.User;
import com.example.backend.entity.request.admin.AddAdminRequest;
import com.example.backend.entity.request.system.LoginRequest;
import com.example.backend.entity.request.system.RegisterRequest;
import com.example.backend.entity.request.user.UpdatePassRequest;
import com.example.backend.entity.vo.admin.AdminLoginVO;

import java.util.List;

/**
* @author Administrator
* @description 针对表【sys_admin】的数据库操作Service
*/
public interface IAdminService extends IService<Admin> {
    /**
     * 管理员登录
     * @param request 请求体
     * @return token
     */
    AdminLoginVO login(LoginRequest request);

    /**
     * 获取当前登录管理员的信息
     * @return 脱敏后管理员的信息
     */
    AdminLoginVO getCurrentAdminInfo();

    /**
     * 新增管理员
     * @param adminRequest 请求参数
     * @return 管理员id
     */
    Boolean add(AddAdminRequest adminRequest);

    /**
     * 编辑管理员信息
     * @param adminRequest 请求参数
     * @return 成功结果
     */
    Boolean edit(Admin adminRequest);

    /**
     * 批量删除管理员
     * @param ids 管理员ID列表
     * @return 是否成功
     */
    Boolean batchDel(List<Integer> ids);

    /**
     * 修改密码
     * @param request 请求参数
     * @return 成功结果
     */
    Boolean updatePass(UpdatePassRequest request);
}
