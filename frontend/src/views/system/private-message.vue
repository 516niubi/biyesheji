<script setup lang="ts">
import http from "../../utils/http";
import { Promotion, Search, Refresh } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { getImageUrl } from "../../utils/system";

const avatarSrc = (path: string | undefined | null) => {
  const u = getImageUrl(path || "");
  return u || undefined;
};

const searchNick = ref("");
const params = ref({ pageNum: 1, pageSize: 12 });
const threads = ref<any[]>([]);
const total = ref(0);
const loadingList = ref(false);
const loadingMsg = ref(false);
const activeThreadId = ref<number | null>(null);
const messages = ref<any[]>([]);
const replyText = ref("");
const sending = ref(false);
const msgScrollRef = ref<HTMLElement | null>(null);

const activeThread = computed(() => threads.value.find((t) => t.id === activeThreadId.value));

/** 旧数据「联系管理员」等在列表与标题中统一展示为「私信」 */
const displaySubject = (subject: string | undefined | null) => {
  const s = (subject || "").trim();
  if (!s || s === "联系管理员") return "私信";
  return s;
};

const threadListLetter = (t: any) => {
  const n = t.inheritorNickName || "";
  return n.slice(0, 1) || "传";
};

const fetchThreads = async () => {
  loadingList.value = true;
  try {
    let url = `/privateMessage/admin/threads?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}`;
    if (searchNick.value.trim()) url += `&inheritorNick=${encodeURIComponent(searchNick.value.trim())}`;
    const res = await http.get(url);
    if (res.code === 200) {
      threads.value = res.data.records || [];
      total.value = res.data.total ?? 0;
    }
  } finally {
    loadingList.value = false;
  }
};

