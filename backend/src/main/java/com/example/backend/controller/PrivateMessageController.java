package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.entity.PrivateMessageThread;
import com.example.backend.entity.request.privatemessage.PmCreateThreadRequest;
import com.example.backend.entity.request.privatemessage.PmInheritorToUserRequest;
import com.example.backend.entity.request.privatemessage.PmReplyRequest;
import com.example.backend.entity.request.privatemessage.PmSendToInheritorRequest;
import com.example.backend.entity.vo.privatemessage.FrontUserBriefVO;
import com.example.backend.entity.vo.privatemessage.PmContactAdminVO;
import com.example.backend.entity.vo.privatemessage.PrivateMessageItemVO;
import com.example.backend.entity.vo.privatemessage.PrivateMessageThreadListVO;
import com.example.backend.service.IPrivateMessageService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/privateMessage")
public class PrivateMessageController {

    @Resource
    private IPrivateMessageService privateMessageService;

    // ---------- 传承人 ----------

    @PostMapping("/inheritor/thread")
    @ApiOperation("新建私信会话并发首条消息")
    public BaseResponse<PrivateMessageThread> createThread(@RequestBody PmCreateThreadRequest req) {
        PrivateMessageThread t = privateMessageService.createThread(
                req.getSubject(), req.getContent(), req.getPlatformTargetAdminId());
        return Result.success(t);
    }

    @GetMapping("/inheritor/threads")
    public BaseResponse<Page<PrivateMessageThreadListVO>> inheritorThreads(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(privateMessageService.pageThreadsForInheritor(pageNum, pageSize));
    }

    @GetMapping("/inheritor/thread/{threadId}/messages")
    public BaseResponse<List<PrivateMessageItemVO>> inheritorMessages(@PathVariable Integer threadId) {
        return Result.success(privateMessageService.listMessagesAndMarkReadAsInheritor(threadId));
    }

    @PostMapping("/inheritor/reply")
    public BaseResponse<Boolean> inheritorReply(@RequestBody PmReplyRequest req) {
        privateMessageService.replyAsInheritor(req.getThreadId(), req.getContent());
        return Result.success(true);
    }

    @GetMapping("/inheritor/adminsForChat")
    @ApiOperation("可选平台管理员列表（发起平台私信）")
    public BaseResponse<List<PmContactAdminVO>> inheritorAdminsForChat() {
        return Result.success(privateMessageService.listAdminsForInheritorChat());
    }

    @GetMapping("/inheritor/searchUsers")
    @ApiOperation("搜索前台用户（发起访客私信）")
    public BaseResponse<Page<FrontUserBriefVO>> inheritorSearchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "15") long pageSize) {
        return Result.success(privateMessageService.pageUsersForInheritorPicker(keyword, pageNum, pageSize));
    }

    @PostMapping("/inheritor/threadWithUser")
    @ApiOperation("向指定访客发起或续写私信会话")
    public BaseResponse<PrivateMessageThread> inheritorThreadWithUser(@RequestBody PmInheritorToUserRequest req) {
        PrivateMessageThread t = privateMessageService.sendToUserAsInheritor(req.getFrontUserId(), req.getContent());
        return Result.success(t);
    }

    // ---------- 管理员 ----------

    @GetMapping("/admin/threads")
    public BaseResponse<Page<PrivateMessageThreadListVO>> adminThreads(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String inheritorNick) {
        return Result.success(privateMessageService.pageThreadsForAdmin(pageNum, pageSize, inheritorNick));
    }

    @GetMapping("/admin/thread/{threadId}/messages")
    public BaseResponse<List<PrivateMessageItemVO>> adminMessages(@PathVariable Integer threadId) {
        return Result.success(privateMessageService.listMessagesAndMarkReadAsAdmin(threadId));
    }

    @DeleteMapping("/admin/thread/{threadId}")
    @ApiOperation("删除平台私信会话及全部消息")
    public BaseResponse<Boolean> adminDeleteThread(@PathVariable Integer threadId) {
        privateMessageService.deleteThreadAsAdmin(threadId);
        return Result.success(true);
    }

    @PostMapping("/admin/reply")
    public BaseResponse<Boolean> adminReply(@RequestBody PmReplyRequest req) {
        privateMessageService.replyAsAdmin(req.getThreadId(), req.getContent());
        return Result.success(true);
    }

    // ---------- 前台用户（与传承人私信，共用消息表）----------

    @GetMapping("/front/threads")
    public BaseResponse<Page<PrivateMessageThreadListVO>> frontThreads(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize) {
        return Result.success(privateMessageService.pageThreadsForFrontUser(pageNum, pageSize));
    }

    @GetMapping("/front/thread/{threadId}/messages")
    public BaseResponse<List<PrivateMessageItemVO>> frontMessages(@PathVariable Integer threadId) {
        return Result.success(privateMessageService.listMessagesAndMarkReadAsFrontUser(threadId));
    }

    @PostMapping("/front/reply")
    public BaseResponse<Boolean> frontReply(@RequestBody PmReplyRequest req) {
        privateMessageService.replyAsFrontUser(req.getThreadId(), req.getContent());
        return Result.success(true);
    }

    @PostMapping("/front/sendToInheritor")
    @ApiOperation("向传承人发私信（无会话则自动创建）")
    public BaseResponse<PrivateMessageThread> frontSendToInheritor(@RequestBody PmSendToInheritorRequest req) {
        PrivateMessageThread t = privateMessageService.sendToInheritor(req.getInheritorId(), req.getContent());
        return Result.success(t);
    }
}
