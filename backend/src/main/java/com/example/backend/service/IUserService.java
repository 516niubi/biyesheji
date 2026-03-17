package com.example.backend.service;

import com.example.backend.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.request.system.LoginRequest;
import com.example.backend.entity.request.system.RegisterRequest;
import com.example.backend.entity.request.user.AddUserRequest;
import com.example.backend.entity.request.user.UpdatePassRequest;
import com.example.backend.entity.vo.user.UserLoginVO;

import java.util.List;

/**
* @author Administrator
* @description 针对表【sys_user】的数据库操作Service
* @createDate 2024-08-09 11:58:10
*/
public interface IUserService extends IService<User> {

    /**
     * 用户注册
     * @param request 请求体
     * @return 受影响行数
     */
    Integer register(RegisterRequest request);

    /**
     * 用户登录
     * @param request 请求体
     * @return token
     */
    UserLoginVO login(LoginRequest request);

    /**
     * 获取当前登录用户的信息
     * @return 脱敏后用户的信息
     */
    UserLoginVO getCurrentUserInfo();

    /**
     * 新增用户
     * @param userRequest 请求参数
     * @return 用户id
     */
    Boolean add(AddUserRequest userRequest);

    /**
     * 编辑用户信息
     * @param userRequest 请求参数
     * @return 成功结果
     */
    Boolean edit(User userRequest);

    /**
     * 批量删除用户
     * @param ids 用户ID列表
     * @return 是否成功
     */
    Boolean batchDel(List<Integer> ids);

    /**
     * 修改密码
     * @param request 请求参数
     * @return 成功结果
     */
    Boolean updatePass(UpdatePassRequest request);

    /**
     * 获取登录用户的信息
     * @return 用户信息
     */
    User getLoginUserInfo();
}
