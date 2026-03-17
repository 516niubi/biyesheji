<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElCarousel, ElCarouselItem, ElCard, ElButton, ElRow, ElCol, ElImage, ElTag } from 'element-plus'
import { useRouter } from 'vue-router'
import http from '../../utils/http'
import { getImageUrl } from '../../utils/system'
import HeritageCard from '../../components/HeritageCard.vue'
import ActivityCard from '../../components/ActivityCard.vue'

const router = useRouter()

// 轮播图数据
const bannerList = ref([])
// 热门非遗文物数据
const hotHeritageList = ref([])
// 热门活动数据
const hotActivityList = ref([])
// 热门资讯数据
const hotNewsList = ref([])
// 加载状态
const loading = ref(false)

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

// 获取热门非遗文物（按浏览量降序，分页）
const getHotHeritageList = async () => {
  try {
    const res = await http.get('/culturalHeritage/hot?pageNum=1&pageSize=8&orderBy=view_count&orderType=desc')
    if (res.code === 200) {
      hotHeritageList.value = res.data?.records.map(t => {
        t.coverImage = getImageUrl(t.coverImage)
        return t
      })
    }
  } catch (error) {
    console.error('获取热门非遗文物失败:', error)
  }
}

// 获取热门活动（按浏览量降序，分页）
const getHotActivityList = async () => {
  try {
    const res = await http.get('/activity/hot?pageNum=1&pageSize=6&orderBy=view_count&orderType=desc')
    if (res.code === 200) {
      hotActivityList.value = res.data?.records || []
    }
  } catch (error) {
    console.error('获取热门活动失败:', error)
  }
}

