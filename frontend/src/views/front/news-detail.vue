<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import http from '../../utils/http'
import { getImageUrl } from '../../utils/system'
import { ElMessage } from 'element-plus'
import { Clock, View } from '@element-plus/icons-vue'
import PublisherInheritorRow from '../../components/PublisherInheritorRow.vue'

const route = useRoute()
const loading = ref(false)
const newsDetail = ref<any>({})

const getNewsDetail = async () => {
  const id = route.query.id
  if (!id) {
    ElMessage.error('参数错误')
    return
  }

  loading.value = true
  try {
    const res = await http.get(`/article/getById?id=${id}`)
    if (res.code === 200) {
      newsDetail.value = res.data
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
  getNewsDetail()
})
</script>

<template>
  <div class="news-detail-page" v-loading="loading">
    <template v-if="!loading && newsDetail.id">
      <nav class="breadcrumb" aria-label="面包屑导航">
        <router-link to="/front/home">首页</router-link>
        <span class="sep">/</span>
        <router-link to="/front/news">新闻资讯</router-link>
        <span class="sep">/</span>
        <span class="current">正文</span>
      </nav>

      <article class="article-shell">
        <header class="article-header">
          <p class="kicker">新闻资讯</p>
          <h1 class="title">{{ newsDetail.title }}</h1>
          <div class="meta-bar">
            <div class="meta-pill meta-pill--publisher">
              <PublisherInheritorRow
                :inheritor-id="newsDetail.creatorId"
                :name="newsDetail.publisherName || '平台'"
                :avatar="newsDetail.publisherAvatar"
                :size="40"
              />
            </div>
            <span class="meta-pill" v-if="newsDetail.createTime">
              <el-icon><Clock /></el-icon>
              {{ formatDate(newsDetail.createTime) }}
            </span>
            <span class="meta-pill">
              <el-icon><View /></el-icon>
              {{ newsDetail.viewCount ?? 0 }} 次浏览
            </span>
          </div>
        </header>

        <div v-if="newsDetail.coverUrl" class="cover-wrap">
          <el-image
            :src="getImageUrl(newsDetail.coverUrl)"
            :alt="newsDetail.title"
            class="cover-image"
            fit="cover"
            :preview-src-list="[getImageUrl(newsDetail.coverUrl)]"
            :preview-teleported="true"
            :hide-on-click-modal="true"
          >
            <template #error>
              <div class="image-error">图片加载失败</div>
            </template>
          </el-image>
        </div>

        <section v-if="newsDetail.intro" class="block intro-block">
          <h2 class="block-title">简介</h2>
          <p class="intro-text">{{ newsDetail.intro }}</p>
        </section>

        <section v-if="newsDetail.content" class="block content-block">
          <h2 class="block-title">正文</h2>
          <div class="prose" v-html="newsDetail.content"></div>
        </section>
      </article>
    </template>

    <div v-else-if="!loading" class="empty-wrap">
      <el-empty description="暂无数据" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.news-detail-page {
  --accent: #c53d2e;
  --ink: #1f1a17;
  --muted: #5c534c;
  --card: #ffffff;
  --line: rgba(31, 26, 23, 0.08);
  min-height: 100vh;
  padding: 28px 20px 56px;
  background: linear-gradient(180deg, #e8f0ec 0%, #f6f3ef 35%, #f8f9fa 100%);
}

.breadcrumb {
  max-width: 880px;
  margin: 0 auto 20px;
  font-size: 13px;
  color: var(--muted);
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;

  a {
    color: var(--accent);
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

.article-shell {
  max-width: 880px;
  margin: 0 auto;
  background: var(--card);
  border-radius: 20px;
  padding: 36px 40px 44px;
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
    background: linear-gradient(180deg, #2d6a4f, #40916c);
  }
}

.article-header {
  margin-bottom: 28px;
}

.kicker {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--accent);
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
  background: rgba(45, 106, 79, 0.08);
  border: 1px solid var(--line);
  font-weight: 500;

  .el-icon {
    color: var(--accent);
  }

  &--publisher {
    border-radius: 14px;
    padding: 10px 16px;
    align-items: center;
  }
}

.cover-wrap {
  margin-bottom: 28px;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid var(--line);
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.06);
}

.cover-image {
  width: 100%;
  max-height: 420px;
  display: block;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  background: #f0f0f0;
  color: #999;
}

.block {
  margin-bottom: 28px;

  &:last-child {
    margin-bottom: 0;
  }
}

.block-title {
  font-size: 18px;
  font-weight: 800;
  color: var(--ink);
  margin: 0 0 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--line);
}

.intro-text {
  margin: 0;
  font-size: 16px;
  line-height: 1.85;
  color: var(--muted);
}

.prose {
  font-size: 16px;
  line-height: 1.9;
  color: #3d3833;

  :deep(img) {
    max-width: 100%;
    height: auto;
    border-radius: 12px;
    margin: 16px 0;
  }

  :deep(p) {
    margin: 16px 0;
  }

  :deep(h1, h2, h3, h4, h5, h6) {
    color: var(--ink);
    margin: 24px 0 12px;
    font-weight: 700;
  }
}

.empty-wrap {
  max-width: 480px;
  margin: 80px auto;
}

@media (max-width: 768px) {
  .article-shell {
    padding: 24px 20px 32px;
  }

  .cover-image {
    max-height: 260px;
  }
}
</style>
