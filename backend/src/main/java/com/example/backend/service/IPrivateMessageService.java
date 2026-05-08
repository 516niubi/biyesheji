package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.PrivateMessageThread;
import com.example.backend.entity.vo.privatemessage.FrontUserBriefVO;
import com.example.backend.entity.vo.privatemessage.PmContactAdminVO;
import com.example.backend.entity.vo.privatemessage.PrivateMessageItemVO;
import com.example.backend.entity.vo.privatemessage.PrivateMessageThreadListVO;

import java.util.List;

public interface IPrivateMessageService extends IService<PrivateMessageThread> {

    PrivateMessageThread createThread(String subject, String content, Integer platformTargetAdminId);

    Page<PrivateMessageThreadListVO> pageThreadsForInheritor(long pageNum, long pageSize);

    Page<PrivateMessageThreadListVO> pageThreadsForAdmin(long pageNum, long pageSize, String inheritorNickLike);

    Page<PrivateMessageThreadListVO> pageThreadsForFrontUser(long pageNum, long pageSize);

    List<PrivateMessageItemVO> listMessagesAndMarkReadAsInheritor(Integer threadId);

    List<PrivateMessageItemVO> listMessagesAndMarkReadAsAdmin(Integer threadId);

    /** 管理员删除平台私信会话及其全部消息 */
    void deleteThreadAsAdmin(Integer threadId);

    List<PrivateMessageItemVO> listMessagesAndMarkReadAsFrontUser(Integer threadId);

    void replyAsInheritor(Integer threadId, String content);

    void replyAsAdmin(Integer threadId, String content);

    void replyAsFrontUser(Integer threadId, String content);

    /** 用户向传承人发私信（无会话则创建 kind=2） */
    PrivateMessageThread sendToInheritor(Integer inheritorId, String content);

    /** 传承人端：可选管理员列表（发起平台会话时展示） */
    List<PmContactAdminVO> listAdminsForInheritorChat();

    /** 传承人端：按昵称/账号搜索正常状态前台用户 */
    Page<FrontUserBriefVO> pageUsersForInheritorPicker(String keyword, long pageNum, long pageSize);

    /**
     * 传承人向指定前台用户发起或续写 kind=2 会话（首条或追加均由传承人发送）
     */
    PrivateMessageThread sendToUserAsInheritor(Integer frontUserId, String content);
}
