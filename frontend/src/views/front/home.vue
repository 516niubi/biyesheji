<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { ElCarousel, ElCarouselItem, ElCard, ElButton, ElRow, ElCol, ElImage, ElTag } from 'element-plus'
import { Bell, Collection, Calendar, Reading } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import http from '../../utils/http'
import { getImageUrl } from '../../utils/system'
import HeritageCard from '../../components/HeritageCard.vue'
import ActivityCard from '../../components/ActivityCard.vue'
import NewsCard from '../../components/NewsCard.vue'

const router = useRouter()

// 轮播图数据
const bannerList = ref([])
// 热门非遗文物数据
const hotHeritageList = ref([])
// 热门活动数据
const hotActivityList = ref([])
// 热门资讯数据
const hotNewsList = ref([])
// 公告数据
const noticeList = ref([])
const noticeDialogVisible = ref(false)
const currentNotice = ref<any>(null)
const latestHeritageList = computed(() => hotHeritageList.value.slice(0, 6))
const latestActivityList = computed(() => hotActivityList.value.slice(0, 6))
const latestNewsList = computed(() => hotNewsList.value.slice(0, 6))
// 加载状态
const loading = ref(false)

const getLatestTime = (item) => {
  return new Date(item?.updateTime || item?.createTime || 0).getTime()
}

const sortByLatestTime = (list = []) => {
  return [...list].sort((a, b) => getLatestTime(b) - getLatestTime(a))
}

// 获取轮播图数据（使用获取全部的接口）
const getBannerList = async () => {
  try {
    const res = await http.get('/banner/list')
    if (res.code === 200) {
      bannerList.value = (res.data || []).map(t => {
        t.url = getImageUrl(t.url)
        return t
      });
    }
  } catch (error) {
    console.error('获取轮播图失败:', error)
  }
}

// 获取非遗文物（按浏览量降序，展示 6 条）
const getHotHeritageList = async () => {
  try {
    const res = await http.get(
      '/culturalHeritage/hot?pageNum=1&pageSize=6&orderBy=view_count&orderType=desc'
    )
    if (res.code === 200) {
      hotHeritageList.value = (res.data?.records || []).map((t) => {
        t.coverImage = getImageUrl(t.coverImage)
        return t
      })
    }
  } catch (error) {
    console.error('获取热门非遗文物失败:', error)
  }
}

// 获取活动（按最新发布时间/修改时间，展示6条）
const getHotActivityList = async () => {
  try {
    const res = await http.get('/activity/page?pageNum=1&pageSize=20')
    if (res.code === 200) {
      hotActivityList.value = sortByLatestTime(res.data?.records || []).slice(0, 6)
    }
  } catch (error) {
    console.error('获取热门活动失败:', error)
  }
}

// 获取资讯（按最新发布时间/修改时间，展示6条）
const getHotNewsList = async () => {
  try {
    const res = await http.get('/article/page?pageNum=1&pageSize=20')
    if (res.code === 200) {
      hotNewsList.value = sortByLatestTime(res.data?.records || []).slice(0, 6)
    }
  } catch (error) {
    console.error('获取热门资讯失败:', error)
  }
}

const getLatestNoticeList = async () => {
  try {
    const res = await http.get('/notice/page?pageNum=1&pageSize=10')
    if (res.code === 200) {
      noticeList.value = res.data?.records || []
    }
  } catch (error) {
    console.error('获取公告失败:', error)
  }
}

// 跳转到列表页
const goToList = (path) => {
  router.push(path)
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 获取日期中的天
const getDay = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return String(date.getDate()).padStart(2, '0')
}

// 获取日期中的月份
const getMonth = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const months = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']
  return `${date.getFullYear()}-${months[date.getMonth()]}`
}

