package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Comment;
import com.example.backend.entity.CulturalHeritage;
import com.example.backend.entity.User;
import com.example.backend.mapper.CommentMapper;
import com.example.backend.mapper.CulturalHeritageMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.ICommentService;
import com.example.backend.entity.vo.comment.CommentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private CulturalHeritageMapper culturalHeritageMapper;
    
    @Override
    public boolean add(Comment comment) {
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        return save(comment);
    }
    
    @Override
    public boolean deleteById(Integer id) {
        return removeById(id);
    }
    
    @Override
    public boolean deleteBatch(List<Integer> ids) {
        return removeByIds(ids);
    }
    
    @Override
    public boolean updateById(Comment comment) {
        comment.setUpdateTime(LocalDateTime.now());
        return updateById(comment);
    }
    
    @Override
    public Comment selectById(Integer id) {
        return getById(id);
    }
    
    @Override
    public List<Comment> selectAll() {
        return list();
    }
    
    @Override
    public Page<CommentVO> selectPage(Integer pageNum, Integer pageSize, String userName) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        
        // 如果提供了用户名，先根据用户名查询用户ID
        if (userName != null && !userName.trim().isEmpty()) {
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.like("name", userName);
            List<User> users = userMapper.selectList(userQueryWrapper);
            if (users.isEmpty()) {
                // 如果没有找到匹配的用户，返回空结果
                return new Page<>(pageNum, pageSize);
            }
            List<Integer> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            queryWrapper.in("user_id", userIds);
        }
        
        queryWrapper.orderByDesc("create_time");
        Page<Comment> commentPage = page(page, queryWrapper);
        
        // 转换为CommentVO
        Page<CommentVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(commentPage.getTotal());
        voPage.setPages(commentPage.getPages());
        
        List<CommentVO> voList = commentPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    @Override
    public List<CommentVO> selectByHeritageId(Integer heritageId) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("heritage_id", heritageId);
        queryWrapper.orderByDesc("create_time");
        
        List<Comment> comments = list(queryWrapper);
        return comments.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    /**
     * 将Comment转换为CommentVO
     */
    private CommentVO convertToVO(Comment comment) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);
        
        // 获取用户名
        if (comment.getUserId() != null) {
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                vo.setUserName(user.getNickName());
                vo.setAvatar(user.getAvatar());
            }
        }
        
        // 获取文物名称
        if (comment.getHeritageId() != null) {
            CulturalHeritage heritage = culturalHeritageMapper.selectById(comment.getHeritageId());
            if (heritage != null) {
                vo.setHeritageName(heritage.getName());
            }
        }
        
        return vo;
    }
}