<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ChatDotRound, UserFilled } from "@element-plus/icons-vue";
import useUserStore from "@/stores/userStore";
import VideoCard from "@/components/VideoCard.vue";
import ActivityCard from "@/components/ActivityCard.vue";
import http from "@/utils/http";
import { getImageUrl } from "@/utils/system";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const creatorId = computed(() => {
  const n = Number(route.params.id);
  return Number.isFinite(n) ? n : NaN;
});

const profile = ref<any>(null);
const loadError = ref(false);

const heritageList = ref<any[]>([]);
const videoList = ref<any[]>([]);
const articleList = ref<any[]>([]);
const activityList = ref<any[]>([]);
const loading = ref(true);

/** 与 VideoCard 约定字段对齐；接口已含发布人信息时必须传入，否则会误显示「平台」 */
const videoCardPayload = (row: any) => ({
  id: row.id,
  title: row.title || "视频",
  url: row.url || "",
  coverUrl: row.coverUrl,
  viewCount: row.viewCount ?? 0,
  createTime: row.createTime || "",
  creatorId: row.creatorId ?? creatorId.value,
  publisherName: row.publisherName || profile.value?.nickName,
  publisherAvatar: row.publisherAvatar ?? profile.value?.avatar,
});

const loadAll = async () => {
  if (!Number.isFinite(creatorId.value)) {
    loadError.value = true;
    loading.value = false;
    return;
  }
  loading.value = true;
  loadError.value = false;
  const id = creatorId.value;
  try {
    const pres = await http.get(`/inheritor/public/profile/${id}`);
    if (pres.code !== 200) {
      loadError.value = true;
      profile.value = null;
      return;
    }
    profile.value = pres.data;

    const [h, v, ar, ac] = await Promise.all([
      http.get(`/culturalHeritage/page?pageNum=1&pageSize=12&creatorId=${id}`),
      http.get(`/video/page?pageNum=1&pageSize=12&creatorId=${id}`),
      http.get(`/article/page?pageNum=1&pageSize=12&creatorId=${id}`),
      http.get(`/activity/page?pageNum=1&pageSize=12&creatorId=${id}`),
    ]);
    heritageList.value = h.code === 200 ? h.data.records || [] : [];
    videoList.value = v.code === 200 ? v.data.records || [] : [];
    articleList.value = ar.code === 200 ? ar.data.records || [] : [];
    activityList.value = ac.code === 200 ? ac.data.records || [] : [];
  } finally {
    loading.value = false;
  }
};

const goHeritage = (hid: number) => {
  router.push({ path: "/front/heritageDetail", query: { id: hid } });
};

const goArticle = (aid: number) => {
  router.push({ path: "/front/newsDetail", query: { id: aid } });
};

const goDirectory = () => router.push("/front/inheritors");

/** 跳转私信页并带上传承人 id；未登录先去登录页，成功后回到私信 */
const goPrivateMessage = () => {
  const id = creatorId.value;
  const target = `/front/inbox?inheritorId=${id}`;
  const token = userStore.token || localStorage.getItem("token");
  if (!token) {
    router.push({ path: "/login", query: { redirect: target } });
    return;
  }
  router.push(target);
};

onMounted(loadAll);
watch(
  () => route.params.id,
  () => loadAll()
);
</script>

