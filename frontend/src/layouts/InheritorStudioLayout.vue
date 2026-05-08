<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  Collection,
  VideoCamera,
  Reading,
  Calendar,
  ChatDotRound,
  List,
  Message,
} from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();

const tabs = [
  { path: "/studio/heritage", label: "非遗文物", icon: Collection, hint: "图文作品·前台展示" },
  { path: "/studio/video", label: "宣传视频", icon: VideoCamera, hint: "影像记录" },
  { path: "/studio/article", label: "资讯动态", icon: Reading, hint: "文章与播报" },
  { path: "/studio/activity", label: "活动", icon: Calendar, hint: "线下线上活动" },
  { path: "/studio/comment", label: "评论", icon: ChatDotRound, hint: "受众留言" },
  { path: "/studio/application", label: "报名审核", icon: List, hint: "活动报名" },
  { path: "/studio/privateMessage", label: "私信", icon: Message, hint: "访客与平台" },
];

const activePath = computed(() => route.path);

const go = (path: string) => {
  if (path !== route.path) router.push(path);
};
</script>

<template>
  <div class="studio-shell">
    <header class="studio-hero">
      <div class="hero-copy">
        <p class="eyebrow">传承人专属</p>
        <h1>内容工作室</h1>
        <p class="sub">
          以更直观的方式管理您的非遗作品。以下为<strong>仅本人可见范围</strong>的数据，编辑后立即作用于前台展示。
        </p>
      </div>
      <div class="hero-glow" aria-hidden="true" />
    </header>

    <nav class="tab-strip" aria-label="内容分类">
      <button
        v-for="t in tabs"
        :key="t.path"
        type="button"
        class="tab-btn"
        :class="{ active: activePath === t.path }"
        @click="go(t.path)"
      >
        <span class="tab-icon">
          <el-icon :size="20"><component :is="t.icon" /></el-icon>
        </span>
        <span class="tab-label">{{ t.label }}</span>
        <span class="tab-hint">{{ t.hint }}</span>
      </button>
    </nav>

    <main class="studio-panel">
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<style scoped lang="scss">
$ink: #5c2624;
$gold: #c9a227;

.studio-shell {
  max-width: 1280px;
  margin: 0 auto;
}

.studio-hero {
  position: relative;
  border-radius: 20px;
  padding: 28px 32px 32px;
  margin-bottom: 20px;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(92, 38, 36, 0.92) 0%, rgba(139, 69, 69, 0.85) 42%, rgba(201, 162, 39, 0.35) 100%);
  color: #fff;
  box-shadow: 0 18px 48px rgba(45, 28, 18, 0.18);
}

.hero-glow {
  position: absolute;
  right: -20%;
  top: -40%;
  width: 55%;
  height: 180%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.14) 0%, transparent 65%);
  pointer-events: none;
}

.hero-copy {
  position: relative;
  z-index: 1;
  max-width: 640px;
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  opacity: 0.85;
}

.hero-copy h1 {
  margin: 0 0 10px;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.sub {
  margin: 0;
  font-size: 14px;
  line-height: 1.65;
  opacity: 0.92;
}

.sub strong {
  color: lighten($gold, 22%);
}

.tab-strip {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(158px, 1fr));
  gap: 10px;
  margin-bottom: 18px;
}

.tab-btn {
  text-align: left;
  border: 1px solid rgba(92, 38, 36, 0.12);
  border-radius: 14px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(10px);
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease, background 0.18s ease;
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: #3d3d3d;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 28px rgba(45, 28, 18, 0.1);
    border-color: rgba(201, 162, 39, 0.45);
  }

  &.active {
    background: linear-gradient(145deg, #fffdf9 0%, #fff6e8 100%);
    border-color: rgba(92, 38, 36, 0.35);
    box-shadow: 0 12px 32px rgba(92, 38, 36, 0.12);
  }
}

.tab-icon {
  color: $ink;
  opacity: 0.9;
}

.tab-label {
  font-weight: 600;
  font-size: 15px;
}

.tab-hint {
  font-size: 11px;
  color: #777;
  line-height: 1.35;
}

.studio-panel {
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(14px);
  border-radius: 18px;
  border: 1px solid rgba(92, 38, 36, 0.1);
  padding: 22px 22px 28px;
  box-shadow: 0 16px 40px rgba(45, 28, 18, 0.08);
  min-height: 420px;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
