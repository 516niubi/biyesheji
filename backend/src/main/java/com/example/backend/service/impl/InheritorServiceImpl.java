package com.example.backend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.constants.LoginConstant;
import com.example.backend.common.enums.CodeEnum;
import com.example.backend.common.model.PageResult;
import com.example.backend.entity.Inheritor;
import com.example.backend.entity.request.inheritor.InheritorAdminUpdateRequest;
import com.example.backend.entity.request.inheritor.InheritorRegisterRequest;
import com.example.backend.entity.request.system.LoginRequest;
import com.example.backend.entity.vo.inheritor.InheritorLoginVO;
import com.example.backend.entity.vo.inheritor.InheritorPublicProfileVO;
import com.example.backend.entity.vo.inheritor.InheritorVO;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.InheritorMapper;
import com.example.backend.service.ICaptchaService;
import com.example.backend.service.IInheritorService;
import com.example.backend.utils.BackendAuthHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InheritorServiceImpl extends ServiceImpl<InheritorMapper, Inheritor> implements IInheritorService {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_REJECTED = 2;

    private static final String LOGIN_ID_PREFIX = "inheritor:";

    @Resource
    private ICaptchaService captchaService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(InheritorRegisterRequest request) {
        captchaService.verifyAndConsume(request.getCaptchaKey(), request.getCaptcha());
        if (StrUtil.isBlank(request.getUsername()) || StrUtil.isBlank(request.getPassword())) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "账号密码不能为空");
        }
        if (StrUtil.isBlank(request.getPhone())) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "请填写手机号");
        }
        if (StrUtil.isBlank(request.getProfile())) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "请填写个人简介");
        }
        if (request.getCertificateUrls() == null || request.getCertificateUrls().isEmpty()) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "请上传至少一份认证材料");
        }

        QueryWrapper<Inheritor> uq = new QueryWrapper<>();
        uq.eq("username", request.getUsername());
        if (this.count(uq) > 0) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "该用户名已被使用");
        }

        Inheritor row = new Inheritor();
        row.setUsername(request.getUsername().trim());
        row.setPassword(request.getPassword());
        row.setNickName(StrUtil.blankToDefault(request.getNickName(), request.getUsername()));
        row.setPhone(request.getPhone().trim());
        row.setGender(request.getGender() != null ? request.getGender() : 2);
        row.setAge(request.getAge() != null ? request.getAge() : 18);
        row.setAvatar(StrUtil.blankToDefault(request.getAvatar(), null));
        row.setProfile(request.getProfile().trim());
        row.setCertificateUrls(JSONUtil.toJsonStr(request.getCertificateUrls()));
        row.setStatus(STATUS_PENDING);
        row.setCreateTime(new Date());
        row.setUpdateTime(new Date());
        this.save(row);
    }

    @Override
    public InheritorLoginVO login(LoginRequest request) {
        captchaService.verifyAndConsume(request.getCaptchaKey(), request.getCaptcha());
        if (!"inheritor".equals(request.getRole())) {
            throw new BusinessException(CodeEnum.AUTH_ERROR, "请使用传承人入口登录");
        }
        QueryWrapper<Inheritor> q = new QueryWrapper<>();
        q.eq("username", request.getUsername());
        Inheritor inheritor = this.getOne(q);
        if (ObjectUtil.isNull(inheritor)) {
            throw new BusinessException(CodeEnum.NULL_ERROR, "账号错误");
        }
        if (!request.getPassword().equals(inheritor.getPassword())) {
            throw new BusinessException(CodeEnum.NULL_ERROR, "密码错误");
        }
        if (inheritor.getStatus() == STATUS_PENDING) {
            throw new BusinessException(CodeEnum.NULL_ERROR, "账号审核中，通过后方可登录");
        }
        if (inheritor.getStatus() == STATUS_REJECTED) {
            throw new BusinessException(CodeEnum.NULL_ERROR, "注册未通过：" + StrUtil.blankToDefault(inheritor.getAuditRemark(), "请重新注册或联系管理员"));
        }

        StpUtil.login(LOGIN_ID_PREFIX + inheritor.getId());
        String token = StpUtil.getTokenValue();
        StpUtil.getSession().set(LoginConstant.USER_ID, inheritor.getId());
        StpUtil.getSession().set(LoginConstant.USER_KEY, inheritor);

        InheritorLoginVO vo = new InheritorLoginVO();
        BeanUtils.copyProperties(inheritor, vo);
        vo.setRole("inheritor");
        vo.setToken(token);
        return vo;
    }

    @Override
    public InheritorLoginVO getCurrentInfo() {
        Inheritor inh = BackendAuthHelper.requireLoginInheritor();
        inh = this.getById(inh.getId());
        InheritorLoginVO vo = new InheritorLoginVO();
        BeanUtils.copyProperties(inh, vo);
        vo.setRole("inheritor");
        return vo;
    }

    @Override
    public Boolean updateProfile(Inheritor body) {
        Inheritor session = BackendAuthHelper.requireLoginInheritor();
        Inheritor db = this.getById(session.getId());
        if (db == null) {
            return false;
        }
        if (StrUtil.isNotBlank(body.getNickName())) {
            db.setNickName(body.getNickName());
        }
        if (body.getGender() != null) {
            db.setGender(body.getGender());
        }
        if (body.getAge() != null) {
            db.setAge(body.getAge());
        }
        if (StrUtil.isNotBlank(body.getAvatar())) {
            db.setAvatar(body.getAvatar());
        }
        if (StrUtil.isNotBlank(body.getProfile())) {
            db.setProfile(body.getProfile());
        }
        db.setUpdateTime(new Date());
        return this.updateById(db);
    }

    @Override
    public Boolean updatePass(Integer userId, String oldPass, String newPass) {
        Inheritor session = BackendAuthHelper.requireLoginInheritor();
        if (!session.getId().equals(userId)) {
            throw new BusinessException(CodeEnum.AUTH_ERROR, "仅能修改本人密码");
        }
        Inheritor db = this.getById(userId);
        if (!oldPass.equals(db.getPassword())) {
            throw new BusinessException(CodeEnum.NULL_ERROR, "旧密码错误");
        }
        if (newPass.equals(db.getPassword())) {
            throw new BusinessException(CodeEnum.NULL_ERROR, "与原密码相同");
        }
        db.setPassword(newPass);
        db.setUpdateTime(new Date());
        return this.updateById(db);
    }

    @Override
    public PageResult<List<InheritorVO>> pageForAudit(Integer pageNum, Integer pageSize, String username, String phone, Integer status, String nickName) {
        BackendAuthHelper.requireLoginAdmin();
        QueryWrapper<Inheritor> q = new QueryWrapper<>();
        if (StrUtil.isNotBlank(username)) {
            q.like("username", username);
        }
        if (StrUtil.isNotBlank(phone)) {
            q.like("phone", phone);
        }
        if (StrUtil.isNotBlank(nickName)) {
            q.like("nick_name", nickName);
        }
        if (status != null) {
            q.eq("status", status);
        }
        q.orderByDesc("id");
        Page<Inheritor> page = this.page(new Page<>(pageNum, pageSize), q);
        PageResult<List<InheritorVO>> pr = new PageResult<>();
        pr.setCurrent(pageNum);
        pr.setSize(pageSize);
        pr.setTotal(page.getTotal());
        pr.setRecords(page.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return pr;
    }

    @Override
    public Boolean audit(Integer id, Integer status, String remark, Integer auditorId) {
        Inheritor row = this.getById(id);
        if (row == null) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "记录不存在");
        }
        if (status != STATUS_APPROVED && status != STATUS_REJECTED) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "审核状态无效");
        }
        row.setStatus(status);
        row.setAuditRemark(remark);
        row.setAuditorId(auditorId);
        row.setAuditTime(new Date());
        row.setUpdateTime(new Date());
        return this.updateById(row);
    }

    @Override
    public Boolean adminUpdate(InheritorAdminUpdateRequest req) {
        BackendAuthHelper.requireLoginAdmin();
        if (req.getId() == null) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "id不能为空");
        }
        Inheritor row = this.getById(req.getId());
        if (row == null) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "记录不存在");
        }
        if (row.getStatus() != STATUS_APPROVED) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "仅可编辑已审核通过的传承人");
        }
        if (StrUtil.isNotBlank(req.getNickName())) {
            row.setNickName(req.getNickName().trim());
        }
        if (StrUtil.isNotBlank(req.getPhone())) {
            row.setPhone(req.getPhone().trim());
        }
        if (req.getGender() != null) {
            row.setGender(req.getGender());
        }
        if (req.getAge() != null) {
            row.setAge(req.getAge());
        }
        if (req.getAvatar() != null) {
            row.setAvatar(StrUtil.blankToDefault(req.getAvatar(), null));
        }
        if (req.getProfile() != null) {
            row.setProfile(req.getProfile().trim());
        }
        if (req.getCertificateUrls() != null) {
            if (req.getCertificateUrls().isEmpty()) {
                throw new BusinessException(CodeEnum.PARAMS_ERROR, "至少保留一份认证材料");
            }
            row.setCertificateUrls(JSONUtil.toJsonStr(req.getCertificateUrls()));
        }
        row.setUpdateTime(new Date());
        return this.updateById(row);
    }

    @Override
    public Boolean adminDelete(Integer id) {
        BackendAuthHelper.requireLoginAdmin();
        Inheritor row = this.getById(id);
        if (row == null) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "记录不存在");
        }
        if (row.getStatus() != STATUS_APPROVED) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "仅可删除已审核通过的传承人");
        }
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean adminBatchDelete(List<Integer> ids) {
        BackendAuthHelper.requireLoginAdmin();
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "请选择要删除的记录");
        }
        List<Inheritor> list = this.listByIds(ids);
        if (list.size() != ids.size()) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "部分记录不存在");
        }
        for (Inheritor r : list) {
            if (r.getStatus() != STATUS_APPROVED) {
                throw new BusinessException(CodeEnum.PARAMS_ERROR, "仅可删除已审核通过的传承人");
            }
        }
        return this.removeBatchByIds(ids);
    }

    @Override
    public InheritorPublicProfileVO getPublicProfile(Integer id) {
        Inheritor row = this.getById(id);
        if (row == null || row.getStatus() != STATUS_APPROVED) {
            throw new BusinessException(CodeEnum.PARAMS_ERROR, "传承人不存在或未通过审核");
        }
        return toPublicVo(row);
    }

    @Override
    public PageResult<List<InheritorPublicProfileVO>> publicPage(Integer pageNum, Integer pageSize, String keyword) {
        QueryWrapper<Inheritor> q = new QueryWrapper<>();
        q.eq("status", STATUS_APPROVED);
        if (StrUtil.isNotBlank(keyword)) {
            q.and(w -> w.like("nick_name", keyword).or().like("username", keyword));
        }
        q.orderByDesc("id");
        Page<Inheritor> page = this.page(new Page<>(pageNum, pageSize), q);
        PageResult<List<InheritorPublicProfileVO>> pr = new PageResult<>();
        pr.setCurrent(pageNum);
        pr.setSize(pageSize);
        pr.setTotal(page.getTotal());
        pr.setRecords(page.getRecords().stream().map(this::toPublicVo).collect(Collectors.toList()));
        return pr;
    }

    private InheritorPublicProfileVO toPublicVo(Inheritor e) {
        InheritorPublicProfileVO vo = new InheritorPublicProfileVO();
        vo.setId(e.getId());
        vo.setNickName(e.getNickName());
        vo.setAvatar(e.getAvatar());
        vo.setProfile(e.getProfile());
        return vo;
    }

    private InheritorVO toVo(Inheritor e) {
        InheritorVO v = new InheritorVO();
        BeanUtils.copyProperties(e, v);
        return v;
    }
}
