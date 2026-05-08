<script setup lang="ts">
import http from "@/utils/http";
import { Promotion, Plus, Search } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getImageUrl } from "@/utils/system";

const route = useRoute();
const router = useRouter();

const avatarSrc = (path: string | undefined | null) => {
  const u = getImageUrl(path || "");
  return u || undefined;
};

const params = ref({ pageNum: 1, pageSize: 30 });
const threads = ref<any[]>([]);
const totalThreads = ref(0);
const loadingList = ref(false);
const loadingMsg = ref(false);
const activeThreadId = ref<number | null>(null);
const messages = ref<any[]>([]);
const replyText = ref("");
const sending = ref(false);
const msgScrollRef = ref<HTMLElement | null>(null);

const pickerVisible = ref(false);
const inhKeyword = ref("");
const inhLoading = ref(false);
const inhList = ref<any[]>([]);
const pickedInheritorId = ref<number | null>(null);
const firstMessage = ref("");

const activeThread = computed(() => threads.value.find((t) => t.id === activeThreadId.value));

const threadListFallbackLetter = (t: any) => {
  const name = t.counterpartyName || t.inheritorNickName || "";
  return name.slice(0, 1) || "传";
};

const fetchThreads = async () => {
  loadingList.value = true;
  try {
    const res = await http.get(
      `/privateMessage/front/threads?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}`
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
    const res = await http.get(`/privateMessage/front/thread/${threadId}/messages`);
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
    const res = await http.post("/privateMessage/front/reply", {
      threadId: activeThreadId.value,
      content: text,
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

/** 气泡：2=本人用户靠右；0=传承人靠左 */
const rowCls = (m: any) =>
  m.senderRole === 2 ? "msg-row--me" : "msg-row--them";

const openPicker = () => {
  pickedInheritorId.value = null;
  inhKeyword.value = "";
  firstMessage.value = "";
  inhList.value = [];
  pickerVisible.value = true;
  searchInheritors();
};

const searchInheritors = async () => {
  inhLoading.value = true;
  try {
    const q = encodeURIComponent(inhKeyword.value.trim());
    const res = await http.get(`/inheritor/public/page?pageNum=1&pageSize=30&keyword=${q}`);
    if (res.code === 200) {
      inhList.value = res.data.records || [];
    }
  } finally {
    inhLoading.value = false;
  }
};

const confirmPickAndSend = async () => {
  if (pickedInheritorId.value == null) {
    ElMessage.warning("请选择传承人");
    return;
  }
  const c = firstMessage.value.trim();
  if (!c) {
    ElMessage.warning("请输入第一条私信");
    return;
  }
  try {
    const res = await http.post("/privateMessage/front/sendToInheritor", {
      inheritorId: pickedInheritorId.value,
      content: c,
    });
    if (res.code === 200 && res.data?.id) {
      ElMessage.success("已发送");
      pickerVisible.value = false;
      await fetchThreads();
      activeThreadId.value = res.data.id;
      await fetchMessages(res.data.id);
    }
  } catch {
    ElMessage.error("发送失败");
  }
};

watch(activeThreadId, () => {
  replyText.value = "";
});

const stripInheritorQuery = () => {
  const q = { ...route.query };
  delete q.inheritorId;
  router.replace({ path: route.path, query: q });
};

/** 从传承人主页「发私信」进入：已有会话则直达，否则打开发信对话框并预填传承人 */
const applyInheritorFromQuery = async () => {
  const raw = route.query.inheritorId;
  const id = Number(typeof raw === "string" ? raw : Array.isArray(raw) ? raw[0] : NaN);
  if (!Number.isFinite(id) || id <= 0) return;

  const hit = threads.value.find((t) => Number(t.inheritorId) === id);
  if (hit) {
    activeThreadId.value = hit.id;
    await fetchMessages(hit.id);
    stripInheritorQuery();
    return;
  }

  try {
    const pres = await http.get(`/inheritor/public/profile/${id}`);
    if (pres.code !== 200 || !pres.data) {
      ElMessage.warning("无法发起与该传承人的私信");
      stripInheritorQuery();
      return;
    }
    const row = pres.data;
    const hid = row.id != null ? Number(row.id) : id;
    inhList.value = [
      {
        id: hid,
        nickName: row.nickName,
        username: row.username,
        avatar: row.avatar,
        profile: row.profile,
      },
    ];
    pickedInheritorId.value = hid;
    firstMessage.value = "";
    pickerVisible.value = true;
    stripInheritorQuery();
  } catch {
    ElMessage.error("加载传承人信息失败");
    stripInheritorQuery();
  }
};

onMounted(async () => {
  await fetchThreads();
  await applyInheritorFromQuery();
});
</script>

<template>
  <div class="inbox-page">
    <div class="inbox-head">
      <div>
        <h1 class="title">私信</h1>
      </div>
      <el-button type="primary" round :icon="Plus" @click="openPicker">私信传承人</el-button>
    </div>

    <div class="pm-layout">
      <aside class="pm-aside">
        <div class="aside-title">会话</div>
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
                <div class="thread-top">
                  <span class="nick">{{ t.counterpartyName || t.inheritorNickName }}</span>
                  <el-badge v-if="t.hasUnread" is-dot />
                </div>
                <div class="preview">{{ t.lastMessagePreview }}</div>
                <div class="time">{{ t.updateTime }}</div>
              </div>
            </div>
          </button>
          <el-empty v-if="!loadingList && !threads.length" description="暂无会话，点击右上角私信传承人" />
        </div>
      </aside>

      <section class="pm-chat">
        <template v-if="activeThreadId">
          <header class="chat-head">
            <el-avatar class="chat-head-ava" :size="48" :src="avatarSrc(activeThread?.counterpartyAvatar)">
              {{ threadListFallbackLetter(activeThread || {}) }}
            </el-avatar>
            <div class="chat-head-text">
              <h2>{{ activeThread?.counterpartyName || activeThread?.inheritorNickName }}</h2>
              <span class="muted">与传承人对话</span>
            </div>
          </header>
          <div ref="msgScrollRef" v-loading="loadingMsg" class="msg-scroll">
            <div
              v-for="m in messages"
              :key="m.id"
              class="msg-row"
              :class="rowCls(m)"
            >
              <el-avatar class="msg-avatar" :size="40" :src="avatarSrc(m.senderAvatar)">
                {{ (m.senderName || '?').slice(0, 1) }}
              </el-avatar>
              <div class="msg-stack">
                <div class="msg-head">
                  <strong>{{ m.senderName }}</strong>
                  <el-tag v-if="m.senderRole === 2" size="small" type="primary" effect="plain">我</el-tag>
                  <el-tag v-else-if="m.senderRole === 0" size="small" type="warning" effect="plain">传承人</el-tag>
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
              placeholder="输入消息，Ctrl+Enter 发送（或点按钮）"
              @keydown.ctrl.enter="sendReply"
            />
            <el-button type="primary" :loading="sending" :icon="Promotion" @click="sendReply">
              发送
            </el-button>
          </footer>
        </template>
        <div v-else class="chat-empty">
          <p>从左侧选择会话，或发起「私信传承人」</p>
        </div>
      </section>
    </div>

    <el-dialog v-model="pickerVisible" title="选择传承人并发私信" width="560px" destroy-on-close append-to-body>
      <div class="picker-toolbar">
        <el-input
          v-model="inhKeyword"
          placeholder="搜索昵称"
          clearable
          @keyup.enter="searchInheritors"
        >
          <template #append>
            <el-button :icon="Search" @click="searchInheritors">搜索</el-button>
          </template>
        </el-input>
      </div>
      <div v-loading="inhLoading" class="inh-pick-list">
        <el-radio-group v-model="pickedInheritorId" class="inh-radios">
          <div v-for="row in inhList" :key="row.id" class="inh-option-wrap">
            <el-radio :label="row.id" class="inh-radio-row">
              <el-avatar :size="36" :src="avatarSrc(row.avatar)" />
              <div class="inh-meta">
                <div class="inh-name">{{ row.nickName || row.username }}</div>
                <div class="inh-snippet">{{ (row.profile || '').slice(0, 60) || '已通过认证传承人' }}</div>
              </div>
            </el-radio>
          </div>
        </el-radio-group>
        <el-empty v-if="!inhLoading && !inhList.length" description="未找到传承人，试试其它关键词" />
      </div>
      <el-input
        v-model="firstMessage"
        type="textarea"
        :rows="4"
        maxlength="5000"
        show-word-limit
        placeholder="第一条私信内容"
        style="margin-top: 12px"
      />
      <template #footer>
        <el-button @click="pickerVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPickAndSend">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.inbox-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px 16px 40px;
}

.inbox-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.title {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 700;
  color: #2d2420;
}

.pm-layout {
  display: grid;
  grid-template-columns: minmax(260px, 300px) 1fr;
  gap: 16px;
  min-height: 520px;
}

.pm-aside {
  border: 1px solid rgba(92, 38, 36, 0.12);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.92);
  display: flex;
  flex-direction: column;
}

.aside-title {
  padding: 12px 14px;
  font-weight: 600;
  border-bottom: 1px solid rgba(92, 38, 36, 0.08);
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
  border-radius: 12px;
  padding: 10px 12px;
  margin-bottom: 8px;
  cursor: pointer;
  background: rgba(255, 251, 248, 0.95);
  transition: border-color 0.2s, box-shadow 0.2s;

  &:hover {
    border-color: rgba(168, 44, 40, 0.22);
  }

  &.active {
    border-color: rgba(168, 44, 40, 0.35);
    background: #fff;
    box-shadow: 0 6px 18px rgba(92, 38, 36, 0.06);
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

.nick {
  font-size: 14px;
  color: #2d2420;
  min-width: 0;
  word-break: break-word;
}

.preview {
  font-size: 12px;
  color: #8b7d78;
  margin-top: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.time {
  font-size: 11px;
  color: #a89892;
  margin-top: 6px;
}

.pm-chat {
  border: 1px solid rgba(92, 38, 36, 0.12);
  border-radius: 14px;
  background: #fff;
  display: flex;
  flex-direction: column;
  min-height: 520px;
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

  h2 {
    margin: 0 0 4px;
    font-size: 18px;
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
  background: rgba(245, 240, 236, 0.5);
}

.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 14px;
  max-width: min(100%, 720px);
}

.msg-row--them {
  flex-direction: row;
  margin-right: auto;
}

.msg-row--me {
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

.msg-row--them .msg-stack {
  text-align: left;
}

.msg-row--me .msg-stack {
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

.msg-row--me .msg-head {
  justify-content: flex-end;
}

.msg-time {
  color: #8b7d78;
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

.msg-row--them .msg-bubble {
  background: #f6f7f9;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.msg-row--me .msg-bubble {
  background: linear-gradient(135deg, #fff4f0 0%, #ffe8e0 100%);
  border: 1px solid rgba(168, 44, 40, 0.22);
}

.compose {
  padding: 12px 16px;
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
}

.picker-toolbar {
  margin-bottom: 12px;
}

.inh-pick-list {
  max-height: 280px;
  overflow-y: auto;
}

.inh-option-wrap {
  padding: 4px 0;
}

.inh-radio-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  height: auto;
  margin-right: 0;
  padding: 8px;
  border-radius: 10px;
  width: 100%;

  &:hover {
    background: rgba(197, 61, 46, 0.06);
  }
}

.inh-radios {
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

.inh-meta {
  flex: 1;
  min-width: 0;
}

.inh-name {
  font-weight: 600;
  font-size: 14px;
}

.inh-snippet {
  font-size: 12px;
  color: #8b7d78;
  margin-top: 4px;
}

@media (max-width: 900px) {
  .pm-layout {
    grid-template-columns: 1fr;
  }
}
</style>
