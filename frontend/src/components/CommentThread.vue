<script setup lang="ts">
import { ref, computed } from "vue";
import CommentThreadSelf from "./CommentThread.vue";
import { getImageUrl } from "@/utils/system";
import { getStoredToken } from "@/utils/authToken";
import Config from "@/config";
import { ElMessage } from "element-plus";

const props = withDefaults(
  defineProps<{
    comment: Record<string, any>;
    depth: number;
    /** 为 false 时隐藏回复入口（如只读列表） */
    canReply?: boolean;
    /** 为 true 时显示单条删除（传承人工作室等；会删除该条及其所有子回复） */
    canDelete?: boolean;
  }>(),
  { canReply: true, canDelete: false }
);

const emit = defineEmits<{
  submitReply: [payload: { parentId: number; content: string; images: string[] }];
  deleteComment: [id: number];
}>();

const showReply = ref(false);
const replyText = ref("");
const replyImages = ref<string[]>([]);

const uploadUrl = `${Config.baseUrl}/file/upload`;
const uploadHeaders = computed(() => ({ authorization: `${getStoredToken()}` }));

const beforeUpload = (raw: File) => {
  if (!raw.type.includes("image")) {
    ElMessage.error("仅支持图片");
    return false;
  }
  if (raw.size / 1024 / 1024 > 2) {
    ElMessage.error("单张图片不超过 2MB");
    return false;
  }
  return true;
};

const onUploadSuccess = (res: any) => {
  if (res?.code === 200 && res.data) {
    if (replyImages.value.length >= 9) {
      ElMessage.warning("最多 9 张图片");
      return;
    }
    replyImages.value.push(res.data);
  }
};

const removeImg = (idx: number) => {
  replyImages.value.splice(idx, 1);
};

const sendReply = () => {
  const t = replyText.value.trim();
  if (!t && !replyImages.value.length) {
    ElMessage.warning("请输入文字或上传图片");
    return;
  }
  emit("submitReply", {
    parentId: props.comment.id,
    content: t,
    images: [...replyImages.value],
  });
  replyText.value = "";
  replyImages.value = [];
  showReply.value = false;
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return "";
  const date = new Date(dateStr);
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
};
</script>

<template>
  <div class="thread" :class="{ 'thread--root': depth === 0 }" :style="{ marginLeft: depth === 0 ? 0 : Math.min(depth, 10) * 14 + 'px' }">
    <div class="comment-item">
      <div class="comment-avatar">
        <el-avatar :src="getImageUrl(comment.avatar)" size="large" />
      </div>
      <div class="comment-body">
        <div class="comment-header">
          <span class="comment-author">{{ comment.userName || "匿名用户" }}</span>
          <el-tag v-if="comment.fromInheritor" type="danger" size="small" effect="plain" round class="inh-tag">
            传承人
          </el-tag>
          <span v-if="comment.replyToName" class="reply-to">回复 @{{ comment.replyToName }}</span>
          <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
        </div>
        <div v-if="comment.content" class="comment-text">{{ comment.content }}</div>
        <div v-if="comment.imageUrls?.length" class="comment-images">
          <el-image
            v-for="(url, idx) in comment.imageUrls"
            :key="idx"
            :src="getImageUrl(url)"
            :preview-src-list="comment.imageUrls.map((u) => getImageUrl(u))"
            :initial-index="idx"
            fit="cover"
            class="cmt-img"
          />
        </div>
        <div v-if="props.canReply || props.canDelete" class="comment-toolbar">
          <el-button
            v-if="props.canReply"
            type="primary"
            link
            size="small"
            @click="showReply = !showReply"
          >
            {{ showReply ? "取消" : "回复" }}
          </el-button>
          <el-popconfirm
            v-if="props.canDelete && comment.id != null"
            title="确定删除这条评论？其下的回复也会一并删除。"
            confirm-button-text="删除"
            cancel-button-text="取消"
            width="260"
            @confirm="emit('deleteComment', comment.id)"
          >
            <template #reference>
              <el-button type="danger" link size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </div>

        <div v-if="props.canReply && showReply" class="reply-editor">
          <el-input v-model="replyText" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="写下回复…" />
          <div class="reply-upload">
            <span class="lbl">图片（可选，最多9张）</span>
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :on-success="onUploadSuccess"
              accept="image/*"
            >
              <el-button size="small">上传图片</el-button>
            </el-upload>
          </div>
          <div v-if="replyImages.length" class="thumb-row">
            <div v-for="(p, i) in replyImages" :key="i" class="thumb-wrap">
              <el-image :src="getImageUrl(p)" fit="cover" class="thumb" />
              <el-button text type="danger" size="small" class="rm" @click="removeImg(i)">移除</el-button>
            </div>
          </div>
          <el-button type="primary" size="small" round @click="sendReply">发送回复</el-button>
        </div>
      </div>
    </div>

    <div v-if="comment.children?.length" class="children">
      <CommentThreadSelf
        v-for="ch in comment.children"
        :key="ch.id"
        :comment="ch"
        :depth="depth + 1"
        :can-reply="props.canReply"
        :can-delete="props.canDelete"
        @submit-reply="$emit('submitReply', $event)"
        @delete-comment="$emit('deleteComment', $event)"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
.thread {
  border-left: 2px solid rgba(168, 44, 40, 0.15);
  padding-left: 10px;
  margin-bottom: 12px;
}

.thread--root {
  border-left: none;
  padding-left: 0;
}

.comment-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.comment-author {
  font-weight: 600;
  color: #1f1a17;
}

.inh-tag {
  margin: 0 !important;
}

.reply-to {
  font-size: 12px;
  color: #a82c28;
}

.comment-time {
  margin-left: auto;
  font-size: 12px;
  color: #9a8f88;
}

.comment-text {
  font-size: 14px;
  line-height: 1.6;
  color: #4d4540;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.cmt-img {
  width: 96px;
  height: 96px;
  border-radius: 10px;
  cursor: pointer;
}

.comment-toolbar {
  margin-top: 6px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px 12px;
}

.reply-editor {
  margin-top: 12px;
  padding: 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(168, 44, 40, 0.15);
}

.reply-upload {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 10px 0;
  flex-wrap: wrap;
  .lbl {
    font-size: 12px;
    color: #7a6a62;
  }
}

.thumb-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
}

.thumb-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.thumb {
  width: 72px;
  height: 72px;
  border-radius: 8px;
}

.children {
  margin-top: 4px;
}
</style>
