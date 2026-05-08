<script setup>
import Header from "./components/header.vue";
import Aside from "./components/aside.vue";
import TagsView from "./components/tags-view.vue";
import { ref } from "vue";

const isCollapse = ref(false);
</script>

<template>
  <div class="layout-container">
    <div
      class="aside-container ich-aside-bg"
      :class="{ 'is-collapse': isCollapse }"
    >
      <Aside :is-collapse="isCollapse" />
    </div>
    <div class="main-content ich-main-bg">
      <Header v-model:is-collapse="isCollapse" />
      <TagsView />
      <div class="page-container">
        <router-view />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
// 非遗后台：宣纸暖色 + 祥云暗纹（与登录页红褐主色呼应）
$ich-ink: #6b2c28;
$ich-paper-top: #fdfbf7;
$ich-paper-mid: #f5efe6;
$ich-paper-deep: #ebe3d8;

// 祥云纹理（低对比，平铺）
$ich-pattern-clouds: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='140' height='140' viewBox='0 0 140 140'%3E%3Cg fill='none' stroke='%236b2c28' stroke-width='0.45' opacity='0.14'%3E%3Cpath d='M0 70 Q28 48 70 70 T140 70'/%3E%3Cpath d='M0 92 Q32 108 70 92 T140 92'/%3E%3Cpath d='M70 12 Q92 38 70 68 Q48 38 70 12'/%3E%3Cpath d='M18 118 Q50 100 88 118'/%3E%3C/g%3E%3C/svg%3E");

// 细锦地斜纹
$ich-pattern-weave: repeating-linear-gradient(
  -32deg,
  transparent,
  transparent 9px,
  rgba($ich-ink, 0.028) 9px,
  rgba($ich-ink, 0.028) 10px
);

.layout-container {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;

  .aside-container {
    width: 200px;
    height: 100%;
    overflow: hidden;
    transition: width 0.3s;
    border-right: 1px solid rgba($ich-ink, 0.14);
    box-shadow: 2px 0 14px rgba(45, 28, 18, 0.07);

    &.is-collapse {
      width: 64px;
    }
  }

  .ich-aside-bg {
    background-color: $ich-paper-top;
    background-image: $ich-pattern-weave, $ich-pattern-clouds;
    background-size: auto, 140px 140px;
  }

  .main-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    overflow-y: auto;
    position: relative;
    isolation: isolate;
    background-color: $ich-paper-mid;
    background-image: linear-gradient(
        168deg,
        $ich-paper-top 0%,
        $ich-paper-mid 42%,
        $ich-paper-deep 100%
      ),
      $ich-pattern-weave;

    .page-container {
      padding: 16px;
      box-sizing: border-box;
      min-height: calc(100vh - 65px - 34px); // 减去header和tags-view的高度
    }
  }

  .ich-main-bg::before {
    content: "";
    position: absolute;
    inset: 0;
    z-index: 0;
    pointer-events: none;
    background-image: $ich-pattern-clouds;
    background-size: 160px 160px;
    opacity: 0.55;
  }

  // 角隅装饰（极淡，不抢内容）
  .ich-main-bg::after {
    content: "";
    position: absolute;
    right: -8%;
    bottom: -12%;
    width: min(420px, 55vw);
    height: min(420px, 55vw);
    z-index: 0;
    pointer-events: none;
    border-radius: 50%;
    background: radial-gradient(
      circle,
      rgba($ich-ink, 0.045) 0%,
      transparent 68%
    );
    border: 1px solid rgba($ich-ink, 0.06);
    opacity: 0.85;
  }

  .main-content > :deep(.header-container),
  .main-content > :deep(.tags-view),
  .main-content > .page-container {
    position: relative;
    z-index: 1;
  }
}
</style>
