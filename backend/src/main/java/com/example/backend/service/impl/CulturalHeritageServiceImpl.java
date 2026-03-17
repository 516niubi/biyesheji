package com.example.backend.service.impl;

import com.example.backend.entity.CulturalHeritage;
import com.example.backend.entity.IchType;
import com.example.backend.entity.vo.culturalheritage.CulturalHeritageVO;
import com.example.backend.entity.vo.notice.NoticeVO;
import com.example.backend.mapper.CulturalHeritageMapper;
import com.example.backend.mapper.IchTypeMapper;
import com.example.backend.service.ICulturalHeritageService;
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
 * 非遗文物表 服务实现类
 * </p>
 *
 * @author 
 * @since 2025-01-29
 */
@Service
public class CulturalHeritageServiceImpl extends ServiceImpl<CulturalHeritageMapper, CulturalHeritage> implements ICulturalHeritageService {

    @Resource
    private CulturalHeritageMapper culturalHeritageMapper;
    
    @Resource
    private IchTypeMapper ichTypeMapper;

    /**
     * 新增
     *
     * @param request
     * @return
     */
    @Override
    public Integer add(CulturalHeritage request) {
        culturalHeritageMapper.insert(request);
        return request.getId();
    }

    /**
     * 批量新增
     *
     * @param request
     * @return
     */
    @Override
    public Boolean batchAdd(List<CulturalHeritage> request) {
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
        return culturalHeritageMapper.deleteById(id) > 0;
    }

    /**
     * 批量删除
     *
     * @param ids 批量ID列表
     * @return 是否成功
     */
    @Override
    public Boolean batchDel(List<Integer> ids) {
        return culturalHeritageMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 编辑
     *
     * @param id      id
     * @param request 请求参数
     * @return
     */
    @Override
    public Boolean edit(Integer id, CulturalHeritage request) {
        CulturalHeritage culturalHeritage = culturalHeritageMapper.selectById(id);
        if (culturalHeritage == null) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "数据不存在");
        }
        request.setId(id);
        return culturalHeritageMapper.updateById(request) > 0;
    }

    /**
     * 分页条件查询
     *
     * @return
     */
    @Override
    public PageResult<List<CulturalHeritageVO>> queryPage(Integer pageNum, Integer pageSize, String name) {
        QueryWrapper<CulturalHeritage> queryWrapper = new QueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(name)) {
            queryWrapper.like("name", name);
        }
        queryWrapper.orderByDesc("create_time");
        Page<CulturalHeritage> page = new Page<>(pageNum, pageSize);
        Page<CulturalHeritage> culturalHeritagePage = culturalHeritageMapper.selectPage(page, queryWrapper);
        List<CulturalHeritageVO> culturalHeritageVOList = new ArrayList<>();
        for (CulturalHeritage culturalHeritage : culturalHeritagePage.getRecords()) {
            CulturalHeritageVO culturalHeritageVO = convertVO(culturalHeritage);
            culturalHeritageVOList.add(culturalHeritageVO);
        }
        // 返回结果
        PageResult<List<CulturalHeritageVO>> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setSize(page.getSize());
        pageResult.setCurrent(page.getCurrent());
        pageResult.setRecords(culturalHeritageVOList);
        return pageResult;
    }

    /**
     * 查询全部
     *
     * @return
     */
    @Override
    public List<CulturalHeritage> getAll() {
        QueryWrapper<CulturalHeritage> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return culturalHeritageMapper.selectList(queryWrapper);
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    @Override
    public CulturalHeritage getByIdDetail(Integer id) {
        // 增加浏览量
        CulturalHeritage culturalHeritage = culturalHeritageMapper.selectById(id);
        if (culturalHeritage != null) {
            culturalHeritage.setViewCount(culturalHeritage.getViewCount() + 1);
            culturalHeritageMapper.updateById(culturalHeritage);
        }
        return culturalHeritageMapper.selectById(id);
    }

    /**
     * 获取热门非遗文物分页数据
     *
     * @param pageNum   页码
     * @param pageSize  页大小
     * @param orderBy   排序字段
     * @param orderType 排序类型
     * @return
     */
    @Override
    public PageResult<List<CulturalHeritageVO>> getHotPage(Integer pageNum, Integer pageSize, String orderBy, String orderType) {
        QueryWrapper<CulturalHeritage> queryWrapper = new QueryWrapper<>();
        
        // 根据排序类型设置排序
        if ("desc".equalsIgnoreCase(orderType)) {
            queryWrapper.orderByDesc(orderBy);
        } else {
            queryWrapper.orderByAsc(orderBy);
        }
        
        Page<CulturalHeritage> page = new Page<>(pageNum, pageSize);
        Page<CulturalHeritage> culturalHeritagePage = culturalHeritageMapper.selectPage(page, queryWrapper);
        
        List<CulturalHeritageVO> culturalHeritageVOList = new ArrayList<>();
        for (CulturalHeritage culturalHeritage : culturalHeritagePage.getRecords()) {
            CulturalHeritageVO culturalHeritageVO = convertVO(culturalHeritage);
            culturalHeritageVOList.add(culturalHeritageVO);
        }
        
        // 返回结果
        PageResult<List<CulturalHeritageVO>> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setSize(page.getSize());
        pageResult.setCurrent(page.getCurrent());
        pageResult.setRecords(culturalHeritageVOList);
        return pageResult;
    }

    /**
     * 转换为VO对象
     *
     * @param culturalHeritage
     * @return
     */
    private CulturalHeritageVO convertVO(CulturalHeritage culturalHeritage) {
        CulturalHeritageVO culturalHeritageVO = new CulturalHeritageVO();
        BeanUtils.copyProperties(culturalHeritage, culturalHeritageVO);
        
        // 获取分类名称
        if (culturalHeritage.getCategoryId() != null) {
            IchType ichType = ichTypeMapper.selectById(culturalHeritage.getCategoryId());
            if (ichType != null) {
                culturalHeritageVO.setCategoryName(ichType.getName());
            }
        }
        
        return culturalHeritageVO;
    }
}