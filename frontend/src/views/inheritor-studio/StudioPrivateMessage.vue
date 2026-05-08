<script setup lang="ts">
import http from "@/utils/http";
import { Plus, Promotion, Search } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { getImageUrl } from "@/utils/system";

const avatarSrc = (path: string | undefined | null) => {
  const u = getImageUrl(path || "");
  return u || undefined;
};

const params = ref({ pageNum: 1, pageSize: 20 });
const threads = ref<any[]>([]);
const totalThreads = ref(0);
const loadingList = ref(false);
const loadingMsg = ref(false);
const activeThreadId = ref<number | null>(null);
const messages = ref<any[]>([]);
const replyText = ref("");
const sending = ref(false);
const msgScrollRef = ref<HTMLElement | null>(null);

const newVisible = ref(false);
const newContent = ref("");
/** 新建：platform=联系平台管理员；user=指定访客 */
const recipientMode = ref<"platform" | "user">("platform");
const adminList = ref<any[]>([]);
const adminLoading = ref(false);
const pickedAdminId = ref<number | null>(null);
const userKeyword = ref("");
const userList = ref<any[]>([]);
const usersLoading = ref(false);
const pickedUserId = ref<number | null>(null);

const activeThread = computed(() => threads.value.find((t) => t.id === activeThreadId.value));

const chatHeadTitle = computed(() => {
  const t = activeThread.value;
  if (!t) return "";
  return t.counterpartyName || t.subject || "会话";
});

const chatHeadSub = computed(() => {
  const t = activeThread.value;
  if (!t) return "";
  return t.threadKind === 2 ? "与访客对话" : "与平台管理员沟通";
});

const threadListFallbackLetter = (t: any) => {
  const name = t.counterpartyName || "";
  return name.slice(0, 1) || "私";
};

const fetchThreads = async () => {
  loadingList.value = true;
  try {
    const res = await http.get(
      `/privateMessage/inheritor/threads?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}`
    );
    if (res.code === 200) {
      threads.value = res.data.records || [];
      totalThreads.value = res.data.total ?? 0;
    }
  } finally {
    loadingList.value = false;
  }
};

const fetchMessages = async (threadId: number) => {
  loadingMsg.value = true;
  try {
    const res = await http.get(`/privateMessage/inheritor/thread/${threadId}/messages`);
    if (res.code === 200) {
      messages.value = res.data || [];
      await nextTick();
      scrollToBottom();
      await fetchThreads();
    }
  } finally {
    loadingMsg.value = false;
  }
};

const scrollToBottom = () => {
  const el = msgScrollRef.value;
  if (el) el.scrollTop = el.scrollHeight;
};

const selectThread = (row: any) => {
  activeThreadId.value = row.id;
  fetchMessages(row.id);
};

const sendReply = async () => {
  const t = replyText.value.trim();
  if (!t || activeThreadId.value == null) return;
  sending.value = true;
  try {
    const res = await http.post("/privateMessage/inheritor/reply", {
      threadId: activeThreadId.value,
      content: t,
    });
    if (res.code === 200) {
      replyText.value = "";
      ElMessage.success("已发送");
      await fetchMessages(activeThreadId.value);
    }
  } catch {
    ElMessage.error("发送失败");
  } finally {
    sending.value = false;
  }
};

const loadAdminsForDialog = async () => {
  adminLoading.value = true;
  try {
    const res = await http.get("/privateMessage/inheritor/adminsForChat");
    if (res.code === 200) {
      adminList.value = Array.isArray(res.data) ? res.data : [];
      if (adminList.value.length) {
        pickedAdminId.value = adminList.value[0].id;
      } else {
        pickedAdminId.value = null;
      }
    }
  } finally {
    adminLoading.value = false;
  }
};

const searchUsersForDialog = async () => {
  usersLoading.value = true;
  try {
    const kw = encodeURIComponent(userKeyword.value.trim());
    const res = await http.get(
      `/privateMessage/inheritor/searchUsers?keyword=${kw}&pageNum=1&pageSize=20`
    );
    if (res.code === 200) {
      userList.value = res.data.records || [];
    }
  } finally {
    usersLoading.value = false;
  }
};

const openNew = async () => {
  recipientMode.value = "platform";
  newContent.value = "";
  pickedUserId.value = null;
  userKeyword.value = "";
  userList.value = [];
  newVisible.value = true;
  await loadAdminsForDialog();
};

