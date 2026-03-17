package com.example.backend.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.enums.CodeEnum;
import com.example.backend.common.model.PageResult;
import com.example.backend.entity.Collect;
import com.example.backend.entity.User;
import com.example.backend.entity.CulturalHeritage;
import com.example.backend.entity.vo.collect.CollectVO;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.CollectMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.CulturalHeritageMapper;
import com.example.backend.service.ICollectService;
import com.example.backend.utils.UserUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 收藏表 服务实现类
 * </p>
 *
 * @author
 */
@Service
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements ICollectService {
    @Resource
    private CollectMapper collectMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private CulturalHeritageMapper culturalHeritageMapper;

    /**
     * 新增
     *
     * @param request
     * @return
     */
    @Override
    public Integer add(Collect request) {
        Collect saveData = new Collect();
        BeanUtils.copyProperties(request, saveData);
        // 获取当前登录用户信息
        User userInfo = UserUtils.getUserInfo();
        if (userInfo != null) {
            saveData.setUserId(userInfo.getId());
        }
        collectMapper.insert(saveData);
        return saveData.getId();
    }

    /**
     * 批量新增
     *
     * @param request
     * @return
     */
    @Override
    public Boolean batchAdd(List<Collect> request) {
        // 获取当前登录用户信息
        User userInfo = UserUtils.getUserInfo();
        if (userInfo != null) {
            request.forEach(item -> {
                item.setUserId(userInfo.getId());
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
        return collectMapper.deleteById(id) > 0;
    }

    /**
     * 批量删除
     *
     * @param ids 批量ID列表
     * @return 是否成功
     */
    @Override
    public Boolean batchDel(List<Integer> ids) {
        return collectMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 编辑
     *
     * @param id
     * @param request
     * @return
     */
    @Override
    public Boolean edit(Integer id, Collect request) {
        Collect findData = collectMapper.selectById(id);
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
    public PageResult<List<CollectVO>> queryPage(Integer pageNum, Integer pageSize, String userName) {
        QueryWrapper<Collect> queryWrapper = new QueryWrapper<>();
        
        // 如果有用户名查询条件，先查询用户ID
        if (CharSequenceUtil.isNotBlank(userName)) {
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.like("nick_name", userName);
            List<User> users = userMapper.selectList(userQueryWrapper);
            if (users.isEmpty()) {
                // 如果没有找到用户，返回空结果
                PageResult<List<CollectVO>> pageResult = new PageResult<>();
                pageResult.setTotal(0L);
                pageResult.setSize((long) pageSize);
                pageResult.setCurrent((long) pageNum);
                pageResult.setRecords(new ArrayList<>());
                return pageResult;
            }
            List<Integer> userIds = new ArrayList<>();
            for (User user : users) {
                userIds.add(user.getId());
            }
            queryWrapper.in("user_id", userIds);
        }
        
        // ID 降序
        queryWrapper.orderByDesc("id");
        Page<Collect> page = page(new Page<>(pageNum, pageSize), queryWrapper);
        // 返回结果
        PageResult<List<CollectVO>> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setSize(page.getSize());
        pageResult.setCurrent(page.getCurrent());
        List<CollectVO> collectVOS = convertVO(page.getRecords());
        pageResult.setRecords(collectVOS);
        return pageResult;
    }

    /**
     * 查询全部
     *
     * @return
     */
    @Override
    public List<Collect> getAll() {
        return list();
    }


    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    @Override
    public Collect getByIdDetail(Integer id) {
        return getById(id);
    }
    
    @Override
    public Boolean isCollected(Integer userId, Integer heritageId) {
        QueryWrapper<Collect> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("heritage_id", heritageId);
        return count(queryWrapper) > 0;
    }
    
    @Override
    public PageResult<List<CollectVO>> queryPageByUserId(Integer pageNum, Integer pageSize, Integer userId) {
        QueryWrapper<Collect> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("id");
        
        Page<Collect> page = page(new Page<>(pageNum, pageSize), queryWrapper);
        
        // 返回结果
        PageResult<List<CollectVO>> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setSize(page.getSize());
        pageResult.setCurrent(page.getCurrent());
        List<CollectVO> collectVOS = convertVO(page.getRecords());
        pageResult.setRecords(collectVOS);
        return pageResult;
    }

    // 转换VO
    private List<CollectVO> convertVO(List<Collect> collects) {
        ArrayList<CollectVO> list = new ArrayList<>();
        for (Collect collect : collects) {
            CollectVO collectVO = new CollectVO();
            BeanUtils.copyProperties(collect, collectVO);
            // 获取用户信息
            if (collect.getUserId() != null) {
                User user = userMapper.selectById(collect.getUserId());
                if (user != null) {
                    collectVO.setUserName(user.getNickName());
                }
            }
            // 获取文物信息
            if (collect.getHeritageId() != null) {
                CulturalHeritage heritage = culturalHeritageMapper.selectById(collect.getHeritageId());
                if (heritage != null) {
                    collectVO.setHeritageName(heritage.getName());
                }
            }
            list.add(collectVO);
        }
        return list;
    }
}