// 初始化数据
const initData = async () => {
  loading.value = true
  try {
    await Promise.all([
      getBannerList(),
      getHotHeritageList(),
      getHotActivityList(),
      getHotNewsList(),
      getLatestNoticeList()
    ])
  } finally {
    loading.value = false
  }
}

const openNoticeDetail = (notice) => {
  currentNotice.value = notice
  noticeDialogVisible.value = true
}

const formatNoticeDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.split(' ')[0]
}

const getNoticeDisplayTime = (notice) => {
  return notice?.updateTime || notice?.createTime || ''
}

onMounted(() => {
  initData()
})

// 导出方法供模板使用
defineExpose({
  formatDate,
  getDay,
  getMonth
})
</script>

<template>
  <div class="home-container">
    <div class="home-hero-area">
      <!-- 轮播图区域 -->
      <div class="banner-section">
        <el-carousel 
          v-if="bannerList.length > 0" 
          class="home-carousel"
          height="480px" 
          :interval="5000" 
          indicator-position="outside"
          type="card"
          arrow="hover"
        >
          <el-carousel-item v-for="item in bannerList" :key="item.id">
            <div 
              class="banner-item" 
              :style="{ backgroundImage: `url(${item.url})` }"
            >
              <div class="banner-content">
                <span class="banner-kicker">非遗文化数字化保护平台</span>
                <h2 class="banner-title">{{ item.title }}</h2>
                <p class="banner-desc">{{ item.description }}</p>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
        <div v-else class="banner-placeholder">
          <div class="placeholder-scrim" />
          <div class="placeholder-content">
            <span class="banner-kicker">非遗文化数字化保护平台</span>
            <h2>传承 · 记录 · 共享</h2>
            <p>汇聚非遗文物、活动与资讯，用数字技术延续文化记忆</p>
          </div>
        </div>
      </div>
      <div class="hero-bottom-deco" aria-hidden="true" />
    </div>

    <!-- 亮点条 -->
    <section class="home-intro" aria-label="平台亮点">
      <div class="home-intro-inner">
        <div class="home-intro-item">
          <el-icon class="home-intro-icon" :size="20"><Collection /></el-icon>
          <span>文物数字档案</span>
        </div>
        <span class="home-intro-dot" />
        <div class="home-intro-item">
          <el-icon class="home-intro-icon" :size="20"><Calendar /></el-icon>
          <span>活动与动态</span>
        </div>
        <span class="home-intro-dot" />
        <div class="home-intro-item">
          <el-icon class="home-intro-icon" :size="20"><Reading /></el-icon>
          <span>资讯与传播</span>
        </div>
      </div>
    </section>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="content-layout">
        <div class="content-left">
      <!-- 热门非遗文物 -->
      <section class="section">
        <div class="section-header">
          <div class="section-heading-block">
            <div class="section-title-wrapper">
              <div class="title-decoration left"></div>
              <h2 class="section-title">
                热门非遗文物
              </h2>
              <div class="title-decoration right"></div>
            </div>
            <p class="section-sub">按浏览量排序，展示近期最受关注的非遗档案</p>
          </div>
          <el-button class="more-btn" @click="goToList('/front/heritage')">
            查看更多
          </el-button>
        </div>
        <div class="heritage-grid">
          <HeritageCard 
            v-for="item in latestHeritageList" 
            :key="item.id"
            :heritage="item"
          />
        </div>
      </section>

      <!-- 热门活动 -->
      <section class="section">
        <div class="section-header">
          <div class="section-heading-block">
            <div class="section-title-wrapper">
              <div class="title-decoration left"></div>
              <h2 class="section-title">
                最新活动
              </h2>
              <div class="title-decoration right"></div>
            </div>
            <p class="section-sub">线下体验、主题展览与传承活动，按发布时间更新</p>
          </div>
          <el-button class="more-btn" @click="goToList('/front/activity')">
            更多
          </el-button>
        </div>
        <div class="heritage-grid home-activity-news-grid">
          <ActivityCard
            v-for="item in latestActivityList"
            :key="item.id"
            :activity="item"
            size="large"
          />
        </div>
      </section>

      <!-- 热门资讯 -->
      <section class="section">
        <div class="section-header">
          <div class="section-heading-block">
            <div class="section-title-wrapper">
              <div class="title-decoration left"></div>
              <h2 class="section-title">
                最新资讯
              </h2>
              <div class="title-decoration right"></div>
            </div>
            <p class="section-sub">政策解读、保护故事与行业动态，图文同步呈现</p>
          </div>
          <el-button class="more-btn" @click="goToList('/front/news')">
            更多
          </el-button>
        </div>
        <div class="heritage-grid home-activity-news-grid">
          <NewsCard
            v-for="item in latestNewsList"
            :key="item.id"
            :news="item"
            size="large"
          />
        </div>
      </section>
        </div>
        <aside class="notice-board">
          <div class="notice-board-accent" aria-hidden="true" />
          <div class="notice-head">
            <div class="notice-head-title">
              <el-icon class="notice-head-icon"><Bell /></el-icon>
              <h3>公告栏</h3>
            </div>
            <span class="notice-badge">最新 10 条</span>
          </div>
          <div v-if="noticeList.length" class="notice-list">
            <a
              v-for="item in noticeList"
              :key="item.id"
              class="notice-item"
              href="javascript:void(0)"
              @click="openNoticeDetail(item)"
            >
              <div class="notice-date">
                <strong>{{ formatNoticeDate(getNoticeDisplayTime(item)).slice(5) }}</strong>
                <span>{{ formatNoticeDate(getNoticeDisplayTime(item)).slice(0, 4) }}</span>
              </div>
              <div class="notice-text">
                <h4>{{ item.title }}</h4>
                <p>{{ item.content }}</p>
              </div>
            </a>
          </div>
          <el-empty v-else description="暂无公告" :image-size="80" />
        </aside>
      </div>
    </div>

    <el-dialog
      v-model="noticeDialogVisible"
      title="公告详情"
      width="760px"
      class="notice-detail-dialog"
    >
      <div v-if="currentNotice" class="notice-detail">
        <div class="notice-detail-head">
          <h2>{{ currentNotice.title }}</h2>
          <div class="notice-meta">
            <span>发布人：{{ currentNotice.publishName || '系统' }}</span>
            <span>发布时间：{{ currentNotice.createTime }}</span>
            <span>最后更新：{{ currentNotice.updateTime || currentNotice.createTime }}</span>
          </div>
        </div>
        <el-image
          v-if="currentNotice.coverUrl"
          :src="getImageUrl(currentNotice.coverUrl)"
          fit="cover"
          class="notice-image"
          :preview-src-list="[getImageUrl(currentNotice.coverUrl)]"
          :preview-teleported="true"
        />
        <div class="notice-content-card">
          <p class="notice-content">{{ currentNotice.content }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
// 系统主题色定义
:root {
  --text-primary: #2c3e50;
  --text-secondary: #7f8c8d;
  --background-light: #f8f9fa;
  --background-white: #ffffff;
  --border-light: rgba(0, 0, 0, 0.06);
  --shadow-light: 0 2px 12px rgba(0, 0, 0, 0.08);
  --shadow-hover: 0 12px 32px rgba(0, 0, 0, 0.15);
  --primary-color: #c53d2e;
  --secondary-color: #6e1f1f;
}

.home-container {
  min-height: 100vh;
  margin: 0 auto;
  background-color: #f3ebe8;
  background-image:
    radial-gradient(ellipse 100% 60% at 50% -15%, rgba(197, 61, 46, 0.12) 0%, transparent 55%),
    linear-gradient(180deg, #fdf9f7 0%, #f5ebe8 35%, #efe5e1 100%);
}

.home-hero-area {
  position: relative;
  margin-bottom: 0;
}

.hero-bottom-deco {
  height: 28px;
  margin-top: -1px;
  background: linear-gradient(180deg, rgba(197, 61, 46, 0.12) 0%, transparent 100%);
  pointer-events: none;
}

.home-intro {
  max-width: 1200px;
  margin: -8px auto 28px;
  padding: 0 20px;
  position: relative;
  z-index: 3;
}

.home-intro-inner {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 12px 20px;
  padding: 16px 22px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.92) 0%, rgba(255, 251, 248, 0.95) 100%);
  border: 1px solid rgba(168, 44, 40, 0.12);
  border-radius: 999px;
  box-shadow: 0 12px 36px rgba(62, 18, 18, 0.08), 0 1px 0 rgba(255, 255, 255, 0.9) inset;
}