const submitStartChat = async () => {
  const c = newContent.value.trim();
  if (!c) {
    ElMessage.warning("请输入留言内容");
    return;
  }
  try {
    if (recipientMode.value === "platform") {
      let subject = "私信";
      if (pickedAdminId.value != null) {
        const a = adminList.value.find((x) => x.id === pickedAdminId.value);
        if (a) {
          const nick = (a.nickName || a.username || "管理员").trim();
          subject = `私信（${nick}）`;
          if (subject.length > 200) subject = subject.slice(0, 200);
        }
      }
      const res = await http.post("/privateMessage/inheritor/thread", {
        subject,
        content: c,
        platformTargetAdminId: pickedAdminId.value ?? undefined,
      });
      if (res.code === 200) {
        ElMessage.success("已发送");
        newVisible.value = false;
        params.value.pageNum = 1;
        await fetchThreads();
        if (res.data?.id) {
          activeThreadId.value = res.data.id;
          await fetchMessages(res.data.id);
        }
      }
    } else {
      if (pickedUserId.value == null) {
        ElMessage.warning("请先搜索并选择一位访客用户");
        return;
      }
      const res = await http.post("/privateMessage/inheritor/threadWithUser", {
        frontUserId: pickedUserId.value,
        content: c,
      });
      if (res.code === 200) {
        ElMessage.success("已发送");
        newVisible.value = false;
        params.value.pageNum = 1;
        await fetchThreads();
        if (res.data?.id) {
          activeThreadId.value = res.data.id;
          await fetchMessages(res.data.id);
        }
      }
    }
  } catch {
    ElMessage.error("发送失败");
  }
};

watch(activeThreadId, () => {
  replyText.value = "";
});

onMounted(() => fetchThreads());
</script>

