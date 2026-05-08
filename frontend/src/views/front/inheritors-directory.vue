<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { Search, UserFilled } from "@element-plus/icons-vue";
import http from "@/utils/http";
import { getImageUrl } from "@/utils/system";

const router = useRouter();

const keyword = ref("");
const pageNum = ref(1);
const pageSize = ref(12);
const total = ref(0);
const loading = ref(false);
const list = ref<any[]>([]);

const load = async () => {
  loading.value = true;
  try {
    let url = `/inheritor/public/page?pageNum=${pageNum.value}&pageSize=${pageSize.value}`;
    if (keyword.value.trim()) url += `&keyword=${encodeURIComponent(keyword.value.trim())}`;
    const res = await http.get(url);
    if (res.code === 200) {
      list.value = res.data.records || [];
      total.value = res.data.total ?? 0;
    }
  } finally {
    loading.value = false;
  }
};

const goProfile = (id: number) => {
  router.push(`/front/inheritor/${id}`);
};

const excerpt = (text: string) => {
  if (!text) return "暂无简介";
  return text.length > 80 ? text.slice(0, 80) + "…" : text;
};

onMounted(load);
</script>

<template>
  <div class="page">
    <section class="banner">
      <h1>传承人名录</h1>
      <p>浏览已通过认证的非遗传承人公开主页，查看其作品与动态</p>
      <div class="search-row">
        <el-input
          v-model="keyword"
          placeholder="搜索昵称或账号"
          clearable
          class="search-input"
          @keyup.enter="() => { pageNum = 1; load(); }"
        />
        <el-button type="primary" :icon="Search" @click="() => { pageNum = 1; load(); }">搜索</el-button>
      </div>
    </section>

    <el-skeleton v-if="loading" animated :rows="6" />
    <div v-else-if="!list.length" class="empty-wrap">
      <el-empty description="暂无传承人" />
    </div>
    <div v-else class="grid">
      <article
        v-for="item in list"
        :key="item.id"
        class="card"
        role="button"
        tabindex="0"
        @click="goProfile(item.id)"
        @keyup.enter="goProfile(item.id)"
      >
        <div class="avatar-box">
          <el-avatar v-if="item.avatar" :size="72" :src="getImageUrl(item.avatar)" />
          <el-avatar v-else :size="72"><UserFilled /></el-avatar>
        </div>
        <h2 class="name">{{ item.nickName || "传承人" }}</h2>
        <p class="bio">{{ excerpt(item.profile) }}</p>
        <span class="link-hint">进入主页 →</span>
      </article>
    </div>

    <div v-if="total > pageSize" class="pager">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="load"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
.page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}

.banner {
  text-align: center;
  margin-bottom: 32px;

  h1 {
    margin: 0 0 10px;
    font-size: 28px;
    color: var(--el-text-color-primary);
  }

  p {
    margin: 0 0 20px;
    color: var(--el-text-color-secondary);
    font-size: 15px;
  }
}

.search-row {
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.search-input {
  max-width: 360px;
  width: 100%;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.card {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 16px;
  padding: 22px 20px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  outline: none;

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--el-box-shadow-light);
    border-color: var(--el-color-primary-light-5);
  }

  &:focus-visible {
    box-shadow: 0 0 0 2px var(--el-color-primary-light-7);
  }
}

.avatar-box {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;
}

.name {
  margin: 0 0 10px;
  font-size: 18px;
  text-align: center;
}

.bio {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  line-height: 1.55;
  min-height: 3.1em;
}

.link-hint {
  font-size: 13px;
  color: var(--el-color-primary);
  display: block;
  text-align: center;
}

.empty-wrap {
  padding: 48px 0;
}

.pager {
  margin-top: 28px;
  display: flex;
  justify-content: center;
}
</style>