.home-intro-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #4a3f3b;
  letter-spacing: 0.02em;
}

.home-intro-icon {
  color: var(--primary-color);
  opacity: 0.9;
}

.home-intro-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: linear-gradient(135deg, #c53d2e, #e0786a);
  opacity: 0.55;
  flex-shrink: 0;
}

// 轮播图样式
.banner-section {
  margin: 0 auto;
  width: 100%;
  padding: 0 12px;
  max-width: 1280px;

  :deep(.home-carousel.el-carousel--card) {
    --el-carousel-indicator-padding-vertical: 14px;
    /* 关闭 Element Plus 卡片轮播默认白色蒙层，避免侧卡与主图发灰、偏色 */
    .el-carousel__mask {
      opacity: 0 !important;
      background-color: transparent !important;
      pointer-events: none;
    }
    .el-carousel__indicators--outside button {
      background-color: rgba(197, 61, 46, 0.25);
      opacity: 1;
    }
    .el-carousel__indicator.is-active button {
      background-color: var(--primary-color);
    }
    .el-carousel__arrow {
      background: rgba(255, 255, 255, 0.92);
      color: var(--primary-color);
      box-shadow: 0 4px 16px rgba(62, 18, 18, 0.15);
    }
  }

  .banner-item {
    height: 480px;
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    position: relative;
    cursor: pointer;
    border-radius: 20px;
    overflow: hidden;
    box-shadow: 0 20px 50px rgba(45, 28, 18, 0.18);
    border: 1px solid rgba(255, 255, 255, 0.35);

    .banner-content {
      position: absolute;
      bottom: 56px;
      left: 48px;
      right: 48px;
      max-width: 640px;
      color: white;
      z-index: 2;

      .banner-kicker {
        display: inline-block;
        font-size: 12px;
        font-weight: 700;
        letter-spacing: 0.14em;
        text-transform: uppercase;
        color: rgba(255, 255, 255, 0.98);
        margin-bottom: 12px;
        padding: 6px 12px;
        border-radius: 999px;
        background: rgba(0, 0, 0, 0.35);
        border: 1px solid rgba(255, 255, 255, 0.28);
        text-shadow: 0 1px 3px rgba(0, 0, 0, 0.65);
      }

      .banner-title {
        font-size: clamp(28px, 4.2vw, 46px);
        font-weight: 800;
        margin: 0 0 14px;
        line-height: 1.2;
        letter-spacing: -0.02em;
        text-shadow:
          0 1px 2px rgba(0, 0, 0, 0.85),
          0 4px 20px rgba(0, 0, 0, 0.55),
          0 12px 40px rgba(0, 0, 0, 0.35);
        color: #fff;
      }

      .banner-desc {
        font-size: 16px;
        line-height: 1.65;
        margin: 0;
        color: rgba(255, 255, 255, 0.96);
        text-shadow:
          0 1px 2px rgba(0, 0, 0, 0.8),
          0 3px 14px rgba(0, 0, 0, 0.45);
        font-weight: 500;
        display: -webkit-box;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
    }
  }

  .banner-placeholder {
    height: 480px;
    margin: 0 auto;
    max-width: 1240px;
    border-radius: 20px;
    position: relative;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    background: linear-gradient(135deg, #6e1f1f 0%, #a82c28 38%, #c53d2e 72%, #d67a6e 100%);
    box-shadow: 0 20px 50px rgba(62, 18, 18, 0.22);

    .placeholder-scrim {
      position: absolute;
      inset: 0;
      background:
        radial-gradient(ellipse 80% 60% at 20% 80%, rgba(255, 255, 255, 0.08) 0%, transparent 50%),
        radial-gradient(ellipse 60% 50% at 90% 20%, rgba(255, 200, 180, 0.15) 0%, transparent 45%);
      pointer-events: none;
    }

    .placeholder-content {
      text-align: center;
      position: relative;
      z-index: 1;
      padding: 24px;

      .banner-kicker {
        display: inline-block;
        font-size: 12px;
        font-weight: 700;
        letter-spacing: 0.14em;
        color: rgba(255, 236, 232, 0.95);
        margin-bottom: 16px;
        padding: 6px 14px;
        border-radius: 999px;
        background: rgba(0, 0, 0, 0.12);
        border: 1px solid rgba(255, 255, 255, 0.2);
      }

      h2 {
        font-size: clamp(30px, 5vw, 48px);
        margin: 0 0 16px;
        text-shadow: 0 4px 20px rgba(0, 0, 0, 0.25);
        font-weight: 800;
        letter-spacing: -0.02em;
      }

      p {
        font-size: 17px;
        opacity: 0.94;
        font-weight: 500;
        line-height: 1.65;
        max-width: 420px;
        margin: 0 auto;
      }
    }
  }
}

// 主要内容区域
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 2;
  padding: 0 20px 40px;
}

.content-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 24px;
  align-items: start;
}

