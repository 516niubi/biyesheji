package com.example.backend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.example.backend.common.constants.LoginConstant;
import com.example.backend.entity.Feedback;
import com.example.backend.entity.User;
import com.example.backend.entity.vo.feedback.FeedbackVO;
import com.example.backend.mapper.FeedbackMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.IFeedbackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.utils.UserUtils;
import org.springframework.stereotype.Service;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.model.PageResult;
import com.example.backend.exception.BusinessException;
import org.springframework.beans.BeanUtils;

import javax.annotation.Resource;

import com.example.backend.common.enums.CodeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 反馈表 服务实现类
 * </p>
 *
 * @author
 */
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements IFeedbackService {
    @Resource
    private FeedbackMapper feedbackMapper;
    @Resource
    private UserMapper userMapper;

    /**
     * 新增
     *
     * @param request
     * @return
     */
    @Override
    public Integer add(Feedback request) {
        Feedback saveData = new Feedback();
        BeanUtils.copyProperties(request, saveData);
        // 获取当前登录用户信息
        Integer userId = (Integer) StpUtil.getSession().get(LoginConstant.USER_ID);
        User findUser = userMapper.selectById(userId);
        if (findUser != null) {
            saveData.setUserId(findUser.getId());
        }
        // 设置默认状态为未回复
        saveData.setStatus(0);
        feedbackMapper.insert(saveData);
        return saveData.getId();
    }

    /**
     * 批量新增
     *
     * @param request
     * @return
     */
    @Override
    public Boolean batchAdd(List<Feedback> request) {
        // 获取当前登录用户信息
        User userInfo = UserUtils.getUserInfo();
        if (userInfo != null) {
            request.forEach(item -> {
                item.setUserId(userInfo.getId());
                item.setStatus(0); // 设置默认状态为未回复
            });
        }
        return saveBatch(request);
    }

    /**
     * 删除
     *
     * @param id
     * @return
     */
    @Override
    public Boolean del(Integer id) {
        return removeById(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @Override
    public Boolean batchDel(List<Integer> ids) {
        return removeByIds(ids);
    }

    /**
     * 编辑
     *
     * @param id
     * @param request
     * @return
     */
    @Override
    public Boolean edit(Integer id, Feedback request) {
        Feedback findData = feedbackMapper.selectById(id);
        if (findData == null) {
            throw new BusinessException(CodeEnum.SYSTEM_ERROR);
        }
        BeanUtils.copyProperties(request, findData);
        return updateById(findData);
    }

    /**
     * 分页查询
     *
     * @return
     */
    @Override
    public PageResult<List<FeedbackVO>> queryPage(Integer pageNum, Integer pageSize, String title, Integer status) {
        QueryWrapper<Feedback> queryWrapper = new QueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(title)) {
            queryWrapper.like("title", title);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        // ID 降序
        queryWrapper.orderByDesc("id");
        Page<Feedback> page = page(new Page<>(pageNum, pageSize), queryWrapper);
        // 返回结果
        PageResult<List<FeedbackVO>> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setSize(page.getSize());
        pageResult.setCurrent(page.getCurrent());
        List<FeedbackVO> feedbackVOS = convertVO(page.getRecords());
        pageResult.setRecords(feedbackVOS);
        return pageResult;
    }

    /**
     * 查询全部
     *
     * @return
     */
    @Override
    public List<Feedback> getAll() {
        return list();
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    @Override
    public Feedback getByIdDetail(Integer id) {
        return getById(id);
    }

    @Override
    public PageResult<List<FeedbackVO>> pageByUserId(Integer pageNum, Integer pageSize, String title, Integer status, Integer userId) {
        Integer currentUserId = (Integer) StpUtil.getSession().get(LoginConstant.USER_ID);
        if (currentUserId == null) {
            throw new BusinessException(CodeEnum.NOT_LOGIN);
        }

        QueryWrapper<Feedback> queryWrapper = new QueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(title)) {
            queryWrapper.like("title", title);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        // 前台查询强制只返回当前登录用户的反馈
        queryWrapper.eq("user_id", currentUserId);
        // ID 降序
        queryWrapper.orderByDesc("id");
        Page<Feedback> page = page(new Page<>(pageNum, pageSize), queryWrapper);
        // 返回结果
        PageResult<List<FeedbackVO>> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setSize(page.getSize());
        pageResult.setCurrent(page.getCurrent());
        List<FeedbackVO> feedbackVOS = convertVO(page.getRecords());
        pageResult.setRecords(feedbackVOS);
        return pageResult;
    }

    // 转换VO
    private List<FeedbackVO> convertVO(List<Feedback> feedbacks) {
        ArrayList<FeedbackVO> list = new ArrayList<>();
        for (Feedback feedback : feedbacks) {
            FeedbackVO feedbackVO = new FeedbackVO();
            BeanUtils.copyProperties(feedback, feedbackVO);
            // 获取用户信息
            if (feedback.getUserId() != null) {
                User user = userMapper.selectById(feedback.getUserId());
                if (user != null) {
                    feedbackVO.setUserName(user.getNickName());
                }
            }
            list.add(feedbackVO);
        }
        return list;
    }
}