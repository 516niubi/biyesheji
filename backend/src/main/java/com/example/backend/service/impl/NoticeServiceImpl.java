package com.example.backend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.example.backend.entity.Notice;
import com.example.backend.entity.Admin;
import com.example.backend.entity.User;
import com.example.backend.common.constants.LoginConstant;
import com.example.backend.entity.vo.notice.NoticeVO;
import com.example.backend.mapper.AdminMapper;
import com.example.backend.mapper.NoticeMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.INoticeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
 * 公告表 服务实现类
 * </p>
 *
 * @author
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements INoticeService {
    @Resource
    private NoticeMapper noticeMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private AdminMapper adminMapper;

    /**
     * 新增
     *
     * @param request
     * @return
     */
    @Override
    public Integer add(Notice request) {
        Notice saveData = new Notice();
        BeanUtils.copyProperties(request, saveData);
        // 兼容管理员和用户登录，统一从会话中获取登录ID
        Integer loginUserId = (Integer) StpUtil.getSession().get(LoginConstant.USER_ID);
        if (loginUserId != null) {
            saveData.setUserId(loginUserId);
        }
        noticeMapper.insert(saveData);
        return saveData.getId();
    }

    /**
     * 批量新增
     *
     * @param request
     * @return
     */
    @Override
    public Boolean batchAdd(List<Notice> request) {
        Integer loginUserId = (Integer) StpUtil.getSession().get(LoginConstant.USER_ID);
        if (loginUserId != null) {
            request.forEach(item -> {
                item.setUserId(loginUserId);
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
        if (id == null || id <= 0) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR);
        }
        return removeById(id);
    }

    /**
     * 批量删除
     *
     * @param ids 批量ID列表
     * @return 是否成功
     */
    @Override
    public Boolean batchDel(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR);
        }
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
    public Boolean edit(Integer id, Notice request) {
        Notice findData = noticeMapper.selectById(id);
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
    public PageResult<List<NoticeVO>> queryPage(Integer pageNum, Integer pageSize, String title) {
        QueryWrapper<Notice> queryWrapper = new QueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(title)) {
            queryWrapper.like("title", title);
        }
        // 按更新时间和ID倒序，保证展示最新公告
        queryWrapper.orderByDesc("update_time", "id");
        Page<Notice> page = page(new Page<>(pageNum, pageSize), queryWrapper);
        // 返回结果
        PageResult<List<NoticeVO>> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setSize(page.getSize());
        pageResult.setCurrent(page.getCurrent());
        List<NoticeVO> noticeVOS = convertVO(page.getRecords());
        pageResult.setRecords(noticeVOS);
        return pageResult;
    }

    /**
     * 查询全部
     *
     * @return
     */
    @Override
    public List<Notice> getAll() {
        return list();
    }


    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    @Override
    public Notice getByIdDetail(Integer id) {
        return getById(id);
    }


    // 转换VO
    private List<NoticeVO> convertVO(List<Notice> notices) {
        ArrayList<NoticeVO> list = new ArrayList<>();
        for (Notice notice : notices) {
            NoticeVO noticeVO = new NoticeVO();
            BeanUtils.copyProperties(notice, noticeVO);
            // 获取发布人信息
            if (notice.getUserId() != null) {
                Admin admin = adminMapper.selectById(notice.getUserId());
                if (admin != null) {
                    noticeVO.setPublishName(
                            CharSequenceUtil.isNotBlank(admin.getNickName()) ? admin.getNickName() : admin.getUsername()
                    );
                } else {
                    User user = userMapper.selectById(notice.getUserId());
                    if (user != null) {
                        noticeVO.setPublishName(
                                CharSequenceUtil.isNotBlank(user.getNickName()) ? user.getNickName() : user.getUsername()
                        );
                    }
                }
            }
            if (CharSequenceUtil.isBlank(noticeVO.getPublishName())) {
                noticeVO.setPublishName("系统");
            }
            list.add(noticeVO);
        }
        return list;
    }
}