.content-left {
  min-width: 0;
}

.notice-board {
  position: sticky;
  top: 92px;
  overflow: hidden;
  background: linear-gradient(180deg, #fffefb 0%, #fff 48%, #fffcfa 100%);
  border-radius: 20px;
  padding: 20px 18px 18px;
  border: 1px solid rgba(197, 61, 46, 0.12);
  box-shadow:
    0 14px 36px rgba(62, 18, 18, 0.08),
    0 1px 0 rgba(255, 255, 255, 0.95) inset;

  .notice-board-accent {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, transparent 0%, var(--primary-color) 35%, #e0786a 65%, transparent 100%);
    opacity: 0.85;
  }

  .notice-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 14px;
    padding-bottom: 12px;
    border-bottom: 1px solid rgba(197, 61, 46, 0.1);
  }

  .notice-head-title {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 0;

    .notice-head-icon {
      flex-shrink: 0;
      font-size: 22px;
      color: var(--primary-color);
      filter: drop-shadow(0 2px 4px rgba(197, 61, 46, 0.2));
    }

    h3 {
      font-size: 20px;
      font-weight: 800;
      letter-spacing: 0.04em;
      color: #3d2f2a;
      margin: 0;
    }
  }

  .notice-badge {
    flex-shrink: 0;
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.06em;
    color: var(--primary-color);
    padding: 5px 10px;
    border-radius: 999px;
    background: rgba(197, 61, 46, 0.08);
    border: 1px solid rgba(197, 61, 46, 0.18);
  }

  .notice-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .notice-item {
    display: flex;
    gap: 12px;
    padding: 12px 12px;
    border-radius: 14px;
    text-decoration: none;
    color: inherit;
    border: 1px solid rgba(197, 61, 46, 0.08);
    background: rgba(255, 255, 255, 0.65);
    transition: border-color 0.2s, background 0.2s, transform 0.2s, box-shadow 0.2s;

    &:hover {
      border-color: rgba(197, 61, 46, 0.28);
      background: linear-gradient(135deg, rgba(255, 251, 248, 0.98) 0%, rgba(255, 255, 255, 0.95) 100%);
      transform: translateX(3px);
      box-shadow: 0 6px 18px rgba(62, 18, 18, 0.07);
    }

    .notice-date {
      width: 58px;
      flex-shrink: 0;
      text-align: center;
      padding-right: 10px;
      border-right: 1px solid rgba(197, 61, 46, 0.12);

      strong {
        display: block;
        font-size: 20px;
        font-weight: 800;
        line-height: 1.15;
        color: var(--primary-color);
      }

      span {
        font-size: 11px;
        font-weight: 600;
        color: #8b7355;
        letter-spacing: 0.02em;
      }
    }

    .notice-text {
      min-width: 0;
      flex: 1;

      h4 {
        margin: 0 0 6px;
        font-size: 15px;
        color: #1f2937;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      p {
        margin: 0;
        font-size: 13px;
        color: #6b7280;
        line-height: 1.5;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
    }
  }
}

.notice-detail {
  .notice-detail-head {
    margin-bottom: 14px;
    padding-bottom: 12px;
    border-bottom: 1px solid #e5e7eb;
  }

  h2 {
    margin: 0 0 8px;
    font-size: 24px;
    color: #1f2937;
  }

  .notice-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 8px 14px;
    font-size: 13px;
    color: #6b7280;

    span {
      padding: 4px 8px;
      background: #f3f4f6;
      border-radius: 999px;
    }
  }

  .notice-image {
    width: 100%;
    max-height: 340px;
    border-radius: 10px;
    margin-bottom: 14px;
  }

  .notice-content-card {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    padding: 14px;
  }

  .notice-content {
    margin: 0;
    line-height: 1.9;
    color: #374151;
    white-space: pre-wrap;
  }
}

