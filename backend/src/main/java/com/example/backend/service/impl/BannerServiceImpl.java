package com.example.backend.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.enums.CodeEnum;
import com.example.backend.common.model.PageResult;
import com.example.backend.entity.Banner;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.BannerMapper;
import com.example.backend.service.IBannerService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 轮播图表 服务实现类
 * </p>
 *
 * @author
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements IBannerService {
    @Resource
    private BannerMapper bannerMapper;

    /**
     * 新增
     *
     * @param request
     * @return
     */
    @Override
    public Integer add(Banner request) {
        Banner saveData = new Banner();
        BeanUtils.copyProperties(request, saveData);
        bannerMapper.insert(saveData);
        return saveData.getId();
    }

    /**
     * 批量新增
     *
     * @param request
     * @return
     */
    @Override
    public Boolean batchAdd(List<Banner> request) {
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
    public Boolean edit(Integer id, Banner request) {
        if (id == null || id <= 0) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR);
        }
        Banner updateData = new Banner();
        BeanUtils.copyProperties(request, updateData);
        updateData.setId(id);
        return updateById(updateData);
    }

    /**
     * 分页查询
     *
     * @param pageNum
     * @param pageSize
     * @param title
     * @return
     */
    @Override
    public PageResult page(Integer pageNum, Integer pageSize, String title) {
        QueryWrapper<Banner> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        if (CharSequenceUtil.isNotBlank(title)) {
            queryWrapper.like("title", title);
        }
        Page<Banner> page = bannerMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        PageResult<List<Banner>> pageResult = new PageResult<>();
        pageResult.setRecords(page.getRecords());
        pageResult.setCurrent(page.getCurrent());
        pageResult.setTotal(page.getTotal());
        pageResult.setSize(page.getSize());
        return pageResult;
    }

    /**
     * 查询所有轮播图
     *
     * @return
     */
    @Override
    public List<Banner> list() {
        QueryWrapper<Banner> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return bannerMapper.selectList(queryWrapper);
    }
}