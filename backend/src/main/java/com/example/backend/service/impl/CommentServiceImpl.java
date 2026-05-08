package com.example.backend.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Comment;
import com.example.backend.entity.CulturalHeritage;
import com.example.backend.entity.Inheritor;
import com.example.backend.entity.User;
import com.example.backend.entity.request.comment.CommentAddRequest;
import com.example.backend.entity.vo.comment.CommentVO;
import com.example.backend.mapper.CommentMapper;
import com.example.backend.mapper.CulturalHeritageMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.ICommentService;
import com.example.backend.service.IInheritorService;
import com.example.backend.common.enums.CodeEnum;
import com.example.backend.exception.BusinessException;
import com.example.backend.utils.BackendAuthHelper;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    /** 与 {@link com.example.backend.service.impl.InheritorServiceImpl} 中登录 id 前缀一致 */
    private static final String INHERITOR_LOGIN_PREFIX = "inheritor:";

    @Resource
    private UserMapper userMapper;

    @Resource
    private CulturalHeritageMapper culturalHeritageMapper;

    @Resource
    private IInheritorService inheritorService;

    @Override
    public boolean add(Comment comment) {
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        return save(comment);
    }

    @Override
    public boolean addRich(CommentAddRequest req) {
        User u = BackendAuthHelper.tryLoginFrontUser();
        Inheritor inh = BackendAuthHelper.tryLoginInheritor();
        if (u == null && inh == null) {
            Integer uid = resolveLoggedInFrontUserIdOrNull();
            if (uid != null) {
                u = userMapper.selectById(uid);
            }
        }
        if (u == null && inh == null) {
            Integer iid = resolveLoggedInInheritorIdOrNull();
            if (iid != null) {
                inh = inheritorService.getById(iid);
            }
        }
        if (u == null && inh == null) {
            throw new BusinessException(CodeEnum.AUTH_ERROR, "请先登录后再评论");
        }
        if (req.getHeritageId() == null) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "文物不能为空");
        }
        CulturalHeritage heritage = culturalHeritageMapper.selectById(req.getHeritageId());
        if (heritage == null) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "文物不存在");
        }

        String content = req.getContent() == null ? "" : req.getContent().trim();
        List<String> imgs = req.getImages() == null ? Collections.emptyList() : req.getImages();
        if (imgs.size() > 9) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "最多上传 9 张图片");
        }
        if (content.isEmpty() && imgs.isEmpty()) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "请输入文字或上传图片");
        }

        if (req.getParentId() != null) {
            Comment parent = getById(req.getParentId());
            if (parent == null || !parent.getHeritageId().equals(req.getHeritageId())) {
                throw new BusinessException(CodeEnum.PARAMS_ERROR, "回复的评论不存在");
            }
        }

        Comment row = new Comment();
        row.setHeritageId(req.getHeritageId());
        row.setParentId(req.getParentId());
        row.setContent(content.isEmpty() ? null : content);
        if (!imgs.isEmpty()) {
            row.setImages(JSONUtil.toJsonStr(imgs));
        }
        row.setCreateTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());

        if (u != null) {
            row.setUserId(u.getId());
            row.setInheritorId(null);
        } else {
            row.setUserId(null);
            row.setInheritorId(inh.getId());
        }

        return save(row);
    }

    @Override
    public boolean deleteById(Integer id) {
        BackendAuthHelper.requireAdminOrInheritor();
        Comment row = getById(id);
        if (row != null) {
            assertCommentOnOwnedHeritage(row);
        }
        Set<Integer> all = new HashSet<>();
        collectDescendants(id, all);
        return removeByIds(all);
    }

    @Override
    public boolean deleteBatch(List<Integer> ids) {
        BackendAuthHelper.requireAdminOrInheritor();
        Set<Integer> all = new HashSet<>();
        for (Integer id : ids) {
            Comment row = getById(id);
            if (row != null) {
                assertCommentOnOwnedHeritage(row);
            }
            collectDescendants(id, all);
        }
        return removeByIds(all);
    }

    private void collectDescendants(Integer id, Set<Integer> out) {
        out.add(id);
        List<Comment> children = list(new QueryWrapper<Comment>().eq("parent_id", id));
        for (Comment c : children) {
            collectDescendants(c.getId(), out);
        }
    }

    @Override
    public boolean updateById(Comment comment) {
        Comment existing = getById(comment.getId());
        if (existing == null) {
            return false;
        }
        BackendAuthHelper.requireAdminOrInheritor();
        assertCommentOnOwnedHeritage(existing);
        if (comment.getContent() != null) {
            existing.setContent(comment.getContent());
        }
        existing.setUpdateTime(LocalDateTime.now());
        return super.updateById(existing);
    }

    @Override
    public Comment selectById(Integer id) {
        return getById(id);
    }

    @Override
    public List<Comment> selectAll() {
        return list();
    }

    private void assertCommentOnOwnedHeritage(Comment comment) {
        Inheritor inh = BackendAuthHelper.tryLoginInheritor();
        if (inh == null) {
            return;
        }
        CulturalHeritage h = culturalHeritageMapper.selectById(comment.getHeritageId());
        if (h == null || h.getCreatorId() == null || !h.getCreatorId().equals(inh.getId())) {
            throw new BusinessException(CodeEnum.AUTH_ERROR, "仅能管理本人文物下的评论");
        }
    }

    @Override
    public Page<CommentVO> selectPage(Integer pageNum, Integer pageSize, String userName, Integer heritageCreatorAdminId) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();

        if (heritageCreatorAdminId != null) {
            QueryWrapper<CulturalHeritage> hq = new QueryWrapper<>();
            hq.eq("creator_id", heritageCreatorAdminId);
            List<CulturalHeritage> hers = culturalHeritageMapper.selectList(hq);
            List<Integer> hid = hers.stream().map(CulturalHeritage::getId).collect(Collectors.toList());
            if (hid.isEmpty()) {
                Page<CommentVO> empty = new Page<>(pageNum, pageSize);
                empty.setTotal(0);
                empty.setRecords(Collections.emptyList());
                return empty;
            }
            queryWrapper.in("heritage_id", hid);
        }

        if (userName != null && !userName.trim().isEmpty()) {
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.like("nick_name", userName);
            List<User> users = userMapper.selectList(userQueryWrapper);
            if (users.isEmpty()) {
                Page<CommentVO> empty = new Page<>(pageNum, pageSize);
                empty.setTotal(0);
                empty.setRecords(Collections.emptyList());
                return empty;
            }
            List<Integer> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            queryWrapper.in("user_id", userIds);
        }

        queryWrapper.orderByDesc("create_time");
        Page<Comment> commentPage = page(page, queryWrapper);

        Page<CommentVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(commentPage.getTotal());
        voPage.setPages(commentPage.getPages());

        List<CommentVO> voList = commentPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public Page<CommentVO> selectManageRootTreePage(Integer pageNum, Integer pageSize, String userName, Integer heritageCreatorAdminId) {
        Page<Comment> mpPage = new Page<>(pageNum, pageSize);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();

        if (heritageCreatorAdminId != null) {
            QueryWrapper<CulturalHeritage> hq = new QueryWrapper<>();
            hq.eq("creator_id", heritageCreatorAdminId);
            List<CulturalHeritage> hers = culturalHeritageMapper.selectList(hq);
            List<Integer> hid = hers.stream().map(CulturalHeritage::getId).collect(Collectors.toList());
            if (hid.isEmpty()) {
                Page<CommentVO> empty = new Page<>(pageNum, pageSize);
                empty.setTotal(0);
                empty.setRecords(Collections.emptyList());
                return empty;
            }
            queryWrapper.in("heritage_id", hid);
        }

        if (userName != null && !userName.trim().isEmpty()) {
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.like("nick_name", userName);
            List<User> users = userMapper.selectList(userQueryWrapper);
            if (users.isEmpty()) {
                Page<CommentVO> empty = new Page<>(pageNum, pageSize);
                empty.setTotal(0);
                empty.setRecords(Collections.emptyList());
                return empty;
            }
            List<Integer> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            queryWrapper.in("user_id", userIds);
        }

        queryWrapper.isNull("parent_id");
        queryWrapper.orderByDesc("create_time");
        Page<Comment> rootPage = page(mpPage, queryWrapper);

        Page<CommentVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(rootPage.getTotal());
        voPage.setPages(rootPage.getPages());
        voPage.setCurrent(rootPage.getCurrent());
        voPage.setSize(rootPage.getSize());

        if (rootPage.getRecords().isEmpty()) {
            voPage.setRecords(Collections.emptyList());
            return voPage;
        }

        Set<Integer> heritageIds = rootPage.getRecords().stream()
                .map(Comment::getHeritageId)
                .collect(Collectors.toSet());
        QueryWrapper<Comment> allQ = new QueryWrapper<>();
        allQ.in("heritage_id", heritageIds);
        List<Comment> allFlat = list(allQ);

        Map<Integer, List<Comment>> byParentId = new HashMap<>();
        for (Comment c : allFlat) {
            if (c.getParentId() == null) {
                continue;
            }
            byParentId.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(c);
        }
        for (List<Comment> lst : byParentId.values()) {
            lst.sort(Comparator.comparing(Comment::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder())));
        }

        List<CommentVO> records = new ArrayList<>();
        for (Comment root : rootPage.getRecords()) {
            CommentVO rootVo = convertToVO(root);
            fillManageChildren(rootVo, root.getId(), byParentId);
            records.add(rootVo);
        }
        voPage.setRecords(records);
        return voPage;
    }

    private void fillManageChildren(CommentVO parentVo, Integer parentDbId, Map<Integer, List<Comment>> byParentId) {
        List<Comment> kids = byParentId.getOrDefault(parentDbId, Collections.emptyList());
        for (Comment ch : kids) {
            CommentVO chVo = convertToVO(ch);
            fillManageChildren(chVo, ch.getId(), byParentId);
            parentVo.getChildren().add(chVo);
        }
    }

    @Override
    public List<CommentVO> selectByHeritageId(Integer heritageId) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("heritage_id", heritageId);
        List<Comment> flat = list(queryWrapper);
        if (flat.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, CommentVO> map = new LinkedHashMap<>();
        for (Comment c : flat) {
            map.put(c.getId(), convertToVO(c));
        }

        Set<Integer> allIds = flat.stream().map(Comment::getId).collect(Collectors.toSet());
        List<CommentVO> roots = new ArrayList<>();
        for (Comment c : flat) {
            CommentVO vo = map.get(c.getId());
            Integer pid = c.getParentId();
            if (pid == null || !allIds.contains(pid)) {
                roots.add(vo);
            } else {
                CommentVO parentVo = map.get(pid);
                vo.setReplyToName(parentVo.getUserName());
                parentVo.getChildren().add(vo);
            }
        }

        roots.sort(Comparator.comparing(CommentVO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        for (CommentVO root : roots) {
            sortChildrenRecursively(root);
        }
        return roots;
    }

    private void sortChildrenRecursively(CommentVO node) {
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            return;
        }
        node.getChildren().sort(Comparator.comparing(CommentVO::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder())));
        for (CommentVO ch : node.getChildren()) {
            sortChildrenRecursively(ch);
        }
    }

    @Override
    public Page<CommentVO> selectRepliesToUser(Integer userId, Integer pageNum, Integer pageSize) {
        QueryWrapper<Comment> mine = new QueryWrapper<>();
        mine.eq("user_id", userId);
        List<Comment> myComments = list(mine);
        List<Integer> mids = myComments.stream().map(Comment::getId).collect(Collectors.toList());
        if (mids.isEmpty()) {
            Page<CommentVO> empty = new Page<>(pageNum, pageSize);
            empty.setTotal(0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        qw.in("parent_id", mids);
        qw.orderByDesc("create_time");
        Page<Comment> pg = new Page<>(pageNum, pageSize);
        Page<Comment> result = page(pg, qw);
        Page<CommentVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(result.getTotal());
        voPage.setPages(result.getPages());
        voPage.setCurrent(result.getCurrent());
        voPage.setSize(result.getSize());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public Page<CommentVO> selectMyPosts(Integer pageNum, Integer pageSize) {
        Page<Comment> pg = new Page<>(pageNum, pageSize);
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        Integer inhId = resolveLoggedInInheritorIdOrNull();
        Integer uid = resolveLoggedInFrontUserIdOrNull();
        if (inhId != null) {
            qw.eq("inheritor_id", inhId);
        } else if (uid != null) {
            qw.eq("user_id", uid);
        } else {
            throw new BusinessException(CodeEnum.AUTH_ERROR, "请先登录");
        }
        qw.orderByDesc("create_time");
        Page<Comment> res = page(pg, qw);
        Page<CommentVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(res.getTotal());
        voPage.setPages(res.getPages());
        voPage.setCurrent(res.getCurrent());
        voPage.setSize(res.getSize());
        voPage.setRecords(res.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public boolean deleteMyComment(Integer id) {
        Comment row = getById(id);
        if (row == null) {
            return false;
        }
        Integer inhId = resolveLoggedInInheritorIdOrNull();
        Integer uid = resolveLoggedInFrontUserIdOrNull();
        if (inhId != null) {
            if (row.getInheritorId() == null || !row.getInheritorId().equals(inhId)) {
                throw new BusinessException(CodeEnum.AUTH_ERROR, "只能删除自己的评论");
            }
        } else if (uid != null) {
            if (row.getUserId() == null || !row.getUserId().equals(uid)) {
                throw new BusinessException(CodeEnum.AUTH_ERROR, "只能删除自己的评论");
            }
        } else {
            throw new BusinessException(CodeEnum.AUTH_ERROR, "请先登录");
        }
        Set<Integer> all = new HashSet<>();
        collectDescendants(id, all);
        return removeByIds(all);
    }

    @Override
    public Page<CommentVO> selectMyRepliesForCurrentLogin(Integer pageNum, Integer pageSize) {
        Integer inhId = resolveLoggedInInheritorIdOrNull();
        if (inhId != null) {
            return selectRepliesToInheritor(inhId, pageNum, pageSize);
        }
        Integer uid = resolveLoggedInFrontUserIdOrNull();
        if (uid != null) {
            return selectRepliesToUser(uid, pageNum, pageSize);
        }
        throw new BusinessException(CodeEnum.AUTH_ERROR, "请先登录");
    }

    /**
     * JWT 等场景下 Session 里的 USER_KEY 可能无法反序列化为 User，此时用登录 id + user 表校验。
     */
    private Integer resolveLoggedInFrontUserIdOrNull() {
        User u = BackendAuthHelper.tryLoginFrontUser();
        if (u != null && u.getId() != null) {
            return u.getId();
        }
        Object key = BackendAuthHelper.tryLoginPrincipal();
        if (key instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) key;
            if ("user".equals(String.valueOf(m.get("role")))) {
                Object idObj = m.get("id");
                if (idObj instanceof Number) {
                    return ((Number) idObj).intValue();
                }
            }
        }
        try {
            Object lid = StpUtil.getLoginIdDefaultNull();
            if (lid instanceof Number) {
                int n = ((Number) lid).intValue();
                User row = userMapper.selectById(n);
                if (row != null && "user".equals(row.getRole())) {
                    return n;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /** 传承人登录 id 为 inheritor:主键；兼容 Session 中为 Inheritor 实例 */
    private Integer resolveLoggedInInheritorIdOrNull() {
        try {
            Object lid = StpUtil.getLoginIdDefaultNull();
            if (lid instanceof String && ((String) lid).startsWith(INHERITOR_LOGIN_PREFIX)) {
                return Integer.parseInt(((String) lid).substring(INHERITOR_LOGIN_PREFIX.length()));
            }
        } catch (Exception ignored) {
        }
        Inheritor inh = BackendAuthHelper.tryLoginInheritor();
        if (inh != null && inh.getId() != null) {
            return inh.getId();
        }
        return null;
    }

    @Override
    public Page<CommentVO> selectRepliesToInheritor(Integer inheritorId, Integer pageNum, Integer pageSize) {
        QueryWrapper<Comment> mine = new QueryWrapper<>();
        mine.eq("inheritor_id", inheritorId);
        List<Comment> myComments = list(mine);
        List<Integer> mids = myComments.stream().map(Comment::getId).collect(Collectors.toList());
        if (mids.isEmpty()) {
            Page<CommentVO> empty = new Page<>(pageNum, pageSize);
            empty.setTotal(0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        qw.in("parent_id", mids);
        qw.orderByDesc("create_time");
        Page<Comment> pg = new Page<>(pageNum, pageSize);
        Page<Comment> result = page(pg, qw);
        Page<CommentVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(result.getTotal());
        voPage.setPages(result.getPages());
        voPage.setCurrent(result.getCurrent());
        voPage.setSize(result.getSize());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }

    private CommentVO convertToVO(Comment comment) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);
        vo.setImageUrls(new ArrayList<>());
        vo.setChildren(new ArrayList<>());
        vo.setFromInheritor(false);

        if (StrUtil.isNotBlank(comment.getImages())) {
            try {
                vo.setImageUrls(JSONUtil.toList(comment.getImages(), String.class));
            } catch (Exception ignored) {
                vo.setImageUrls(new ArrayList<>());
            }
        }

        if (comment.getInheritorId() != null) {
            Inheritor inh = inheritorService.getById(comment.getInheritorId());
            if (inh != null) {
                if (StrUtil.isNotBlank(inh.getNickName())) {
                    vo.setUserName(inh.getNickName());
                } else if (StrUtil.isNotBlank(inh.getUsername())) {
                    vo.setUserName(inh.getUsername());
                } else {
                    vo.setUserName("传承人");
                }
                vo.setAvatar(inh.getAvatar());
                vo.setFromInheritor(true);
            }
        } else if (comment.getUserId() != null) {
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                vo.setUserName(StrUtil.isNotBlank(user.getNickName()) ? user.getNickName() : user.getUsername());
                vo.setAvatar(user.getAvatar());
            }
        }

        if (vo.getUserName() == null) {
            vo.setUserName("匿名用户");
        }

        if (comment.getHeritageId() != null) {
            CulturalHeritage heritage = culturalHeritageMapper.selectById(comment.getHeritageId());
            if (heritage != null) {
                vo.setHeritageName(heritage.getName());
            }
        }

        if (comment.getParentId() != null) {
            Comment parent = getById(comment.getParentId());
            if (parent != null) {
                vo.setReplyToName(resolveAuthorDisplayName(parent));
            }
        }

        return vo;
    }

    private String resolveAuthorDisplayName(Comment c) {
        if (c.getInheritorId() != null) {
            Inheritor inh = inheritorService.getById(c.getInheritorId());
            if (inh != null) {
                if (StrUtil.isNotBlank(inh.getNickName())) {
                    return inh.getNickName();
                }
                if (StrUtil.isNotBlank(inh.getUsername())) {
                    return inh.getUsername();
                }
                return "传承人";
            }
        }
        if (c.getUserId() != null) {
            User user = userMapper.selectById(c.getUserId());
            if (user != null) {
                return StrUtil.isNotBlank(user.getNickName()) ? user.getNickName() : user.getUsername();
            }
        }
        return "匿名用户";
    }
}