<template>
  <div class="page">
    <el-skeleton v-if="loading" animated :rows="8" />
    <template v-else-if="loadError || !profile">
      <div class="error-box">
        <el-result icon="warning" title="无法访问该主页" sub-title="传承人不存在或未通过审核">
          <template #extra>
            <el-button type="primary" @click="goDirectory">返回传承人名录</el-button>
          </template>
        </el-result>
      </div>
    </template>
    <template v-else>
      <section class="hero">
        <div class="hero-bg" />
        <div class="hero-inner">
          <el-avatar v-if="profile.avatar" class="hero-avatar" :size="96" :src="getImageUrl(profile.avatar)" />
          <el-avatar v-else class="hero-avatar" :size="96"><UserFilled /></el-avatar>
          <div class="hero-text">
            <h1>{{ profile.nickName || "传承人" }}</h1>
            <p class="bio">{{ profile.profile || "这位传承人暂未填写简介。" }}</p>
            <div class="hero-actions">
              <el-button type="primary" round :icon="ChatDotRound" @click="goPrivateMessage">
                发私信
              </el-button>
              <el-button text type="primary" @click="goDirectory">← 传承人名录</el-button>
            </div>
          </div>
        </div>
      </section>

      <section class="block">
        <h2 class="block-title">非遗文物</h2>
        <p v-if="!heritageList.length" class="empty-line">暂无公开的非遗文物条目</p>
        <div v-else class="heritage-grid">
          <div
            v-for="h in heritageList"
            :key="h.id"
            class="heritage-card"
            role="button"
            tabindex="0"
            @click="goHeritage(h.id)"
            @keyup.enter="goHeritage(h.id)"
          >
            <el-image :src="getImageUrl(h.coverImage)" fit="cover" class="h-cover">
              <template #error>
                <div class="img-fallback">暂无封面</div>
              </template>
            </el-image>
            <div class="h-info">
              <h3>{{ h.name }}</h3>
              <span class="cat">{{ h.categoryName || "非遗" }}</span>
              <p v-if="h.intro" class="intro">{{ h.intro }}</p>
            </div>
          </div>
        </div>
      </section>

      <section class="block">
        <h2 class="block-title">宣传视频</h2>
        <p v-if="!videoList.length" class="empty-line">暂无视频</p>
        <div v-else class="video-grid">
          <VideoCard v-for="vid in videoList" :key="vid.id" :video="videoCardPayload(vid)" size="small" />
        </div>
      </section>

      <section class="block">
        <h2 class="block-title">资讯动态</h2>
        <p v-if="!articleList.length" class="empty-line">暂无资讯</p>
        <div v-else class="article-list">
          <div
            v-for="a in articleList"
            :key="a.id"
            class="article-row"
            role="button"
            tabindex="0"
            @click="goArticle(a.id)"
            @keyup.enter="goArticle(a.id)"
          >
            <el-image v-if="a.coverUrl" :src="getImageUrl(a.coverUrl)" class="ar-cover" fit="cover" />
            <div class="ar-main">
              <h3>{{ a.title }}</h3>
              <p>{{ a.intro }}</p>
            </div>
          </div>
        </div>
      </section>

      <section class="block">
        <h2 class="block-title">活动</h2>
        <p v-if="!activityList.length" class="empty-line">暂无活动</p>
        <div v-else class="activity-grid">
          <ActivityCard v-for="act in activityList" :key="act.id" :activity="act" size="small" />
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped lang="scss">
.page {
  max-width: 1100px;
  margin: 0 auto;
  padding-bottom: 48px;
}

.error-box {
  padding: 48px 16px;
}

.hero {
  position: relative;
  margin-bottom: 28px;
  border-radius: 0 0 20px 20px;
  overflow: hidden;
}

.hero-bg {
  height: 140px;
  background: linear-gradient(120deg, rgba(139, 69, 69, 0.55), rgba(212, 165, 116, 0.45));
}

.hero-inner {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  padding: 0 24px 28px;
  margin-top: -48px;
  position: relative;
}

.hero-avatar {
  border: 4px solid var(--el-bg-color);
  flex-shrink: 0;
}

.hero-text h1 {
  margin: 52px 0 8px;
  font-size: 26px;
}

.bio {
  margin: 0 0 10px;
  max-width: 720px;
  line-height: 1.6;
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.block {
  padding: 0 16px;
  margin-bottom: 36px;
}

.block-title {
  margin: 0 0 14px;
  font-size: 20px;
  border-left: 4px solid var(--el-color-primary);
  padding-left: 12px;
}

.empty-line {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.heritage-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 18px;
}

.heritage-card {
  background: var(--el-bg-color);
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  outline: none;

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--el-box-shadow-light);
  }
}

.h-cover {
  width: 100%;
  height: 140px;
}

.img-fallback {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: var(--el-text-color-placeholder);
  background: var(--el-fill-color-light);
}

.h-info {
  padding: 12px 14px 16px;
}

.h-info h3 {
  margin: 0 0 6px;
  font-size: 16px;
}

.cat {
  font-size: 12px;
  color: var(--el-color-primary);
}

.intro {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 18px;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.article-row {
  display: flex;
  gap: 14px;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter);
  cursor: pointer;
  background: var(--el-bg-color);
  outline: none;
  transition: border-color 0.15s ease;

  &:hover {
    border-color: var(--el-color-primary-light-5);
  }
}

.ar-cover {
  width: 120px;
  height: 72px;
  border-radius: 8px;
  flex-shrink: 0;
}

.ar-main h3 {
  margin: 0 0 6px;
  font-size: 16px;
}

.ar-main p {
  margin: 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 18px;
}
</style>
