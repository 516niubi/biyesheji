<script setup>
import { ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

// 首页标签
const homeTag = {
  path: "/home",
  title: "首页",
  name: "dashboard",
  affix: true, // 固定标签，不可关闭
};

// 标签列表
const tags = ref([homeTag]);

// 当前激活的标签
const activeTag = ref(route.path);

// 添加标签
const addTag = (tag) => {
  // 如果是首页路径，只需要激活首页标签
  if (tag.path === homeTag.path) {
    activeTag.value = homeTag.path;
    return;
  }

  // 如果标签已存在，只更新激活状态
  if (tags.value.some((item) => item.path === tag.path)) {
    activeTag.value = tag.path;
    return;
  }

  // 添加新标签
  tags.value.push(tag);
  activeTag.value = tag.path;
};

// 关闭标签
const closeTag = (path) => {
  // 不允许关闭首页
  if (path === homeTag.path) return;

  const index = tags.value.findIndex((item) => item.path === path);
  if (index === -1) return;

  // 如果关闭的是当前激活的标签
  if (path === activeTag.value) {
    const nextTag = tags.value[index - 1] || tags.value[index + 1] || homeTag;
    router.push(nextTag.path);
  }

  tags.value.splice(index, 1);

  // 如果关闭后没有其他标签了，跳转到首页
  if (tags.value.length === 1) {
    router.push(homeTag.path);
  }
};

// 点击标签
const clickTag = (path) => {
  activeTag.value = path;
  router.push(path);
};

// 监听路由变化，添加标签
watch(
  () => route.path,
  (newPath) => {
    // 跳过 入口
    // if (route.meta.title === "入口") return;
    addTag({
      path: newPath,
      title: route.meta.title || "未命名",
      name: route.name,
    });
  },
  { immediate: true }
);
</script>

<template>
  <div class="tags-view">
    <el-scrollbar class="tags-scrollbar">
      <div class="tags-wrapper">
        <el-tag
          v-for="tag in tags"
          :key="tag.path"
          :type="activeTag === tag.path ? '' : 'info'"
          :effect="activeTag === tag.path ? 'dark' : 'plain'"
          :closable="!tag.affix"
          class="tag-item"
          @click="clickTag(tag.path)"
          @close="closeTag(tag.path)"
        >
          {{ tag.title }}
        </el-tag>
      </div>
    </el-scrollbar>
  </div>
</template>

<style lang="scss" scoped>
.tags-view {
  height: 34px;
  background: #fff;
  border-bottom: 1px solid #d8dce5;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.12), 0 0 3px 0 rgba(0, 0, 0, 0.04);
}

.tags-scrollbar {
  height: 100%;
  white-space: nowrap;

  :deep(.el-scrollbar__wrap) {
    height: 100%;
  }
}

.tags-wrapper {
  padding: 4px 8px;
}

.tag-item {
  margin-right: 4px;
  cursor: pointer;
  height: 26px;
  line-height: 26px;

  &:last-child {
    margin-right: 15px;
  }

  &:hover {
    opacity: 0.85;
  }
}
</style>
