package com.example.backend.utils;

import cn.hutool.core.util.StrUtil;
import com.example.backend.entity.Inheritor;
import com.example.backend.service.IInheritorService;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 将 creatorId（传承人主键）解析为前台展示的发布人名称与头像。
 */
@Component
public class PublisherNameResolver {

    @Resource
    private IInheritorService inheritorService;

    @Data
    public static class PublisherView {
        private String name;
        /** 头像相对路径，与传承人表 avatar 一致 */
        private String avatar;
    }

    /**
     * 一次查询传承人，填充名称与头像。
     */
    public PublisherView resolveView(Integer creatorId) {
        PublisherView v = new PublisherView();
        if (creatorId == null) {
            v.setName("平台");
            v.setAvatar(null);
            return v;
        }
        Inheritor inh = inheritorService.getById(creatorId);
        if (inh == null) {
            v.setName("未知");
            v.setAvatar(null);
            return v;
        }
        if (StrUtil.isNotBlank(inh.getNickName())) {
            v.setName(inh.getNickName());
        } else if (StrUtil.isNotBlank(inh.getUsername())) {
            v.setName(inh.getUsername());
        } else {
            v.setName("传承人");
        }
        v.setAvatar(inh.getAvatar());
        return v;
    }

    public String resolve(Integer creatorId) {
        return resolveView(creatorId).getName();
    }
}