// 区块样式
.section {
  margin-bottom: 56px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(165deg, #ffffff 0%, #fffdfb 55%, #fffaf7 100%);
  border-radius: 20px;
  padding: 28px 28px 8px;
  border: 1px solid rgba(197, 61, 46, 0.08);
  box-shadow:
    0 12px 32px rgba(62, 18, 18, 0.07),
    0 1px 0 rgba(255, 255, 255, 0.9) inset;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, transparent 0%, rgba(197, 61, 46, 0.35) 40%, rgba(224, 120, 106, 0.45) 60%, transparent 100%);
    pointer-events: none;
  }
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 20px;
    margin-bottom: 22px;

    .more-btn {
      flex-shrink: 0;
      border-radius: 999px;
      padding: 10px 22px;
      font-weight: 600;
      border: none !important;
      color: #fff !important;
      background: linear-gradient(135deg, #a82c28 0%, #d0453c 100%) !important;
      box-shadow: 0 8px 20px rgba(110, 26, 26, 0.28);
      transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.3s, filter 0.3s;

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 10px 24px rgba(110, 26, 26, 0.34);
        filter: brightness(1.03);
      }
    }
  }

  .section-heading-block {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .section-sub {
    margin: 0;
    max-width: 520px;
    font-size: 14px;
    line-height: 1.65;
    color: #6b5c55;
    letter-spacing: 0.02em;
  }
  
  .section-title-wrapper {
    display: flex;
    align-items: center;
    justify-content: flex-start;
    flex-wrap: wrap;
    position: relative;
    gap: 12px;
    
    .title-decoration {
      height: 2px;
      width: 32px;
      border-radius: 2px;
      background: linear-gradient(90deg, rgba(197, 61, 46, 0.2) 0%, var(--primary-color) 100%);
      
      &.right {
        background: linear-gradient(90deg, var(--primary-color) 0%, rgba(197, 61, 46, 0.2) 100%);
      }
    }
    
    .section-title {
      font-size: clamp(22px, 3vw, 30px);
      font-weight: 800;
      display: flex;
      align-items: center;
      gap: 12px;
      text-align: center;
      white-space: nowrap;
      background: linear-gradient(135deg, #c53d2e 0%, #5c1818 100%);
      -webkit-background-clip: text;
      background-clip: text;
      -webkit-text-fill-color: transparent;
      color: transparent;
      position: relative;
      letter-spacing: 0.02em;
      
      &::after {
        content: '';
        position: absolute;
        bottom: -8px;
        left: 50%;
        transform: translateX(-50%);
        width: 56px;
        height: 3px;
        border-radius: 2px;
        background: linear-gradient(90deg, transparent, rgba(197, 61, 46, 0.5), transparent);
      }
      
      .title-icon {
        font-size: 28px;
        filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
      }
    }
  }
}

// 首页文物 / 活动 / 资讯：统一两列，最多 6 条（由数据 slice 控制）
.heritage-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 26px;
  padding: 12px 0 22px;
}

