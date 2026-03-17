package com.example.backend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.constants.LoginConstant;
import com.example.backend.common.enums.CodeEnum;
import com.example.backend.common.enums.GenderEnum;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.UserMapper;
import com.example.backend.entity.User;
import com.example.backend.entity.request.system.LoginRequest;
import com.example.backend.entity.request.system.RegisterRequest;
import com.example.backend.entity.request.user.AddUserRequest;
import com.example.backend.entity.request.user.UpdatePassRequest;
import com.example.backend.entity.vo.user.UserLoginVO;
import com.example.backend.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Administrator
 * @description 针对表【sys_user】的数据库操作Service实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements IUserService {

    static final String DEFAULT_PASSWORD = "123";

    @Resource
    private UserMapper userMapper;

    /**
     * 用户注册
     *
     * @param request 请求体
     * @return 受影响行数
     */
    @Override
    public Integer register(RegisterRequest request) {
        String username = request.getUsername();
        // 账号不能重名
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        if (ObjectUtil.isNotNull(user))
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "账号重复");
        // 数据库操作
        User saveUser = new User();
        saveUser.setRole("user");
        saveUser.setNickName(request.getUsername());
        BeanUtils.copyProperties(request, saveUser);
        return userMapper.insert(saveUser);
    }

    /**
     * 用户登录
     *
     * @param request 请求体
     * @return token
     */
    @Override
    public UserLoginVO login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        // 根据账号查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("role", request.getRole());
        User user = userMapper.selectOne(queryWrapper);
        // 判断用户是否存在
        if (ObjectUtil.isNull(user))
            throw new BusinessException(CodeEnum.NULL_ERROR, "账号错误");
        // 判断密码是否正确
        if (!password.equals(user.getPassword()))
            throw new BusinessException(CodeEnum.NULL_ERROR, "密码错误");
        // 登录成功，返回token
        StpUtil.login(user.getId());
        // 获取token
        String token = StpUtil.getTokenValue();
        // 将用户信息存储到session中
        StpUtil.getSession().set(LoginConstant.USER_ID, user.getId());
        StpUtil.getSession().set(LoginConstant.USER_KEY, user);
        // 脱敏用户信息
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);
        userLoginVO.setToken(token);
        return userLoginVO;
    }

    /**
     * 获取当前登录用户的信息
     *
     * @return 脱敏后用户的信息
     */
    @Override
    public UserLoginVO getCurrentUserInfo() {
        // 从session中获取用户id
        Integer userId = (Integer) StpUtil.getSession().get(LoginConstant.USER_ID);
        // 根据id查询用户信息
        User user = userMapper.selectById(userId);
        // 用户信息脱敏
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);
        return userLoginVO;
    }

    /**
     * 新增用户
     *
     * @param userRequest 请求参数
     * @return 用户id
     */
    @Override
    public Boolean add(AddUserRequest userRequest) {
        // 校验参数
        String username = userRequest.getUsername();
        // 账号不能重名
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        if (ObjectUtil.isNotNull(user))
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "账号重复");

        // 性别不能随便设置
        GenderEnum gender = GenderEnum.getGender(userRequest.getGender());
        if (gender == null)
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "性别参数错误");

        // 默认密码
        String defaultPassword = "123";
        if (userRequest.getPassword() == null)
            userRequest.setPassword(defaultPassword);

        // 数据库操作
        User saveUser = new User();
        saveUser.setRole("user");
        BeanUtils.copyProperties(userRequest, saveUser);
        return this.save(saveUser);
    }

    /**
     * 编辑用户信息
     *
     * @param user 请求参数
     * @return 成功结果
     */
    @Override
    public Boolean edit(User user) {
        // 性别不能随便设置
        GenderEnum gender = GenderEnum.getGender(user.getGender());
        if (gender == null)
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "性别参数错误");

        // 数据库操作
        return this.updateById(user);
    }

    /**
     * 批量删除用户
     *
     * @param ids 用户ID列表
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
        // 用户id
        Integer userId = request.getUserId();
        // 根据id查询用户信息
        User user = userMapper.selectById(userId);
        // 校验旧密码是否正确
        if (!oldPass.equals(user.getPassword()))
            throw new BusinessException(CodeEnum.NULL_ERROR, "旧密码错误");
        // 判断 新旧密码 是否一致
        if (password.equals(user.getPassword()))
            throw new BusinessException(CodeEnum.NULL_ERROR, "与原密码相同");
        // 数据库操作
        user.setPassword(password);
        return this.updateById(user);
    }

    @Override
    public User getLoginUserInfo() {
        // 获取用户id
        Integer userId = (Integer) StpUtil.getSession().get(LoginConstant.USER_ID);
        // 根据id查询用户信息
        if (userId == null)
            throw new BusinessException(CodeEnum.NULL_ERROR, "用户不存在");
        // 从session中获取用户信息
        User user = (User) StpUtil.getSession().get(LoginConstant.USER_KEY);
        if (user == null)
            throw new BusinessException(CodeEnum.NULL_ERROR, "用户不存在");
        return user;
    }
}




