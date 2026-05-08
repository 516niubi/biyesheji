package com.example.backend.service;

import com.example.backend.entity.Article;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.common.model.PageResult;
import com.example.backend.entity.vo.article.ArticleVO;

import java.util.List;

/**
 * <p>
 * 资讯表 服务类
 * </p>
 *
 * @author 
 * @since 2025-01-20
 */
public interface IArticleService extends IService<Article> {
        /**
        * 新增
        * @param request
        * @return
        */
        Integer add(Article request);

        /**
         * 批量新增
         *
         * @param request
         * @return
         */
        Boolean batchAdd(List<Article> request);

        /**
         * 删除
         * @param id
         * @return
         */
        Boolean del(Integer id);
        
        /**
         * 批量删除
         * @param ids 批量ID列表
         * @return 是否成功
         */
        Boolean batchDel(List<Integer> ids);

        /**
         * 编辑
         * @param id id
         * @param request 请求参数
         * @return
         */
        Boolean edit(Integer id, Article request);

        /**
         * @param creatorIdFilter 非空时仅查询该创建人的资讯
         */
        PageResult<List<ArticleVO>> queryPage(Integer pageNum, Integer pageSize, String title, String intro, Integer creatorIdFilter);

        default PageResult<List<ArticleVO>> queryPage(Integer pageNum, Integer pageSize, String title, String intro) {
            return queryPage(pageNum, pageSize, title, intro, null);
        }

        /**
         * 查询全部
         * @return
         */
        List<Article> getAll();

        /**
         * 根据id查询
         *
         * @param id
         * @return
         */
        Article getByIdDetail(Integer id);
        
        /**
         * 根据id查询详情并增加浏览次数
         *
         * @param id
         * @return
         */
        Article getByIdDetailAndIncreaseViewCount(Integer id);
        
        /**
         * 获取热门资讯分页数据
         * @param pageNum 页码
         * @param pageSize 页大小
         * @param orderBy 排序字段
         * @param orderType 排序类型
         * @return 分页结果
         */
        PageResult<List<ArticleVO>> getHotPage(Integer pageNum, Integer pageSize, String orderBy, String orderType);
}