<template>
  <div class="pm-studio">
    <p class="lead">
      左侧可查看与<strong>平台管理员</strong>、<strong>访客用户</strong>的会话；点击「新建私信」可选择对象并发送首条消息，有新回复时列表会标注<strong>未读</strong>。
    </p>

    <div class="pm-layout">
      <aside class="pm-aside">
        <div class="aside-head">
          <span>我的会话</span>
          <el-button type="primary" size="small" round :icon="Plus" @click="openNew">新建私信</el-button>
        </div>
        <div v-loading="loadingList" class="thread-list">
          <button
            v-for="t in threads"
            :key="t.id"
            type="button"
            class="thread-item"
            :class="{ active: activeThreadId === t.id, unread: t.hasUnread }"
            @click="selectThread(t)"
          >
            <div class="thread-item-inner">
              <el-avatar class="thread-ava" :size="44" :src="avatarSrc(t.counterpartyAvatar)">
                {{ threadListFallbackLetter(t) }}
              </el-avatar>
              <div class="thread-body">
                <div class="thread-title-row">
                  <span class="thread-sub">{{ t.counterpartyName || t.subject || "会话" }}</span>
                  <div class="thread-tags">
                    <el-tag v-if="t.threadKind === 2" size="small" type="success" effect="plain">访客</el-tag>
                    <el-tag v-else size="small" type="info" effect="plain">平台</el-tag>
                    <el-badge v-if="t.hasUnread" is-dot class="dot" />
                  </div>
                </div>
                <div class="thread-preview">{{ t.lastMessagePreview || "暂无内容" }}</div>
                <div class="thread-time">{{ t.updateTime }}</div>
              </div>
            </div>
          </button>
          <el-empty v-if="!loadingList && !threads.length" description="暂无会话，点击新建私信" />
        </div>
        <el-pagination
          v-if="totalThreads > params.pageSize"
          small
          layout="prev, pager, next"
          :total="totalThreads"
          :page-size="params.pageSize"
          v-model:current-page="params.pageNum"
          @current-change="fetchThreads"
          class="aside-page"
        />
      </aside>

      <section class="pm-chat">
        <template v-if="activeThreadId">
          <header class="chat-head">
            <el-avatar class="chat-head-ava" :size="48" :src="avatarSrc(activeThread?.counterpartyAvatar)">
              {{ threadListFallbackLetter(activeThread || {}) }}
            </el-avatar>
            <div class="chat-head-text">
              <h3>{{ chatHeadTitle }}</h3>
              <span class="muted">{{ chatHeadSub }}</span>
            </div>
          </header>
          <div ref="msgScrollRef" v-loading="loadingMsg" class="msg-scroll">
            <div
              v-for="m in messages"
              :key="m.id"
              class="msg-row"
              :class="m.senderRole === 0 ? 'msg-row--admin' : 'msg-row--inh'"
            >
              <el-avatar class="msg-avatar" :size="40" :src="avatarSrc(m.senderAvatar)">
                {{ (m.senderName || "?").slice(0, 1) }}
              </el-avatar>
              <div class="msg-stack">
                <div class="msg-head">
                  <strong class="msg-name">{{ m.senderName }}</strong>
                  <el-tag v-if="m.senderRole === 0" size="small" type="warning" effect="plain">我</el-tag>
                  <el-tag v-else-if="m.senderRole === 1" size="small" type="danger" effect="plain">管理员</el-tag>
                  <el-tag v-else size="small" type="success" effect="plain">访客</el-tag>
                  <span class="msg-time">{{ m.createTime }}</span>
                </div>
                <div class="msg-bubble">{{ m.content }}</div>
              </div>
            </div>
          </div>
          <footer class="compose">
            <el-input
              v-model="replyText"
              type="textarea"
              :rows="3"
              maxlength="5000"
              show-word-limit
              placeholder="输入回复，Enter 发送（Shift+Enter 换行）"
              @keydown.enter.exact.prevent="sendReply"
            />
            <el-button type="primary" :loading="sending" :icon="Promotion" @click="sendReply">
              发送
            </el-button>
          </footer>
        </template>
        <div v-else class="chat-empty">
          <p>请从左侧选择会话，或新建一条私信</p>
        </div>
      </section>
    </div>

    <el-dialog v-model="newVisible" title="发起私信" width="580px" destroy-on-close append-to-body>
      <el-radio-group v-model="recipientMode" class="recipient-mode-row">
        <el-radio-button label="platform">平台管理员</el-radio-button>
        <el-radio-button label="user">访客用户</el-radio-button>
      </el-radio-group>

      <template v-if="recipientMode === 'platform'">
        <p class="dlg-hint">选择对接人（写入会话主题，便于识别；任意在线管理员均可查看并回复）。</p>
        <div v-loading="adminLoading" class="pick-scroll">
          <el-radio-group v-model="pickedAdminId" class="pick-radios">
            <div v-for="a in adminList" :key="a.id" class="pick-row">
              <el-radio :label="a.id" class="pick-radio">
                <el-avatar :size="36" :src="avatarSrc(a.avatar)" />
                <span class="pick-name">{{ a.nickName || a.username }}</span>
              </el-radio>
            </div>
          </el-radio-group>
          <el-empty v-if="!adminLoading && !adminList.length" description="暂无管理员账号，仍可发送平台私信（主题默认为「私信」）" />
        </div>
      </template>

      <template v-else>
        <p class="dlg-hint">按昵称或登录账号搜索前台用户，选中后发送首条消息即可开始对话。</p>
        <div class="user-search-row">
          <el-input
            v-model="userKeyword"
            clearable
            placeholder="昵称或账号"
            @keyup.enter="searchUsersForDialog"
          />
          <el-button type="primary" :icon="Search" @click="searchUsersForDialog">搜索</el-button>
        </div>
        <div v-loading="usersLoading" class="pick-scroll">
          <el-radio-group v-model="pickedUserId" class="pick-radios">
            <div v-for="row in userList" :key="row.id" class="pick-row">
              <el-radio :label="row.id" class="pick-radio">
                <el-avatar :size="36" :src="avatarSrc(row.avatar)" />
                <div class="pick-meta">
                  <span class="pick-name">{{ row.nickName || row.username }}</span>
                  <span class="pick-sub">{{ row.username }}</span>
                </div>
              </el-radio>
            </div>
          </el-radio-group>
          <el-empty
            v-if="!usersLoading && userKeyword.trim() && !userList.length"
            description="未找到用户，请更换关键词"
          />
        </div>
      </template>

      <div class="dlg-field">
        <span class="dlg-label"><span class="req">*</span> 留言内容</span>
        <el-input v-model="newContent" type="textarea" :rows="5" maxlength="5000" show-word-limit />
      </div>

      <template #footer>
        <el-button @click="newVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStartChat">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.pm-studio {
  .lead {
    margin: 0 0 16px;
    font-size: 13px;
    line-height: 1.65;
    color: #5c4a45;

    strong {
      color: #a82c28;
    }
  }
}

.pm-layout {
  display: grid;
  grid-template-columns: minmax(260px, 320px) 1fr;
  gap: 16px;
  min-height: 480px;
}

.pm-aside {
  border: 1px solid rgba(92, 38, 36, 0.12);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.aside-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-bottom: 1px solid rgba(92, 38, 36, 0.08);
  font-weight: 600;
  color: #3d2f2a;
}

.thread-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  max-height: 520px;
}

