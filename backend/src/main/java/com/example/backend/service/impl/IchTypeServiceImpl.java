package com.example.backend.service.impl;

import com.example.backend.entity.IchType;
import com.example.backend.entity.vo.ichtype.IchTypeVO;
import com.example.backend.mapper.IchTypeMapper;
import com.example.backend.service.IIchTypeService;
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
 * 非遗分类表 服务实现类
 * </p>
 *
 * @author
 */
@Service
public class IchTypeServiceImpl extends ServiceImpl<IchTypeMapper, IchType> implements IIchTypeService {
    @Resource
    private IchTypeMapper ichTypeMapper;

    /**
     * 新增
     *
     * @param request
     * @return
     */
    @Override
    public Integer add(IchType request) {
        IchType saveData = new IchType();
        BeanUtils.copyProperties(request, saveData);
        ichTypeMapper.insert(saveData);
        return saveData.getId();
    }

    /**
     * 批量新增
     *
     * @param request
     * @return
     */
    @Override
    public Boolean batchAdd(List<IchType> request) {
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
    public Boolean edit(Integer id, IchType request) {
        IchType findData = ichTypeMapper.selectById(id);
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
    public PageResult<List<IchTypeVO>> queryPage(Integer pageNum, Integer pageSize, String name) {
        QueryWrapper<IchType> queryWrapper = new QueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(name)) {
            queryWrapper.like("name", name);
        }
        // ID 降序
        queryWrapper.orderByDesc("id");
        Page<IchType> page = page(new Page<>(pageNum, pageSize), queryWrapper);
        // 返回结果
        PageResult<List<IchTypeVO>> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setSize(page.getSize());
        pageResult.setCurrent(page.getCurrent());
        List<IchTypeVO> ichTypeVOS = convertVO(page.getRecords());
        pageResult.setRecords(ichTypeVOS);
        return pageResult;
    }

    /**
     * 查询全部
     *
     * @return
     */
    @Override
    public List<IchType> getAll() {
        return list();
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    @Override
    public IchType getByIdDetail(Integer id) {
        return getById(id);
    }

    // 转换VO
    private List<IchTypeVO> convertVO(List<IchType> ichTypes) {
        ArrayList<IchTypeVO> list = new ArrayList<>();
        for (IchType ichType : ichTypes) {
            IchTypeVO ichTypeVO = new IchTypeVO();
            BeanUtils.copyProperties(ichType, ichTypeVO);
            list.add(ichTypeVO);
        }
        return list;
    }
}