// 获取热门资讯（按浏览量降序，分页）
const getHotNewsList = async () => {
  try {
    const res = await http.get('/article/hot?pageNum=1&pageSize=6&orderBy=view_count&orderType=desc')
    if (res.code === 200) {
      hotNewsList.value = res.data?.records.map(t => {
        t.coverUrl = getImageUrl(t.coverUrl)
        return t
      });
    }
  } catch (error) {
    console.error('获取热门资讯失败:', error)
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
  return `2025-${months[date.getMonth()]}`
}

// 初始化数据
const initData = async () => {
  loading.value = true
  try {
    await Promise.all([
      getBannerList(),
      getHotHeritageList(),
      getHotActivityList(),
      getHotNewsList()
    ])
  } finally {
    loading.value = false
  }
}

// 跳转到资讯详情
const goToNewsDetail = (id) => {
  router.push({
    path: '/front/newsDetail',
    query: {
      id
    }
  })
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
    <!-- 轮播图区域 -->
    <div class="banner-section">
      <el-carousel 
        v-if="bannerList.length > 0" 
        height="450px" 
        :interval="4000" 
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
              <h2 class="banner-title">{{ item.title }}</h2>
              <p class="banner-desc">{{ item.description }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
      <div v-else class="banner-placeholder">
        <div class="placeholder-content">
          <h2>非遗传承保护系统</h2>
          <p>传承文化瑰宝，保护非物质文化遗产</p>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 热门非遗文物 -->
      <section class="section">
        <div class="section-header">
          <div class="section-title-wrapper">
            <div class="title-decoration left"></div>
            <h2 class="section-title">
              热门非遗文物
            </h2>
            <div class="title-decoration right"></div>
          </div>
          <el-button type="primary" class="more-btn" @click="goToList('/front/heritage')">
            查看更多
          </el-button>
        </div>
        <div class="heritage-grid">
          <HeritageCard 
            v-for="item in hotHeritageList" 
            :key="item.id"
            :heritage="item"
          />
        </div>
      </section>

      <!-- 热门活动 -->
      <section class="section">
        <div class="section-header">
          <div class="section-title-wrapper">
            <div class="title-decoration left"></div>
            <h2 class="section-title">
              最新活动
            </h2>
            <div class="title-decoration right"></div>
          </div>
          <el-button type="primary" class="more-btn" @click="goToList('/front/activity')">
            更多
          </el-button>
        </div>
        <div class="activity-layout">
          <!-- 主要活动（左侧大图） -->
          <div class="main-activity-wrapper">
            <ActivityCard 
              v-if="hotActivityList.length > 0"
              :activity="hotActivityList[0]"
              size="large"
            />
          </div>
          
          <!-- 其他活动（右侧小图列表） -->
          <div class="activity-list">
            <ActivityCard 
              v-for="(item, index) in hotActivityList.slice(1, 4)" 
              :key="item.id"
              :activity="item"
              size="small"
            />
          </div>
        </div>
      </section>

      <!-- 热门资讯 -->
      <section class="section">
        <div class="section-header">
          <div class="section-title-wrapper">
            <div class="title-decoration left"></div>
            <h2 class="section-title">
              最新资讯
            </h2>
            <div class="title-decoration right"></div>
          </div>
          <el-button type="primary" class="more-btn" @click="goToList('/front/news')">
            更多
          </el-button>
        </div>
        <div class="news-layout">
          <!-- 主要资讯（左侧大图） -->
          <div class="main-news" v-if="hotNewsList.length > 0" @click="goToNewsDetail(hotNewsList[0].id)">
            <el-image 
              :src="hotNewsList[0].coverUrl" 
              :alt="hotNewsList[0].title"
              class="main-news-image"
              fit="cover"
            >
              <template #error>
                <div class="image-error">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </template>
            </el-image>
            <div class="main-news-content">
              <h3 class="news-title">{{ hotNewsList[0].title }}</h3>
              <div class="news-date">{{ hotNewsList[0].intro }}</div>
            </div>
          </div>
          
          <!-- 其他资讯（右侧列表） -->
          <div class="news-list">
            <div 
              v-for="(item, index) in hotNewsList.slice(1, 5)" 
              :key="item.id" 
              class="news-item"
              @click="goToNewsDetail(item.id)"
            >
              <div class="news-cover-box">
                <img :src="item.coverUrl" alt="">
              </div>
              <div class="news-content">
                <h4 class="news-title">{{ item.title }}</h4>
                <p class="news-summary">{{ item.intro }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
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
}

.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  margin: 0 auto;
}

// 轮播图样式
.banner-section {
  margin: 0 auto 60px;
  width: 100%;
  
  .banner-item {
    height: 500px;
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    position: relative;
    cursor: pointer;
    border-radius: 0 0 24px 24px;
    overflow: hidden;
    
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: linear-gradient(135deg, rgba(52, 152, 219, 0.3) 0%, rgba(231, 76, 60, 0.3) 100%);
    }
    
    .banner-content {
      position: absolute;
      bottom: 80px;
      left: 80px;
      color: white;
      z-index: 2;
      
      .banner-title {
        font-size: 48px;
        font-weight: 700;
        margin-bottom: 16px;
        text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.5);
        background: linear-gradient(45deg, #fff, #f0f0f0);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
      
      .banner-desc {
        font-size: 18px;
        opacity: 0.95;
        text-shadow: 1px 1px 4px rgba(0, 0, 0, 0.5);
        font-weight: 500;
      }
    }
  }
  
  .banner-placeholder {
    height: 500px;
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    border-radius: 0 0 24px 24px;
    
    .placeholder-content {
      text-align: center;
      
      h2 {
        font-size: 48px;
        margin-bottom: 16px;
        text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.3);
        font-weight: 700;
      }
      
      p {
        font-size: 18px;
        opacity: 0.9;
        font-weight: 500;
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
}

// 区块样式
.section {
  margin-bottom: 80px;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 40px;
    
    .section-title-wrapper {
      display: flex;
      align-items: center;
      justify-content: center;
      flex: 1;
      position: relative;
      
      .title-decoration {
        height: 2px;
        background: linear-gradient(90deg, transparent 0%, var(--primary-color) 50%, transparent 100%);
        flex: 1;
        
        &.left {
          margin-right: 30px;
        }
        
        &.right {
          margin-left: 30px;
        }
      }
      
      .section-title {
        font-size: 32px;
        font-weight: 700;
        color: var(--primary-color);
        display: flex;
        align-items: center;
        gap: 12px;
        text-align: center;
        white-space: nowrap;
        background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
        background-clip: text;
        position: relative;
        
        &::before {
          content: '';
          position: absolute;
          bottom: -8px;
          left: 50%;
          transform: translateX(-50%);
          width: 60px;
          height: 3px;
          background: linear-gradient(90deg, var(--primary-color) 0%, var(--secondary-color) 100%);
          border-radius: 2px;
        }
        
        .title-icon {
          font-size: 28px;
          filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
        }
      }
    }
    
    .more-btn {
      // background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
      border: none;
      border-radius: 25px;
      padding: 12px 24px;
      font-weight: 600;
      box-shadow: var(--shadow-light);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: var(--shadow-hover);
      }
    }
  }
}

// 非遗文物卡片样式
.heritage-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 32px;
  padding: 20px 0;
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

// 活动布局样式
.activity-layout {
  display: flex;
  gap: 40px;
  padding: 20px 0;
  
  .main-activity-wrapper {
    flex: 1.2;
  }
  
  .activity-list {
    flex: 0.8;
    display: flex;
    flex-direction: column;
    gap: 16px;
    
    .activity-item {
      display: flex;
      background: var(--background-white);
      border-radius: 16px;
      overflow: hidden;
      box-shadow: var(--shadow-light);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      cursor: pointer;
      border: 1px solid var(--border-light);
      
      &:hover {
        transform: translateX(8px);
        box-shadow: var(--shadow-hover);
        border-color: var(--primary-color);
      }
      
      .activity-image {
        width: 90px;
        height: 90px;
        object-fit: cover;
        background: linear-gradient(135deg, var(--background-light) 0%, #e9ecef 100%);
        flex-shrink: 0;
      }
      
      .activity-content {
        padding: 16px 20px;
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        
        .activity-title {
          font-size: 15px;
          font-weight: 600;
          color: var(--text-primary);
          line-height: 1.5;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
          margin-bottom: 10px;
        }
        
        .activity-date {
          color: var(--primary-color);
          font-size: 13px;
          font-weight: 600;
        }
      }
    }
  }
}

// 资讯布局样式
.news-layout {
  display: flex;
  gap: 40px;
  padding: 20px 0;
  
  .main-news {
    flex: 1.2;
    position: relative;
    cursor: pointer;
    border-radius: 20px;
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: var(--shadow-light);
    border: 1px solid var(--border-light);
    
    &:hover {
      transform: translateY(-8px);
      box-shadow: var(--shadow-hover);
      border-color: var(--primary-color);
    }
    
    .main-news-image {
      width: 100%;
      height: 280px;
      object-fit: cover;
      background: linear-gradient(135deg, var(--background-light) 0%, #e9ecef 100%);
    }
    
    // .main-news-overlay {
    //   position: absolute;
    //   bottom: 0;
    //   left: 0;
    //   right: 0;
    //   background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
    //   padding: 40px 24px 24px;
    //   color: white;
      
    //   .news-date {
    //     position: absolute;
    //     top: -30px;
    //     right: 24px;
    //     background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
    //     color: white;
    //     padding: 8px 16px;
    //     border-radius: 20px;
    //     font-size: 13px;
    //     font-weight: 600;
    //     box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    //   }
      
    //   .news-title {
    //     font-size: 22px;
    //     font-weight: 700;
    //     margin: 0;
    //     line-height: 1.4;
    //     text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
    //   }
    // }
    
    .main-news-content {
      padding: 20px;
      
      .news-title {
        font-size: 18px;
        font-weight: bold;
        color: #2c3e50;
        margin-bottom: 10px;
        line-height: 1.4;
      }
      
      .news-date {
        font-size: 14px;
        color: #95a5a6;
      }
    }
  }
  
  .news-list {
    flex: 0.8;
    display: flex;
    flex-direction: column;
    gap: 20px;
    
    .news-item {
      display: flex;
      background: var(--background-white);
      border-radius: 16px;
      overflow: hidden;
      box-shadow: var(--shadow-light);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      cursor: pointer;
      border: 1px solid var(--border-light);
      
      &:hover {
        transform: translateX(8px);
        box-shadow: var(--shadow-hover);
        border-color: var(--primary-color);
      }
      
      .news-image {
        width: 90px;
        height: 90px;
        object-fit: cover;
        background: linear-gradient(135deg, var(--background-light) 0%, #e9ecef 100%);
        flex-shrink: 0;
      }
      
      .news-cover-box {
        flex-shrink: 0;
        width: 50px;
        text-align: center;
        display: flex;
        align-items: center;
        
        img {
          width: 55px;
          height: 55px;
          overflow: hidden;
          background-size: cover;
          background-position: center;
        }
      }
      
      .news-content {
        flex: 1;
        padding: 16px 20px;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        
        .news-title {
          font-size: 15px;
          font-weight: 600;
          color: var(--text-primary);
          line-height: 1.5;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
          margin-bottom: 10px;
        }
        
        .news-date {
          color: var(--primary-color);
          font-size: 13px;
          font-weight: 600;
        }
        
        .news-summary {
          font-size: 12px;
          color: #7f8c8d;
          line-height: 1.4;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
      }
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
    padding: 0 20px;
  }
  
  .activity-layout,
  .news-layout {
    gap: 30px;
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
    margin-bottom: 60px;
    
    .section-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
      margin-bottom: 30px;
      
      .section-title-wrapper {
        .section-title {
          font-size: 28px;
        }
        
        .title-decoration {
          &.left {
            margin-right: 20px;
          }
          
          &.right {
            margin-left: 20px;
          }
        }
      }
    }
  }
  
  .heritage-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  
  // 活动布局响应式
  .activity-layout {
    flex-direction: column;
    gap: 30px;
    
    .main-activity-wrapper {
      order: 1;
    }
    
    .activity-list {
      order: 2;
      display: grid;
      grid-template-columns: 1fr;
      gap: 16px;
    }
  }
  
  // 资讯布局响应式
  .news-layout {
    flex-direction: column;
    gap: 30px;
    
    .main-news {
      .main-news-image {
        height: 220px;
      }
    }
    
    .news-list {
      display: grid;
      grid-template-columns: 1fr;
      gap: 16px;
      
      .news-item {
        .news-image {
          width: 80px;
          height: 80px;
        }
        
        .news-content {
          padding: 12px 16px;
          
          .news-title {
            font-size: 14px;
          }
          
          .news-date {
            font-size: 12px;
          }
          
          .news-summary {
            font-size: 11px;
          }
        }
      }
    }
  }
}

@media (max-width: 480px) {
  .banner-section {
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