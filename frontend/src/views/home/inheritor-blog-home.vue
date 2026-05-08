<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import {
  Collection,
  VideoCamera,
  Reading,
  Calendar,
  ChatDotRound,
  List,
  Message,
  User,
  Share,
  View,
  DataAnalysis,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import useUserStore from "@/stores/userStore";
import http from "@/utils/http";
import Config from "@/config";
import { getImageUrl } from "@/utils/system";

const router = useRouter();
const userStore = useUserStore();

const profile = computed(() => {
  const u = userStore.userInfo as Record<string, unknown>;
  return {
    id: u?.id as number | undefined,
    nickName: (u?.nickName as string) || "传承人",
    avatar: u?.avatar as string | undefined,
    profile: (u?.profile as string) || "",
  };
});

const stats = ref({ heritage: 0, video: 0, article: 0, activity: 0 });

const tiles = [
  { title: "非遗文物", desc: "发布与管理非遗项目", path: "/studio/heritage", icon: Collection, accent: "purple" },
  { title: "宣传视频", desc: "上传与编辑视频内容", path: "/studio/video", icon: VideoCamera, accent: "rose" },
  { title: "资讯", desc: "撰写动态与文章", path: "/studio/article", icon: Reading, accent: "sky" },
  { title: "活动", desc: "发起与管理活动", path: "/studio/activity", icon: Calendar, accent: "green" },
  { title: "评论", desc: "查看与维护评论", path: "/studio/comment", icon: ChatDotRound, accent: "amber" },
  { title: "活动报名", desc: "审核报名记录", path: "/studio/application", icon: List, accent: "slate" },
  { title: "私信", desc: "与访客对话、咨询平台", path: "/studio/privateMessage", icon: Message, accent: "red" },
  { title: "个人信息", desc: "头像与简介（前台主页展示）", path: "/person", icon: User, accent: "stone" },
];

const loadStats = async () => {
  try {
    const [h, v, a, act] = await Promise.all([
      http.get("/culturalHeritage/manage/page?pageNum=1&pageSize=1"),
      http.get("/video/manage/page?pageNum=1&pageSize=1"),
      http.get("/article/manage/page?pageNum=1&pageSize=1"),
      http.get("/activity/manage/page?pageNum=1&pageSize=1"),
    ]);
    if (h.code === 200) stats.value.heritage = h.data?.total ?? 0;
    if (v.code === 200) stats.value.video = v.data?.total ?? 0;
    if (a.code === 200) stats.value.article = a.data?.total ?? 0;
    if (act.code === 200) stats.value.activity = act.data?.total ?? 0;
  } catch {
    /* 忽略统计失败 */
  }
};

const go = (path: string) => {
  router.push(path);
};

const publicHomeUrl = computed(() => {
  if (!profile.value.id) return "";
  const base = window.location.origin;
  return `${base}/front/inheritor/${profile.value.id}`;
});

const copyPublicLink = async () => {
  if (!publicHomeUrl.value) {
    ElMessage.warning("无法获取主页链接，请重新登录");
    return;
  }
  try {
    await navigator.clipboard.writeText(publicHomeUrl.value);
    ElMessage.success("主页链接已复制");
  } catch {
    ElMessage.info(publicHomeUrl.value);
  }
};

const previewPublic = () => {
  if (!profile.value.id) return;
  window.open(`/front/inheritor/${profile.value.id}`, "_blank");
};

onMounted(() => {
  loadStats();
});
</script>

<template>
  <div class="blog-home">
    <section class="hero">
      <div class="hero-inner">
        <div class="hero-text">
          <p class="hero-kicker">传承人工作台</p>
          <h1 class="hero-title">{{ profile.nickName }}，你好</h1>
          <p class="hero-sub">
            在这里像经营个人博客一样管理非遗内容：完善资料后，访客可在平台前台查看你的
            <strong>公开主页</strong>。
          </p>
          <div class="hero-actions">
            <el-button type="primary" :icon="Share" @click="copyPublicLink">复制公开主页链接</el-button>
            <el-button :icon="View" @click="previewPublic">新窗口预览</el-button>
          </div>
        </div>
        <div class="hero-card">
          <div class="avatar-wrap">
            <el-avatar v-if="profile.avatar" :size="96" :src="getImageUrl(profile.avatar)" />
            <el-avatar v-else :size="96">{{ profile.nickName?.slice(0, 1) || "传" }}</el-avatar>
          </div>
          <p class="card-name">{{ profile.nickName }}</p>
          <p class="card-bio">{{ profile.profile || "在「个人信息」中填写简介，将展示在公开主页上" }}</p>
          <div class="mini-stats">
            <div class="ms-item"><span class="num">{{ stats.heritage }}</span><span class="lbl">文物</span></div>
            <div class="ms-item"><span class="num">{{ stats.video }}</span><span class="lbl">视频</span></div>
            <div class="ms-item"><span class="num">{{ stats.article }}</span><span class="lbl">资讯</span></div>
            <div class="ms-item"><span class="num">{{ stats.activity }}</span><span class="lbl">活动</span></div>
          </div>
        </div>
      </div>
    </section>

    <section class="tiles-section">
      <div class="section-head">
        <h2><el-icon><DataAnalysis /></el-icon> 内容中心</h2>
        <p>点击下方卡片进入对应管理页，上传与维护内容</p>
      </div>
      <div class="tile-grid">
        <button
          v-for="item in tiles"
          :key="item.path"
          type="button"
          class="tile"
          :class="'accent-' + item.accent"
          @click="go(item.path)"
        >
          <span class="tile-icon">
            <el-icon :size="28"><component :is="item.icon" /></el-icon>
          </span>
          <span class="tile-title">{{ item.title }}</span>
          <span class="tile-desc">{{ item.desc }}</span>
        </button>
      </div>
    </section>

    <section class="hint">
      <p>
        前台展示域名与接口一致时为：<code>{{ publicHomeUrl || "登录后可生成链接" }}</code>
      </p>
      <p class="muted">提示：游客访问「传承人名录」可浏览所有已通过认证的传承人主页 · {{ Config.frontName }}</p>
    </section>
  </div>
</template>

<style scoped lang="scss">
.blog-home {
  padding: 8px 12px 32px;
  max-width: 1100px;
  margin: 0 auto;
}

.hero {
  margin-bottom: 28px;
}

.hero-inner {
  display: grid;
  grid-template-columns: 1fr minmax(260px, 320px);
  gap: 24px;
  align-items: stretch;
}

@media (max-width: 900px) {
  .hero-inner {
    grid-template-columns: 1fr;
  }
}

.hero-text {
  background: linear-gradient(135deg, rgba(139, 69, 69, 0.08) 0%, rgba(255, 255, 255, 0.95) 45%);
  border-radius: 16px;
  padding: 28px 32px;
  border: 1px solid var(--el-border-color-lighter);
}

.hero-kicker {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  letter-spacing: 0.06em;
}

.hero-title {
  margin: 0 0 12px;
  font-size: 26px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.hero-sub {
  margin: 0 0 20px;
  line-height: 1.65;
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.hero-sub strong {
  color: var(--el-color-primary);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-card {
  background: var(--el-bg-color);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid var(--el-border-color-lighter);
  box-shadow: var(--el-box-shadow-light);
  text-align: center;
}

.avatar-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;
}

.card-name {
  margin: 0 0 8px;
  font-weight: 600;
  font-size: 17px;
}

.card-bio {
  margin: 0 0 16px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
  min-height: 3em;
}

.mini-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.ms-item {
  background: var(--el-fill-color-light);
  border-radius: 10px;
  padding: 10px 6px;
}

.ms-item .num {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: var(--el-color-primary);
}

.ms-item .lbl {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.tiles-section {
  margin-bottom: 24px;
}

.section-head h2 {
  margin: 0 0 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
}

.section-head p {
  margin: 0 0 18px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.tile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 14px;
}

.tile {
  text-align: left;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 14px;
  padding: 18px 16px;
  cursor: pointer;
  background: var(--el-bg-color);
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.tile:hover {
  transform: translateY(-3px);
  box-shadow: var(--el-box-shadow-light);
  border-color: var(--el-color-primary-light-5);
}

.tile-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
  color: #fff;
}

.accent-purple .tile-icon {
  background: linear-gradient(135deg, #667eea, #764ba2);
}
.accent-rose .tile-icon {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}
.accent-sky .tile-icon {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
}
.accent-green .tile-icon {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
}
.accent-amber .tile-icon {
  background: linear-gradient(135deg, #fa709a, #fee140);
}
.accent-slate .tile-icon {
  background: linear-gradient(135deg, #868f96, #596164);
}
.accent-red .tile-icon {
  background: linear-gradient(135deg, #c53d2e, #8b2924);
}
.accent-stone .tile-icon {
  background: linear-gradient(135deg, #c79081, #dfa579);
}

.tile-title {
  font-weight: 600;
  font-size: 15px;
}

.tile-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.45;
}

.hint {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
}

.hint code {
  font-size: 12px;
  word-break: break-all;
}

.hint .muted {
  margin-top: 8px;
  font-size: 12px;
  opacity: 0.85;
}
</style>