.thread-item {
  width: 100%;
  text-align: left;
  border: 1px solid transparent;
  border-radius: 12px;
  padding: 10px 12px;
  margin-bottom: 8px;
  background: rgba(255, 251, 248, 0.9);
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;

  &:hover {
    border-color: rgba(168, 44, 40, 0.25);
  }

  &.active {
    border-color: rgba(168, 44, 40, 0.45);
    box-shadow: 0 6px 18px rgba(92, 38, 36, 0.08);
    background: #fff;
  }

  &.unread .thread-sub {
    font-weight: 700;
  }
}

.thread-item-inner {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.thread-ava {
  flex-shrink: 0;
  margin-top: 2px;
}

.thread-body {
  flex: 1;
  min-width: 0;
}

.thread-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
}

.thread-tags {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.thread-sub {
  font-size: 14px;
  color: #2d2420;
  flex: 1;
  min-width: 0;
  word-break: break-word;
}

.thread-preview {
  font-size: 12px;
  color: #8b7d78;
  margin-top: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.thread-time {
  font-size: 11px;
  color: #a89892;
  margin-top: 6px;
}

.aside-page {
  padding: 8px;
  justify-content: center;
}

.pm-chat {
  border: 1px solid rgba(92, 38, 36, 0.12);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  flex-direction: column;
  min-height: 480px;
}

.chat-head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  border-bottom: 1px solid rgba(92, 38, 36, 0.08);

  .chat-head-ava {
    flex-shrink: 0;
  }

  .chat-head-text {
    min-width: 0;
  }

  h3 {
    margin: 0 0 4px;
    font-size: 17px;
    color: #2d2420;
  }

  .muted {
    font-size: 12px;
    color: #8b7d78;
  }
}

.msg-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 12px 14px;
  max-height: 420px;
  background: rgba(245, 240, 236, 0.9);
}

.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 14px;
  max-width: min(100%, 720px);
}

.msg-row--inh {
  flex-direction: row;
  margin-right: auto;
}

.msg-row--admin {
  flex-direction: row-reverse;
  margin-left: auto;
}

.msg-avatar {
  flex-shrink: 0;
}

.msg-stack {
  flex: 1;
  min-width: 0;
}

.msg-row--inh .msg-stack {
  text-align: left;
}

.msg-row--admin .msg-stack {
  text-align: right;
}

.msg-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px 10px;
  margin-bottom: 6px;
  font-size: 12px;
}

.msg-row--admin .msg-head {
  justify-content: flex-end;
}

.msg-name {
  color: #3d2f2a;
}

.msg-time {
  color: #8b7d78;
  font-size: 12px;
}

.msg-bubble {
  display: inline-block;
  max-width: 100%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
  text-align: left;
  box-sizing: border-box;
}

.msg-row--inh .msg-bubble {
  background: #f6f7f9;
  border: 1px solid rgba(0, 0, 0, 0.06);
  color: #3d2f2a;
}

.msg-row--admin .msg-bubble {
  background: linear-gradient(135deg, #fff4f0 0%, #ffe8e0 100%);
  border: 1px solid rgba(168, 44, 40, 0.22);
  color: #3d2f2a;
}

.compose {
  padding: 12px 16px 16px;
  border-top: 1px solid rgba(92, 38, 36, 0.08);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chat-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9a8a84;
  font-size: 14px;
}

@media (max-width: 900px) {
  .pm-layout {
    grid-template-columns: 1fr;
  }

  .pm-aside {
    max-height: 280px;
  }
}

.recipient-mode-row {
  width: 100%;
  margin-bottom: 12px;
}

.dlg-hint {
  margin: 0 0 10px;
  font-size: 12px;
  line-height: 1.55;
  color: #8b7d78;
}

.pick-scroll {
  max-height: 220px;
  overflow-y: auto;
  margin-bottom: 4px;
}

.pick-radios {
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

.pick-row {
  padding: 4px 0;
}

.pick-radio {
  display: flex;
  align-items: center;
  gap: 10px;
  height: auto;
  margin-right: 0;
  padding: 8px 10px;
  border-radius: 10px;
  width: 100%;

  &:hover {
    background: rgba(197, 61, 46, 0.06);
  }
}

.pick-name {
  font-weight: 600;
  font-size: 14px;
  color: #2d2420;
}

.pick-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.pick-sub {
  font-size: 12px;
  color: #8b7d78;
}

.user-search-row {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.user-search-row .el-input {
  flex: 1;
}

.dlg-field {
  margin-top: 14px;
}

.dlg-label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  color: #3d2f2a;
}

.req {
  color: var(--el-color-danger);
}
</style>