.home-activity-news-grid {
  :deep(.activity-card--large .card-image-wrapper) {
    height: 220px;
  }

  :deep(.news-card--large .news-image) {
    height: 220px;
  }
}

.heritage-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;
  
  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
  }
  
  .heritage-image {
    width: 100%;
    height: 200px;
  }
  
  .heritage-info {
    padding: 16px;
    
    .heritage-name {
      font-size: 16px;
      font-weight: bold;
      color: #2c3e50;
      margin-bottom: 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .heritage-category {
      font-size: 14px;
      color: #7f8c8d;
      margin-bottom: 12px;
    }
    
    .heritage-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
}

// 图片错误占位符
.image-error {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--background-light) 0%, #e9ecef 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
}

// 响应式设计
@media (max-width: 1024px) {
  .main-content {
    max-width: 95%;
    padding: 0 16px 24px;
  }

  .content-layout {
    grid-template-columns: 1fr;
  }

  .notice-board {
    position: static;
  }
}

@media (max-width: 768px) {
  .banner-item .banner-content {
    bottom: 40px;
    left: 40px;
    
    .banner-title {
      font-size: 32px;
    }
    
    .banner-desc {
      font-size: 16px;
    }
  }
  
  .banner-placeholder .placeholder-content h2 {
    font-size: 32px;
  }
  
  .main-content {
    padding: 0 15px;
    border-radius: 16px 16px 0 0;
  }
  
  .section {
    margin-bottom: 32px;
    padding: 20px 16px 4px;
    
    .section-header {
      flex-direction: column;
      align-items: stretch;
      gap: 14px;
      margin-bottom: 18px;
    }

    .section-heading-block {
      width: 100%;
    }
      
    .section-title-wrapper {
      width: 100%;
      justify-content: center;
      .section-title {
        font-size: 24px;
      }
      
      .title-decoration {
        &.left {
          margin-right: 0;
        }
        
        &.right {
          margin-left: 0;
        }
      }
    }

    .section-sub {
      max-width: none;
      text-align: center;
    }

    .section-header .more-btn {
      align-self: center;
    }
  }
  
  .heritage-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}

@media (max-width: 480px) {
  .banner-section {
    padding: 0 10px;

    :deep(.home-carousel.el-carousel) {
      height: 300px !important;
    }

    :deep(.home-carousel .el-carousel__container) {
      height: 300px !important;
    }

    .banner-item,
    .banner-placeholder {
      height: 300px;
      
      .banner-content,
      .placeholder-content {
        bottom: 40px;
        left: 40px;
        
        .banner-title,
        h2 {
          font-size: 32px;
        }
        
        .banner-desc,
        p {
          font-size: 16px;
        }
      }
    }
  }

  .home-intro {
    margin-bottom: 20px;
    padding: 0 12px;
  }

  .home-intro-inner {
    border-radius: 16px;
    padding: 14px 16px;
    justify-content: flex-start;
  }
  
  .section {
    .section-header {
      .section-title-wrapper {
        .section-title {
          font-size: 24px;
        }
      }
    }
  }
}
</style>