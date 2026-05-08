package com.example.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Video;
import com.example.backend.entity.vo.video.VideoVO;
import com.example.backend.mapper.VideoMapper;
import com.example.backend.service.IVideoService;
import com.example.backend.common.model.PageResult;
import com.example.backend.exception.BusinessException;
import com.example.backend.common.enums.CodeEnum;
import com.example.backend.entity.Inheritor;
import com.example.backend.utils.BackendAuthHelper;
import com.example.backend.utils.PublisherNameResolver;
import com.example.backend.utils.PublisherNameResolver.PublisherView;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 宣传视频表 服务实现类
 * </p>
 *
 * @author 
 * @since 2025-01-20
 */
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements IVideoService {
    @Resource
    private VideoMapper videoMapper;

    @Resource
    private PublisherNameResolver publisherNameResolver;

    /**
     * 新增
     *
     * @param request
     * @return
     */
    @Override
    public Integer add(Video request) {
        BackendAuthHelper.requireAdminOrInheritor();
        Inheritor inh = BackendAuthHelper.tryLoginInheritor();
        Video saveData = new Video();
        BeanUtils.copyProperties(request, saveData);
        if (inh != null) {
            saveData.setCreatorId(inh.getId());
        } else {
            saveData.setCreatorId(null);
        }
        saveData.setCreateTime(new Date());
        saveData.setUpdateTime(new Date());
        if (saveData.getViewCount() == null) {
            saveData.setViewCount(0);
        }
        videoMapper.insert(saveData);
        return saveData.getId();
    }

    /**
     * 批量新增
     *
     * @param request
     * @return
     */
    @Override
    public Boolean batchAdd(List<Video> request) {
        BackendAuthHelper.requireAdminOrInheritor();
        Inheritor inh = BackendAuthHelper.tryLoginInheritor();
        Date now = new Date();
        for (Video v : request) {
            if (inh != null) {
                v.setCreatorId(inh.getId());
            } else {
                v.setCreatorId(null);
            }
            if (v.getCreateTime() == null) {
                v.setCreateTime(now);
            }
            if (v.getUpdateTime() == null) {
                v.setUpdateTime(now);
            }
            if (v.getViewCount() == null) {
                v.setViewCount(0);
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
        Video row = videoMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "数据不存在");
        }
        BackendAuthHelper.requireAdminOrInheritor();
        assertVideoOwned(row);
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
            Video row = videoMapper.selectById(id);
            if (row != null) {
                assertVideoOwned(row);
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
    public Boolean edit(Integer id, Video request) {
        Video findData = videoMapper.selectById(id);
        if (findData == null) {
            throw new BusinessException(CodeEnum.SYSTEM_ERROR);
        }
        BackendAuthHelper.requireAdminOrInheritor();
        assertVideoOwned(findData);
        Integer preservedCreator = findData.getCreatorId();
        BeanUtils.copyProperties(request, findData);
        findData.setCreatorId(preservedCreator);
        findData.setId(id);
        findData.setUpdateTime(new Date());
        return updateById(findData);
    }

    /**
     * 分页查询
     *
     * @return
     */
    private void assertVideoOwned(Video row) {
        Inheritor inh = BackendAuthHelper.tryLoginInheritor();
        if (inh == null) {
            return;
        }
        if (row.getCreatorId() == null || !row.getCreatorId().equals(inh.getId())) {
            throw new BusinessException(CodeEnum.AUTH_ERROR, "仅能管理本人发布的视频");
        }
    }

    @Override
    public PageResult<List<VideoVO>> queryPage(Integer pageNum, Integer pageSize, String title, Integer creatorIdFilter) {
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(title)) {
            queryWrapper.like("title", title);
        }
        if (creatorIdFilter != null) {
            queryWrapper.eq("creator_id", creatorIdFilter);
        }
        // ID 降序
        queryWrapper.orderByDesc("id");
        Page<Video> page = page(new Page<>(pageNum, pageSize), queryWrapper);
        // 返回结果
        PageResult<List<VideoVO>> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setSize(page.getSize());
        pageResult.setCurrent(page.getCurrent());
        List<VideoVO> videoVOS = convertVO(page.getRecords());
        pageResult.setRecords(videoVOS);
        return pageResult;
    }

    private List<VideoVO> convertVO(List<Video> videos) {
        return videos.stream().map(video -> {
            VideoVO videoVO = new VideoVO();
            BeanUtil.copyProperties(video, videoVO);
            PublisherView pv = publisherNameResolver.resolveView(video.getCreatorId());
            videoVO.setPublisherName(pv.getName());
            videoVO.setPublisherAvatar(pv.getAvatar());
            return videoVO;
        }).collect(Collectors.toList());
    }

    /**
     * 查询全部
     *
     * @return
     */
    @Override
    public List<Video> getAll() {
        return list();
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    @Override
    public Video getByIdDetail(Integer id) {
        Video video = getById(id);
        if (video != null) {
            int vc = video.getViewCount() == null ? 0 : video.getViewCount();
            video.setViewCount(vc + 1);
            updateById(video);
            PublisherView pv = publisherNameResolver.resolveView(video.getCreatorId());
            video.setPublisherName(pv.getName());
            video.setPublisherAvatar(pv.getAvatar());
        }
        return video;
    }
}