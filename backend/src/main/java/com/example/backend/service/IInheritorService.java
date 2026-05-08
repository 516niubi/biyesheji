package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.common.model.PageResult;
import com.example.backend.entity.Inheritor;
import com.example.backend.entity.request.inheritor.InheritorAdminUpdateRequest;
import com.example.backend.entity.request.inheritor.InheritorRegisterRequest;
import com.example.backend.entity.request.system.LoginRequest;
import com.example.backend.entity.vo.inheritor.InheritorLoginVO;
import com.example.backend.entity.vo.inheritor.InheritorPublicProfileVO;
import com.example.backend.entity.vo.inheritor.InheritorVO;

import java.util.List;

public interface IInheritorService extends IService<Inheritor> {

    void register(InheritorRegisterRequest request);

    InheritorLoginVO login(LoginRequest request);

    InheritorLoginVO getCurrentInfo();

    Boolean updateProfile(Inheritor body);

    Boolean updatePass(Integer userId, String oldPass, String newPass);

    PageResult<List<InheritorVO>> pageForAudit(Integer pageNum, Integer pageSize, String username, String phone, Integer status, String nickName);

    Boolean audit(Integer id, Integer status, String remark, Integer auditorId);

    Boolean adminUpdate(InheritorAdminUpdateRequest request);

    Boolean adminDelete(Integer id);

    Boolean adminBatchDelete(List<Integer> ids);

    /** 前台：已通过审核的传承人公开资料 */
    InheritorPublicProfileVO getPublicProfile(Integer id);

    /** 前台：传承人名录分页 */
    PageResult<List<InheritorPublicProfileVO>> publicPage(Integer pageNum, Integer pageSize, String keyword);
}
