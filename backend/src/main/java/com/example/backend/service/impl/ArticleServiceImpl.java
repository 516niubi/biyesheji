package com.example.backend.service.impl;

import com.example.backend.entity.Article;
import com.example.backend.entity.vo.article.ArticleVO;
import com.example.backend.mapper.ArticleMapper;
import com.example.backend.service.IArticleService;
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
import com.example.backend.entity.Inheritor;
import com.example.backend.utils.BackendAuthHelper;
import com.example.backend.utils.PublisherNameResolver;
import com.example.backend.utils.PublisherNameResolver.PublisherView;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 资讯表 服务实现类
 * </p>
 *
 * @author
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {
    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private PublisherNameResolver publisherNameResolver;

    /**
     * 新增
     *
     * @param request
     * @return
     */
    @Override
    public Integer add(Article request) {
        BackendAuthHelper.requireAdminOrInheritor();
        Inheritor inh = BackendAuthHelper.tryLoginInheritor();
        Article saveData = new Article();
        BeanUtils.copyProperties(request, saveData);
        if (inh != null) {
            saveData.setCreatorId(inh.getId());
        } else {
            saveData.setCreatorId(null);
        }
        articleMapper.insert(saveData);
        return saveData.getId();
    }

    /**
     * 批量新增
     *
     * @param request
     * @return
     */
    @Override
    public Boolean batchAdd(List<Article> request) {
        BackendAuthHelper.requireAdminOrInheritor();
        Inheritor inh = BackendAuthHelper.tryLoginInheritor();
        for (Article a : request) {
            if (inh != null) {
                a.setCreatorId(inh.getId());
            } else {
                a.setCreatorId(null);
            }
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
        Article row = articleMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "数据不存在");
        }
        BackendAuthHelper.requireAdminOrInheritor();
        assertArticleOwned(row);
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
        BackendAuthHelper.requireAdminOrInheritor();
        for (Integer id : ids) {
            Article row = articleMapper.selectById(id);
            if (row != null) {
                assertArticleOwned(row);
            }
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
    public Boolean edit(Integer id, Article request) {
        Article findData = articleMapper.selectById(id);
        if (findData == null) {
            throw new BusinessException(CodeEnum.SYSTEM_ERROR);
        }
        BackendAuthHelper.requireAdminOrInheritor();
        assertArticleOwned(findData);
        Integer preservedCreator = findData.getCreatorId();
        BeanUtils.copyProperties(request, findData);
        findData.setCreatorId(preservedCreator);
        findData.setId(id);
        return updateById(findData);
    }

    /**
     * 分页查询
     *
     * @return
     */
    private void assertArticleOwned(Article row) {
        Inheritor inh = BackendAuthHelper.tryLoginInheritor();
        if (inh == null) {
            return;
        }
        if (row.getCreatorId() == null || !row.getCreatorId().equals(inh.getId())) {
            throw new BusinessException(CodeEnum.AUTH_ERROR, "仅能管理本人发布的资讯");
        }
    }

    @Override
    public PageResult<List<ArticleVO>> queryPage(Integer pageNum, Integer pageSize, String title, String intro, Integer creatorIdFilter) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(title)) {
            queryWrapper.like("title", title);
        }
        if (CharSequenceUtil.isNotBlank(intro)) {
            queryWrapper.like("intro", intro);
        }
        if (creatorIdFilter != null) {
            queryWrapper.eq("creator_id", creatorIdFilter);
        }
        // ID 降序
        queryWrapper.orderByDesc("id");
        Page<Article> page = page(new Page<>(pageNum, pageSize), queryWrapper);
        // 返回结果
        PageResult<List<ArticleVO>> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setSize(page.getSize());
        pageResult.setCurrent(page.getCurrent());
        List<ArticleVO> articleVOS = convertVO(page.getRecords());
        pageResult.setRecords(articleVOS);
        return pageResult;
    }

    /**
     * 查询全部
     *
     * @return
     */
    @Override
    public List<Article> getAll() {
        return list();
    }


    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    @Override
    public Article getByIdDetail(Integer id) {
        Article article = getById(id);
        if (article != null) {
            PublisherView pv = publisherNameResolver.resolveView(article.getCreatorId());
            article.setPublisherName(pv.getName());
            article.setPublisherAvatar(pv.getAvatar());
        }
        return article;
    }
    
    /**
     * 根据id查询详情并增加浏览次数
     *
     * @param id
     * @return
     */
    @Override
    public Article getByIdDetailAndIncreaseViewCount(Integer id) {
        Article article = getById(id);
        if (article != null) {
            // 增加浏览次数
            article.setViewCount(article.getViewCount() == null ? 1 : article.getViewCount() + 1);
            updateById(article);
            PublisherView pv = publisherNameResolver.resolveView(article.getCreatorId());
            article.setPublisherName(pv.getName());
            article.setPublisherAvatar(pv.getAvatar());
        }
        return article;
    }
    
    @Override
    public PageResult<List<ArticleVO>> getHotPage(Integer pageNum, Integer pageSize, String orderBy, String orderType) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        
        // 根据排序类型设置排序
        if ("desc".equalsIgnoreCase(orderType)) {
            queryWrapper.orderByDesc(orderBy);
        } else {
            queryWrapper.orderByAsc(orderBy);
        }
        
        Page<Article> page = page(new Page<>(pageNum, pageSize), queryWrapper);
        List<ArticleVO> articleVOList = convertVO(page.getRecords());
        
        // 返回结果
        PageResult<List<ArticleVO>> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setSize(page.getSize());
        pageResult.setCurrent(page.getCurrent());
        pageResult.setRecords(articleVOList);
        return pageResult;
    }

    // 转换VO
    private List<ArticleVO> convertVO(List<Article> articles) {
        ArrayList<ArticleVO> list = new ArrayList<>();
        for (Article article : articles) {
            ArticleVO articleVO = new ArticleVO();
            BeanUtils.copyProperties(article, articleVO);
            articleVO.setCreatorId(article.getCreatorId());
            PublisherView pv = publisherNameResolver.resolveView(article.getCreatorId());
            articleVO.setPublisherName(pv.getName());
            articleVO.setPublisherAvatar(pv.getAvatar());
            list.add(articleVO);
        }
        return list;
    }
}