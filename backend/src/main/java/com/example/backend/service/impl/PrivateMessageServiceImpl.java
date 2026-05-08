package com.example.backend.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Admin;
import com.example.backend.entity.Inheritor;
import com.example.backend.entity.PrivateMessage;
import com.example.backend.entity.PrivateMessageThread;
import com.example.backend.entity.User;
import com.example.backend.entity.vo.privatemessage.FrontUserBriefVO;
import com.example.backend.entity.vo.privatemessage.PmContactAdminVO;
import com.example.backend.entity.vo.privatemessage.PrivateMessageItemVO;
import com.example.backend.entity.vo.privatemessage.PrivateMessageThreadListVO;
import com.example.backend.mapper.AdminMapper;
import com.example.backend.mapper.InheritorMapper;
import com.example.backend.mapper.PrivateMessageMapper;
import com.example.backend.mapper.PrivateMessageThreadMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.IPrivateMessageService;
import com.example.backend.utils.BackendAuthHelper;
import com.example.backend.common.enums.CodeEnum;
import com.example.backend.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageThreadMapper, PrivateMessageThread>
        implements IPrivateMessageService {

    private static final int PREVIEW_LEN = 80;

    private static final LocalDateTime EPOCH = LocalDateTime.of(1970, 1, 1, 0, 0);

    @Resource
    private PrivateMessageMapper privateMessageMapper;

    @Resource
    private InheritorMapper inheritorMapper;

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private UserMapper userMapper;

    private static int kindOf(PrivateMessageThread t) {
        return t.getThreadKind() != null ? t.getThreadKind() : PrivateMessageThread.KIND_PLATFORM;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrivateMessageThread createThread(String subject, String content, Integer platformTargetAdminId) {
        Inheritor inh = BackendAuthHelper.requireLoginInheritor();
        String sub = CharSequenceUtil.blankToDefault(CharSequenceUtil.trim(subject), "私信");
        if (sub.length() > 200) {
            sub = sub.substring(0, 200);
        }
        String body = CharSequenceUtil.trim(content);
        if (CharSequenceUtil.isBlank(body)) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "私信内容不能为空");
        }
        if (body.length() > 5000) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "内容过长（最多 5000 字）");
        }

        Integer targetAdminId = null;
        if (platformTargetAdminId != null) {
            Admin tgt = adminMapper.selectById(platformTargetAdminId);
            if (tgt == null || (tgt.getStatus() != null && tgt.getStatus() != 1)) {
                throw new BusinessException(CodeEnum.PARAMS_ERROR, "指定的管理员不存在或不可用");
            }
            targetAdminId = platformTargetAdminId;
        }

        LocalDateTime now = LocalDateTime.now();
        PrivateMessageThread t = null;
        if (targetAdminId != null) {
            QueryWrapper<PrivateMessageThread> existQw = new QueryWrapper<>();
            existQw.eq("inheritor_id", inh.getId())
                    .eq("thread_kind", PrivateMessageThread.KIND_PLATFORM)
                    .eq("platform_target_admin_id", targetAdminId)
                    .orderByDesc("update_time")
                    .last("LIMIT 1");
            List<PrivateMessageThread> hit = list(existQw);
            if (!hit.isEmpty()) {
                t = hit.get(0);
            }
        }

        if (t == null) {
            t = new PrivateMessageThread();
            t.setInheritorId(inh.getId());
            t.setThreadKind(PrivateMessageThread.KIND_PLATFORM);
            t.setFrontUserId(null);
            t.setSubject(sub);
            t.setPlatformTargetAdminId(targetAdminId);
            t.setInheritorLastReadAt(now);
            t.setAdminLastReadAt(null);
            t.setUserLastReadAt(null);
            t.setCreateTime(now);
            t.setUpdateTime(now);
            save(t);
        }

        PrivateMessage m = new PrivateMessage();
        m.setThreadId(t.getId());
        m.setSenderRole(PrivateMessage.ROLE_INHERITOR);
        m.setSenderId(inh.getId());
        m.setContent(body);
        m.setCreateTime(now);
        privateMessageMapper.insert(m);

        t.setUpdateTime(now);
        t.setInheritorLastReadAt(now);
        updateById(t);

        return getById(t.getId());
    }

    @Override
    public Page<PrivateMessageThreadListVO> pageThreadsForInheritor(long pageNum, long pageSize) {
        Inheritor inh = BackendAuthHelper.requireLoginInheritor();
        Page<PrivateMessageThread> pg = new Page<>(pageNum, pageSize);
        QueryWrapper<PrivateMessageThread> qw = new QueryWrapper<>();
        qw.eq("inheritor_id", inh.getId())
                .and(w -> w.eq("thread_kind", PrivateMessageThread.KIND_PLATFORM)
                        .or()
                        .eq("thread_kind", PrivateMessageThread.KIND_USER_INHERITOR))
                .orderByDesc("update_time");
        Page<PrivateMessageThread> res = page(pg, qw);

        Page<PrivateMessageThreadListVO> out = new Page<>(pageNum, pageSize);
        out.setTotal(res.getTotal());
        out.setPages(res.getPages());
        out.setCurrent(res.getCurrent());
        out.setSize(res.getSize());
        out.setRecords(toThreadVOListForInheritor(res.getRecords()));
        return out;
    }

    @Override
    public Page<PrivateMessageThreadListVO> pageThreadsForAdmin(long pageNum, long pageSize, String inheritorNickLike) {
        Admin admin = BackendAuthHelper.requireLoginAdmin();
        QueryWrapper<PrivateMessageThread> qw = new QueryWrapper<>();
        qw.eq("thread_kind", PrivateMessageThread.KIND_PLATFORM);
        qw.and(w -> w.isNull("platform_target_admin_id").or().eq("platform_target_admin_id", admin.getId()));
        if (CharSequenceUtil.isNotBlank(inheritorNickLike)) {
            QueryWrapper<Inheritor> iq = new QueryWrapper<>();
            iq.like("nick_name", inheritorNickLike.trim());
            List<Inheritor> list = inheritorMapper.selectList(iq);
            List<Integer> ids = list.stream().map(Inheritor::getId).collect(Collectors.toList());
            if (ids.isEmpty()) {
                Page<PrivateMessageThreadListVO> empty = new Page<>(pageNum, pageSize);
                empty.setTotal(0);
                empty.setRecords(new ArrayList<>());
                return empty;
            }
            qw.in("inheritor_id", ids);
        }
        qw.orderByDesc("update_time");
        Page<PrivateMessageThread> pg = new Page<>(pageNum, pageSize);
        Page<PrivateMessageThread> res = page(pg, qw);

        Page<PrivateMessageThreadListVO> out = new Page<>(pageNum, pageSize);
        out.setTotal(res.getTotal());
        out.setPages(res.getPages());
        out.setCurrent(res.getCurrent());
        out.setSize(res.getSize());
        out.setRecords(toThreadVOList(res.getRecords(), false));
        return out;
    }

    @Override
    public Page<PrivateMessageThreadListVO> pageThreadsForFrontUser(long pageNum, long pageSize) {
        User u = BackendAuthHelper.tryLoginFrontUser();
        if (u == null) {
            throw new BusinessException(CodeEnum.AUTH_ERROR, "请先登录");
        }
        Page<PrivateMessageThread> pg = new Page<>(pageNum, pageSize);
        QueryWrapper<PrivateMessageThread> qw = new QueryWrapper<>();
        qw.eq("thread_kind", PrivateMessageThread.KIND_USER_INHERITOR)
                .eq("front_user_id", u.getId())
                .orderByDesc("update_time");
        Page<PrivateMessageThread> res = page(pg, qw);

        Page<PrivateMessageThreadListVO> out = new Page<>(pageNum, pageSize);
        out.setTotal(res.getTotal());
        out.setPages(res.getPages());
        out.setCurrent(res.getCurrent());
        out.setSize(res.getSize());
        out.setRecords(toThreadVOListForFrontUser(res.getRecords()));
        return out;
    }

    /**
     * 传承人端平台会话列表头像：
     * 1）已指派管理员 → 用该管理员头像；
     * 2）否则取本会话内最近一条管理员消息的发送者头像；
     * 3）再无则 null（不再回退到「全局第一个管理员」，避免多条会话头像雷同）。
     */
    private String resolvePlatformCounterpartyAvatar(PrivateMessageThread t) {
        Integer targetId = t.getPlatformTargetAdminId();
        if (targetId != null) {
            Admin target = adminMapper.selectById(targetId);
            return target != null ? target.getAvatar() : null;
        }
        QueryWrapper<PrivateMessage> q = new QueryWrapper<>();
        q.eq("thread_id", t.getId())
                .eq("sender_role", PrivateMessage.ROLE_ADMIN)
                .orderByDesc("create_time")
                .last("LIMIT 1");
        PrivateMessage last = privateMessageMapper.selectOne(q);
        if (last != null && last.getSenderId() != null) {
            Admin a = adminMapper.selectById(last.getSenderId());
            return a != null ? a.getAvatar() : null;
        }
        return null;
    }

    private List<PrivateMessageThreadListVO> toThreadVOListForInheritor(List<PrivateMessageThread> threads) {
        List<PrivateMessageThreadListVO> list = new ArrayList<>();
        for (PrivateMessageThread t : threads) {
            PrivateMessageThreadListVO vo = baseThreadVO(t);
            int k = kindOf(t);
            vo.setThreadKind(k);
            if (k == PrivateMessageThread.KIND_PLATFORM) {
                vo.setCounterpartyName("私信");
                vo.setCounterpartyAvatar(resolvePlatformCounterpartyAvatar(t));
            } else {
                User usr = userMapper.selectById(t.getFrontUserId());
                vo.setCounterpartyName(usr != null ? CharSequenceUtil.blankToDefault(usr.getNickName(), usr.getUsername()) : "用户");
                vo.setCounterpartyAvatar(usr != null ? usr.getAvatar() : null);
                vo.setFrontUserId(t.getFrontUserId());
            }
            vo.setHasUnread(hasUnreadForInheritorOnThread(t));
            list.add(vo);
        }
        return list;
    }

    private List<PrivateMessageThreadListVO> toThreadVOListForFrontUser(List<PrivateMessageThread> threads) {
        List<PrivateMessageThreadListVO> list = new ArrayList<>();
        for (PrivateMessageThread t : threads) {
            PrivateMessageThreadListVO vo = baseThreadVO(t);
            vo.setThreadKind(PrivateMessageThread.KIND_USER_INHERITOR);
            Inheritor inh = inheritorMapper.selectById(t.getInheritorId());
            vo.setCounterpartyName(inh != null ? CharSequenceUtil.blankToDefault(inh.getNickName(), inh.getUsername()) : "传承人");
            vo.setInheritorNickName(vo.getCounterpartyName());
            vo.setCounterpartyAvatar(inh != null ? inh.getAvatar() : null);
            vo.setHasUnread(hasUnreadForFrontUserOnThread(t));
            list.add(vo);
        }
        return list;
    }

    private PrivateMessageThreadListVO baseThreadVO(PrivateMessageThread t) {
        PrivateMessageThreadListVO vo = new PrivateMessageThreadListVO();
        vo.setId(t.getId());
        vo.setInheritorId(t.getInheritorId());
        vo.setSubject(t.getSubject());
        vo.setUpdateTime(t.getUpdateTime());
        Inheritor inh = inheritorMapper.selectById(t.getInheritorId());
        vo.setInheritorNickName(inh != null ? CharSequenceUtil.blankToDefault(inh.getNickName(), inh.getUsername()) : "传承人");
        vo.setCounterpartyAvatar(inh != null ? inh.getAvatar() : null);

        QueryWrapper<PrivateMessage> mq = new QueryWrapper<>();
        mq.eq("thread_id", t.getId()).orderByDesc("create_time").last("LIMIT 1");
        PrivateMessage last = privateMessageMapper.selectOne(mq);
        if (last != null) {
            String c = last.getContent();
            vo.setLastMessagePreview(c.length() <= PREVIEW_LEN ? c : c.substring(0, PREVIEW_LEN) + "…");
        } else {
            vo.setLastMessagePreview("");
        }
        return vo;
    }

    private List<PrivateMessageThreadListVO> toThreadVOList(List<PrivateMessageThread> threads, boolean forInheritorViewer) {
        if (forInheritorViewer) {
            return toThreadVOListForInheritor(threads);
        }
        List<PrivateMessageThreadListVO> list = new ArrayList<>();
        for (PrivateMessageThread t : threads) {
            PrivateMessageThreadListVO vo = baseThreadVO(t);
            vo.setThreadKind(kindOf(t));
            vo.setCounterpartyName(vo.getInheritorNickName());
            vo.setHasUnread(hasUnreadForAdmin(t));
            list.add(vo);
        }
        return list;
    }

    private boolean hasUnreadForInheritorOnThread(PrivateMessageThread t) {
        int k = kindOf(t);
        QueryWrapper<PrivateMessage> q = new QueryWrapper<>();
        q.eq("thread_id", t.getId());
        LocalDateTime since = t.getInheritorLastReadAt() != null ? t.getInheritorLastReadAt() : EPOCH;
        q.gt("create_time", since);
        if (k == PrivateMessageThread.KIND_PLATFORM) {
            q.eq("sender_role", PrivateMessage.ROLE_ADMIN);
        } else {
            q.eq("sender_role", PrivateMessage.ROLE_USER);
        }
        return privateMessageMapper.selectCount(q) > 0;
    }

    private boolean hasUnreadForFrontUserOnThread(PrivateMessageThread t) {
        QueryWrapper<PrivateMessage> q = new QueryWrapper<>();
        q.eq("thread_id", t.getId()).eq("sender_role", PrivateMessage.ROLE_INHERITOR);
        LocalDateTime since = t.getUserLastReadAt() != null ? t.getUserLastReadAt() : EPOCH;
        q.gt("create_time", since);
        return privateMessageMapper.selectCount(q) > 0;
    }

    private boolean hasUnreadForAdmin(PrivateMessageThread t) {
        QueryWrapper<PrivateMessage> q = new QueryWrapper<>();
        q.eq("thread_id", t.getId()).eq("sender_role", PrivateMessage.ROLE_INHERITOR);
        LocalDateTime since = t.getAdminLastReadAt() != null ? t.getAdminLastReadAt() : EPOCH;
        q.gt("create_time", since);
        return privateMessageMapper.selectCount(q) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PrivateMessageItemVO> listMessagesAndMarkReadAsInheritor(Integer threadId) {
        Inheritor inh = BackendAuthHelper.requireLoginInheritor();
        PrivateMessageThread t = getById(threadId);
        if (t == null || !t.getInheritorId().equals(inh.getId())) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "会话不存在");
        }
        List<PrivateMessageItemVO> msgs = loadMessages(threadId);
        t.setInheritorLastReadAt(LocalDateTime.now());
        updateById(t);
        return msgs;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PrivateMessageItemVO> listMessagesAndMarkReadAsAdmin(Integer threadId) {
        Admin admin = BackendAuthHelper.requireLoginAdmin();
        PrivateMessageThread t = getById(threadId);
        assertAdminCanAccessPlatformThread(admin, t);
        List<PrivateMessageItemVO> msgs = loadMessages(threadId);
        t.setAdminLastReadAt(LocalDateTime.now());
        updateById(t);
        return msgs;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteThreadAsAdmin(Integer threadId) {
        Admin admin = BackendAuthHelper.requireLoginAdmin();
        PrivateMessageThread t = getById(threadId);
        assertAdminCanAccessPlatformThread(admin, t);
        QueryWrapper<PrivateMessage> mq = new QueryWrapper<>();
        mq.eq("thread_id", threadId);
        privateMessageMapper.delete(mq);
        removeById(threadId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PrivateMessageItemVO> listMessagesAndMarkReadAsFrontUser(Integer threadId) {
        User u = BackendAuthHelper.tryLoginFrontUser();
        if (u == null) {
            throw new BusinessException(CodeEnum.AUTH_ERROR, "请先登录");
        }
        PrivateMessageThread t = getById(threadId);
        if (t == null || kindOf(t) != PrivateMessageThread.KIND_USER_INHERITOR
                || t.getFrontUserId() == null || !t.getFrontUserId().equals(u.getId())) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "会话不存在");
        }
        List<PrivateMessageItemVO> msgs = loadMessages(threadId);
        t.setUserLastReadAt(LocalDateTime.now());
        updateById(t);
        return msgs;
    }

    private List<PrivateMessageItemVO> loadMessages(Integer threadId) {
        QueryWrapper<PrivateMessage> q = new QueryWrapper<>();
        q.eq("thread_id", threadId).orderByAsc("create_time");
        List<PrivateMessage> rows = privateMessageMapper.selectList(q);
        List<PrivateMessageItemVO> list = new ArrayList<>();
        for (PrivateMessage m : rows) {
            PrivateMessageItemVO vo = new PrivateMessageItemVO();
            vo.setId(m.getId());
            vo.setThreadId(m.getThreadId());
            vo.setSenderRole(m.getSenderRole());
            vo.setContent(m.getContent());
            vo.setCreateTime(m.getCreateTime());
            vo.setSenderName(resolveSenderName(m));
            vo.setSenderAvatar(resolveSenderAvatar(m));
            list.add(vo);
        }
        return list;
    }

    private String resolveSenderAvatar(PrivateMessage m) {
        Integer role = m.getSenderRole();
        if (role != null && role == PrivateMessage.ROLE_INHERITOR) {
            Inheritor inh = inheritorMapper.selectById(m.getSenderId());
            return inh != null ? inh.getAvatar() : null;
        }
        if (role != null && role == PrivateMessage.ROLE_USER) {
            User usr = userMapper.selectById(m.getSenderId());
            return usr != null ? usr.getAvatar() : null;
        }
        Admin ad = adminMapper.selectById(m.getSenderId());
        return ad != null ? ad.getAvatar() : null;
    }

    private String resolveSenderName(PrivateMessage m) {
        Integer role = m.getSenderRole();
        if (role != null && role == PrivateMessage.ROLE_INHERITOR) {
            Inheritor inh = inheritorMapper.selectById(m.getSenderId());
            if (inh != null) {
                return CharSequenceUtil.blankToDefault(inh.getNickName(), inh.getUsername());
            }
            return "传承人";
        }
        if (role != null && role == PrivateMessage.ROLE_USER) {
            User usr = userMapper.selectById(m.getSenderId());
            if (usr != null) {
                return CharSequenceUtil.blankToDefault(usr.getNickName(), usr.getUsername());
            }
            return "用户";
        }
        Admin ad = adminMapper.selectById(m.getSenderId());
        if (ad != null) {
            return CharSequenceUtil.blankToDefault(ad.getNickName(), ad.getUsername());
        }
        return "管理员";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyAsInheritor(Integer threadId, String content) {
        Inheritor inh = BackendAuthHelper.requireLoginInheritor();
        PrivateMessageThread t = getById(threadId);
        if (t == null || !t.getInheritorId().equals(inh.getId())) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "会话不存在");
        }
        String body = CharSequenceUtil.trim(content);
        validateBody(body);
        LocalDateTime now = LocalDateTime.now();
        PrivateMessage m = new PrivateMessage();
        m.setThreadId(threadId);
        m.setSenderRole(PrivateMessage.ROLE_INHERITOR);
        m.setSenderId(inh.getId());
        m.setContent(body);
        m.setCreateTime(now);
        privateMessageMapper.insert(m);

        t.setUpdateTime(now);
        t.setInheritorLastReadAt(now);
        updateById(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyAsAdmin(Integer threadId, String content) {
        Admin admin = BackendAuthHelper.requireLoginAdmin();
        PrivateMessageThread t = getById(threadId);
        assertAdminCanAccessPlatformThread(admin, t);
        String body = CharSequenceUtil.trim(content);
        validateBody(body);
        LocalDateTime now = LocalDateTime.now();
        if (t.getPlatformTargetAdminId() == null) {
            PrivateMessageThread claimPatch = new PrivateMessageThread();
            claimPatch.setPlatformTargetAdminId(admin.getId());
            UpdateWrapper<PrivateMessageThread> claimUw = new UpdateWrapper<>();
            claimUw.eq("id", threadId).isNull("platform_target_admin_id");
            if (!update(claimPatch, claimUw)) {
                PrivateMessageThread fresh = getById(threadId);
                if (fresh == null || fresh.getPlatformTargetAdminId() == null
                        || !fresh.getPlatformTargetAdminId().equals(admin.getId())) {
                    throw new BusinessException(CodeEnum.PARAMS_ERROR, "该会话已被其他管理员认领，请刷新列表");
                }
                t = fresh;
            } else {
                t.setPlatformTargetAdminId(admin.getId());
            }
        }
        PrivateMessage m = new PrivateMessage();
        m.setThreadId(threadId);
        m.setSenderRole(PrivateMessage.ROLE_ADMIN);
        m.setSenderId(admin.getId());
        m.setContent(body);
        m.setCreateTime(now);
        privateMessageMapper.insert(m);

        t.setUpdateTime(now);
        t.setAdminLastReadAt(now);
        updateById(t);
    }

    /** 待认领（platform_target_admin_id 为空）或已指派给当前管理员 */
    private void assertAdminCanAccessPlatformThread(Admin admin, PrivateMessageThread t) {
        if (t == null || kindOf(t) != PrivateMessageThread.KIND_PLATFORM) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "会话不存在");
        }
        Integer tid = t.getPlatformTargetAdminId();
        if (tid != null && !tid.equals(admin.getId())) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "该会话由其他管理员负责");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyAsFrontUser(Integer threadId, String content) {
        User u = BackendAuthHelper.tryLoginFrontUser();
        if (u == null) {
            throw new BusinessException(CodeEnum.AUTH_ERROR, "请先登录");
        }
        PrivateMessageThread t = getById(threadId);
        if (t == null || kindOf(t) != PrivateMessageThread.KIND_USER_INHERITOR
                || t.getFrontUserId() == null || !t.getFrontUserId().equals(u.getId())) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "会话不存在");
        }
        String body = CharSequenceUtil.trim(content);
        validateBody(body);
        LocalDateTime now = LocalDateTime.now();
        PrivateMessage m = new PrivateMessage();
        m.setThreadId(threadId);
        m.setSenderRole(PrivateMessage.ROLE_USER);
        m.setSenderId(u.getId());
        m.setContent(body);
        m.setCreateTime(now);
        privateMessageMapper.insert(m);

        t.setUpdateTime(now);
        t.setUserLastReadAt(now);
        updateById(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrivateMessageThread sendToInheritor(Integer inheritorId, String content) {
        User u = BackendAuthHelper.tryLoginFrontUser();
        if (u == null) {
            throw new BusinessException(CodeEnum.AUTH_ERROR, "请先登录");
        }
        Inheritor inh = inheritorMapper.selectById(inheritorId);
        if (inh == null) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "传承人不存在");
        }
        if (inh.getStatus() == null || inh.getStatus() != 1) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "仅可向已通过认证的传承人发私信");
        }
        String body = CharSequenceUtil.trim(content);
        validateBody(body);

        QueryWrapper<PrivateMessageThread> qw = new QueryWrapper<>();
        qw.eq("thread_kind", PrivateMessageThread.KIND_USER_INHERITOR)
                .eq("front_user_id", u.getId())
                .eq("inheritor_id", inheritorId);
        PrivateMessageThread t = getOne(qw);

        LocalDateTime now = LocalDateTime.now();
        if (t == null) {
            t = new PrivateMessageThread();
            t.setInheritorId(inheritorId);
            t.setThreadKind(PrivateMessageThread.KIND_USER_INHERITOR);
            t.setFrontUserId(u.getId());
            t.setSubject(CharSequenceUtil.blankToDefault(inh.getNickName(), "私信"));
            t.setInheritorLastReadAt(null);
            t.setAdminLastReadAt(null);
            t.setUserLastReadAt(now);
            t.setCreateTime(now);
            t.setUpdateTime(now);
            save(t);
        }

        PrivateMessage m = new PrivateMessage();
        m.setThreadId(t.getId());
        m.setSenderRole(PrivateMessage.ROLE_USER);
        m.setSenderId(u.getId());
        m.setContent(body);
        m.setCreateTime(now);
        privateMessageMapper.insert(m);

        t.setUpdateTime(now);
        t.setUserLastReadAt(now);
        updateById(t);
        return getById(t.getId());
    }

    @Override
    public List<PmContactAdminVO> listAdminsForInheritorChat() {
        BackendAuthHelper.requireLoginInheritor();
        QueryWrapper<Admin> q = new QueryWrapper<>();
        q.eq("status", 1).orderByAsc("id");
        List<Admin> rows = adminMapper.selectList(q);
        List<PmContactAdminVO> list = new ArrayList<>();
        for (Admin a : rows) {
            PmContactAdminVO vo = new PmContactAdminVO();
            vo.setId(a.getId());
            vo.setNickName(a.getNickName());
            vo.setUsername(a.getUsername());
            vo.setAvatar(a.getAvatar());
            list.add(vo);
        }
        return list;
    }

    @Override
    public Page<FrontUserBriefVO> pageUsersForInheritorPicker(String keyword, long pageNum, long pageSize) {
        BackendAuthHelper.requireLoginInheritor();
        Page<User> pg = new Page<>(pageNum, pageSize);
        QueryWrapper<User> q = new QueryWrapper<>();
        q.eq("status", 1);
        if (CharSequenceUtil.isNotBlank(keyword)) {
            String k = keyword.trim();
            q.and(w -> w.like("nick_name", k).or().like("username", k));
        }
        q.orderByDesc("id");
        Page<User> res = userMapper.selectPage(pg, q);
        Page<FrontUserBriefVO> out = new Page<>(res.getCurrent(), res.getSize(), res.getTotal());
        out.setPages(res.getPages());
        List<FrontUserBriefVO> records = res.getRecords().stream().map(u -> {
            FrontUserBriefVO vo = new FrontUserBriefVO();
            vo.setId(u.getId());
            vo.setNickName(u.getNickName());
            vo.setUsername(u.getUsername());
            vo.setAvatar(u.getAvatar());
            return vo;
        }).collect(Collectors.toList());
        out.setRecords(records);
        return out;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrivateMessageThread sendToUserAsInheritor(Integer frontUserId, String content) {
        Inheritor inh = BackendAuthHelper.requireLoginInheritor();
        if (frontUserId == null) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "请选择访客用户");
        }
        User u = userMapper.selectById(frontUserId);
        if (u == null) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "用户不存在");
        }
        if (u.getStatus() == null || u.getStatus() != 1) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "该用户账号不可用");
        }
        String body = CharSequenceUtil.trim(content);
        validateBody(body);

        QueryWrapper<PrivateMessageThread> qw = new QueryWrapper<>();
        qw.eq("thread_kind", PrivateMessageThread.KIND_USER_INHERITOR)
                .eq("inheritor_id", inh.getId())
                .eq("front_user_id", frontUserId);
        PrivateMessageThread t = getOne(qw);

        LocalDateTime now = LocalDateTime.now();
        if (t == null) {
            t = new PrivateMessageThread();
            t.setInheritorId(inh.getId());
            t.setThreadKind(PrivateMessageThread.KIND_USER_INHERITOR);
            t.setFrontUserId(frontUserId);
            t.setSubject(CharSequenceUtil.blankToDefault(CharSequenceUtil.trim(u.getNickName()), u.getUsername()));
            t.setInheritorLastReadAt(now);
            t.setAdminLastReadAt(null);
            t.setUserLastReadAt(null);
            t.setCreateTime(now);
            t.setUpdateTime(now);
            save(t);
        }

        PrivateMessage m = new PrivateMessage();
        m.setThreadId(t.getId());
        m.setSenderRole(PrivateMessage.ROLE_INHERITOR);
        m.setSenderId(inh.getId());
        m.setContent(body);
        m.setCreateTime(now);
        privateMessageMapper.insert(m);

        t.setUpdateTime(now);
        t.setInheritorLastReadAt(now);
        updateById(t);
        return getById(t.getId());
    }

    private void validateBody(String body) {
        if (CharSequenceUtil.isBlank(body)) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "内容不能为空");
        }
        if (body.length() > 5000) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "内容过长");
        }
    }
}