const fetchMessages = async (threadId: number) => {
  loadingMsg.value = true;
  try {
    const res = await http.get(`/privateMessage/admin/thread/${threadId}/messages`);
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

const selectThread = (t: any) => {
  activeThreadId.value = t.id;
  fetchMessages(t.id);
};

const sendReply = async () => {
  const text = replyText.value.trim();
  if (!text || activeThreadId.value == null) return;
  sending.value = true;
  try {
    const res = await http.post("/privateMessage/admin/reply", {
      threadId: activeThreadId.value,
      content: text,
    });
    if (res.code === 200) {
      replyText.value = "";
      ElMessage.success("已回复");
      await fetchMessages(activeThreadId.value);
    }
  } catch {
    ElMessage.error("发送失败");
  } finally {
    sending.value = false;
  }
};

const handleSearch = () => {
  params.value.pageNum = 1;
  fetchThreads();
};

const handleReset = () => {
  searchNick.value = "";
  params.value.pageNum = 1;
  fetchThreads();
};

const deleteThread = async (threadId: number) => {
  try {
    const res = await http.delete(`/privateMessage/admin/thread/${threadId}`);
    if (res.code === 200) {
      ElMessage.success("会话已删除");
      if (activeThreadId.value === threadId) {
        activeThreadId.value = null;
        messages.value = [];
      }
      await fetchThreads();
    }
  } catch {
    ElMessage.error("删除失败");
  }
};

const deleteCurrentThread = () => {
  if (activeThreadId.value != null) deleteThread(activeThreadId.value);
};

watch(activeThreadId, () => {
  replyText.value = "";
});

onMounted(() => fetchThreads());
</script>

<template>
  <div class="pm-admin">
    <div class="toolbar">
      <el-input
        v-model="searchNick"
        placeholder="按传承人昵称筛选"
        clearable
        class="w260"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <div class="pm-layout">
      <aside class="pm-aside" v-loading="loadingList">
        <div class="aside-title">传承人会话</div>
        <div class="thread-list">
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
                {{ threadListLetter(t) }}
              </el-avatar>
              <div class="thread-body">
                <div class="thread-top">
                  <span class="nick">{{ t.inheritorNickName }}</span>
                  <div class="thread-top-end">
                    <el-popconfirm
                      width="240"
                      title="确定删除该会话及全部消息？"
                      confirm-button-text="删除"
                      confirm-button-type="danger"
                      cancel-button-text="取消"
                      @confirm="deleteThread(t.id)"
                    >
                      <template #reference>
                        <el-button type="danger" text size="small" @click.stop>删除</el-button>
                      </template>
                    </el-popconfirm>
                    <el-badge v-if="t.hasUnread" is-dot />
                  </div>
                </div>
                <div class="sub">{{ displaySubject(t.subject) }}</div>
                <div class="preview">{{ t.lastMessagePreview }}</div>
                <div class="time">{{ t.updateTime }}</div>
              </div>
            </div>
          </button>
          <el-empty v-if="!threads.length && !loadingList" description="暂无私信" />
        </div>
        <el-pagination
          v-if="total > params.pageSize"
          small
          layout="prev, pager, next"
          :total="total"
          :page-size="params.pageSize"
          v-model:current-page="params.pageNum"
          @current-change="fetchThreads"
          class="aside-page"
        />
      </aside>

      <section class="pm-chat">
        <template v-if="activeThreadId">
          <header class="chat-head">
            <div class="chat-head-row">
              <div class="chat-head-left">
                <el-avatar class="head-ava" :size="48" :src="avatarSrc(activeThread?.counterpartyAvatar)">
                  {{ threadListLetter(activeThread || {}) }}
                </el-avatar>
                <div class="chat-head-main">
                  <h3>{{ displaySubject(activeThread?.subject) }}</h3>
                  <p class="chat-meta">
                    传承人：<strong>{{ activeThread?.inheritorNickName }}</strong>
                  </p>
                </div>
              </div>
              <el-popconfirm
                width="240"
                title="确定删除当前会话及全部消息？"
                confirm-button-text="删除"
                confirm-button-type="danger"
                cancel-button-text="取消"
                @confirm="deleteCurrentThread"
              >
                <template #reference>
                  <el-button type="danger" plain size="small">删除会话</el-button>
                </template>
              </el-popconfirm>
            </div>
          </header>
          <div ref="msgScrollRef" v-loading="loadingMsg" class="msg-scroll">
            <div
              v-for="m in messages"
              :key="m.id"
              class="msg-row"
              :class="m.senderRole === 1 ? 'msg-row--admin' : 'msg-row--inh'"
            >
              <el-avatar class="msg-avatar" :size="40" :src="avatarSrc(m.senderAvatar)">
                {{ (m.senderName || "?").slice(0, 1) }}
              </el-avatar>
              <div class="msg-stack">
                <div class="msg-head">
                  <strong class="msg-name">{{ m.senderName }}</strong>
                  <el-tag v-if="m.senderRole === 1" size="small" type="danger" effect="plain">管理员</el-tag>
                  <el-tag v-else size="small" type="warning" effect="plain">传承人</el-tag>
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
              :rows="4"
              maxlength="5000"
              show-word-limit
              placeholder="回复传承人…"
              @keydown.enter.exact.prevent="sendReply"
            />
            <el-button type="primary" :loading="sending" :icon="Promotion" @click="sendReply">
              发送回复
            </el-button>
          </footer>
        </template>
        <div v-else class="chat-empty">
          <p>请选择左侧会话进行回复</p>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped lang="scss">
.pm-admin {
  padding: 16px;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-bottom: 16px;
}

.w260 {
  width: min(280px, 100%);
}

.pm-layout {
  display: grid;
  grid-template-columns: minmax(280px, 340px) 1fr;
  gap: 16px;
  min-height: 560px;
}

.pm-aside {
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-bg-color);
  display: flex;
  flex-direction: column;
}

.aside-title {
  padding: 12px 14px;
  font-weight: 600;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.thread-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  max-height: 560px;
}

.thread-item {
  width: 100%;
  text-align: left;
  border: 1px solid transparent;
  border-radius: 8px;
  padding: 10px 12px;
  margin-bottom: 8px;
  cursor: pointer;
  background: var(--el-fill-color-blank);

  &:hover {
    border-color: var(--el-color-primary-light-5);
  }

  &.active {
    border-color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
  }

  &.unread .nick {
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

.thread-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.thread-top-end {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.nick {
  font-size: 14px;
  color: var(--el-text-color-primary);
  min-width: 0;
  word-break: break-word;
}

.sub {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

.preview {
  font-size: 12px;
  color: var(--el-text-color-regular);
  margin-top: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.time {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  margin-top: 6px;
}

.aside-page {
  padding: 8px;
  justify-content: center;
}

.pm-chat {
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-bg-color-overlay);
  display: flex;
  flex-direction: column;
  min-height: 560px;
}

.chat-head {
  padding: 14px 18px;
  border-bottom: 1px solid var(--el-border-color-lighter);

  .chat-head-row {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
  }

  .chat-head-left {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    min-width: 0;
    flex: 1;
  }

  .head-ava {
    flex-shrink: 0;
  }

  .chat-head-main {
    min-width: 0;
  }

  h3 {
    margin: 0 0 6px;
    font-size: 18px;
    word-break: break-word;
  }

  .chat-meta {
    margin: 0;
    font-size: 13px;
    color: var(--el-text-color-regular);
  }
}

.msg-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 12px 14px;
  max-height: 420px;
  background: var(--el-fill-color-blank);
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
  color: var(--el-text-color-primary);
}

.msg-time {
  color: var(--el-text-color-secondary);
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
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
}

.msg-row--admin .msg-bubble {
  background: var(--el-color-danger-light-9);
  border: 1px solid var(--el-color-danger-light-7);
}

.compose {
  padding: 12px 16px;
  border-top: 1px solid var(--el-border-color-lighter);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chat-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary);
}

@media (max-width: 960px) {
  .pm-layout {
    grid-template-columns: 1fr;
  }
}
</style>
