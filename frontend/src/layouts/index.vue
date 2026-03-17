<script setup>
import Header from "./components/header.vue";
import Aside from "./components/aside.vue";
import TagsView from "./components/tags-view.vue";
import { ref } from "vue";

const isCollapse = ref(false);
</script>

<template>
  <div class="layout-container">
    <div class="aside-container" :class="{ 'is-collapse': isCollapse }">
      <Aside :is-collapse="isCollapse" />
    </div>
    <div class="main-content">
      <Header v-model:is-collapse="isCollapse" />
      <TagsView />
      <div class="page-container">
        <router-view />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
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
    background-color: #fff;
    border-right: 1px solid #dcdfe6;
    box-shadow: 2px 0 8px 0 rgba(29, 35, 41, 0.05);

    &.is-collapse {
      width: 64px;
    }
  }

  .main-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    overflow-y: auto;
    background-color: #f0f2f5;

    .page-container {
      padding: 16px;
      box-sizing: border-box;
      min-height: calc(100vh - 65px - 34px); // 减去header和tags-view的高度
    }
  }
}
</style>
