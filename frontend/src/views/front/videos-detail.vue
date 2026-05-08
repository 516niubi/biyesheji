<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import http from '../../utils/http'
import { getImageUrl } from '../../utils/system'
import { ElMessage } from 'element-plus'
import { Clock, View, VideoCamera } from '@element-plus/icons-vue'
import PublisherInheritorRow from '../../components/PublisherInheritorRow.vue'

const route = useRoute()
const loading = ref(false)
const videoDetail = ref<any>({})
const videoSrc = ref('')

const getVideoDetail = async () => {
  const id = route.query.id
  if (!id) {
    ElMessage.error('参数错误')
    return
  }

  loading.value = true
  try {
    const res = await http.get(`/video/getById?id=${id}`)
    if (res.code === 200) {
      videoDetail.value = res.data
      videoSrc.value = getImageUrl(res.data.url)
    } else {
      ElMessage.error('获取详情失败')
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  getVideoDetail()
})
</script>

<template>
  <div class="video-detail-page" v-loading="loading">
    <template v-if="!loading && videoDetail.id">
      <nav class="breadcrumb" aria-label="面包屑导航">
        <router-link to="/front/home">首页</router-link>
        <span class="sep">/</span>
        <router-link to="/front/videos">非遗宣传</router-link>
        <span class="sep">/</span>
        <span class="current">视频详情</span>
      </nav>

      <article class="video-shell">
        <header class="video-header">
          <p class="kicker">宣传视频</p>
          <h1 class="title">{{ videoDetail.title }}</h1>
          <div class="meta-bar">
            <div class="meta-pill meta-pill--publisher">
              <PublisherInheritorRow
                :inheritor-id="videoDetail.creatorId"
                :name="videoDetail.publisherName || '平台'"
                :avatar="videoDetail.publisherAvatar"
                :size="40"
              />
            </div>
            <span class="meta-pill" v-if="videoDetail.createTime">
              <el-icon><Clock /></el-icon>
              {{ formatDate(videoDetail.createTime) }}
            </span>
            <span class="meta-pill">
              <el-icon><View /></el-icon>
              {{ videoDetail.viewCount ?? 0 }} 次播放
            </span>
          </div>
        </header>

        <div class="player-card">
          <div class="player-inner">
            <video
              v-if="videoSrc"
              class="video-player"
              :src="videoSrc"
              controls
              playsinline
              preload="metadata"
            />
            <div v-else class="player-placeholder">
              <el-icon :size="40"><VideoCamera /></el-icon>
              <span>暂无可播放资源</span>
            </div>
          </div>
        </div>
      </article>
    </template>

    <div v-else-if="!loading" class="empty-wrap">
      <el-empty description="暂无数据" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.video-detail-page {
  --accent: #c53d2e;
  --ink: #1f1a17;
  --muted: #5c534c;
  --card: #ffffff;
  --line: rgba(31, 26, 23, 0.08);
  min-height: 100vh;
  padding: 28px 20px 56px;
  background: linear-gradient(180deg, #ebe4f5 0%, #f6f3ef 38%, #f8f9fa 100%);
}

.breadcrumb {
  max-width: 960px;
  margin: 0 auto 20px;
  font-size: 13px;
  color: var(--muted);
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;

  a {
    color: #5b4f9a;
    font-weight: 600;
  }

  .sep {
    opacity: 0.45;
  }

  .current {
    color: var(--ink);
    font-weight: 700;
  }
}

.video-shell {
  max-width: 960px;
  margin: 0 auto;
  background: var(--card);
  border-radius: 20px;
  padding: 36px 40px 40px;
  box-shadow: 0 12px 40px rgba(31, 26, 23, 0.08);
  border: 1px solid var(--line);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4px;
    background: linear-gradient(180deg, #4a3f7a, #7c6ba8);
  }
}

.video-header {
  margin-bottom: 24px;
}

.kicker {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #5b4f9a;
  font-weight: 700;
}

.title {
  margin: 0 0 18px;
  font-size: clamp(24px, 4vw, 32px);
  font-weight: 800;
  color: var(--ink);
  line-height: 1.35;
  letter-spacing: -0.02em;
}

.meta-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.meta-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 999px;
  font-size: 13px;
  color: var(--muted);
  background: rgba(74, 63, 122, 0.08);
  border: 1px solid var(--line);
  font-weight: 500;

  .el-icon {
    color: var(--accent);
  }

  &--publisher {
    border-radius: 14px;
    padding: 10px 16px;
  }
}

.player-card {
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid var(--line);
  background: #0f0f12;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.18);
}

.player-inner {
  position: relative;
}

.video-player {
  width: 100%;
  max-height: 540px;
  display: block;
  background: #000;
}

.player-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 240px;
  color: rgba(255, 255, 255, 0.65);
  font-size: 14px;
}

.empty-wrap {
  max-width: 480px;
  margin: 80px auto;
}

@media (max-width: 768px) {
  .video-shell {
    padding: 24px 18px 28px;
  }

  .video-player {
    max-height: 220px;
  }
}
</style>
