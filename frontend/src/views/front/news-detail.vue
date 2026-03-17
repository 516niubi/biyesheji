<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import http from '../../utils/http'
import { getImageUrl } from '../../utils/system'
import { ElMessage } from 'element-plus'

const route = useRoute()
const loading = ref(false)
const newsDetail = ref<any>({})

// 获取新闻详情
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

// 格式化日期
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
  <div class="news-detail-container">
    <el-card v-loading="loading" class="detail-card">
      <template v-if="!loading && newsDetail.id">
        <!-- 标题居中 -->
        <div class="title-section">
          <h1 class="news-title">{{ newsDetail.title }}</h1>
        </div>
        
        <!-- 创建时间和浏览量 -->
        <div class="meta-info">
          <div class="meta-item">
            <span>{{ formatDate(newsDetail.createTime) }}</span>
          </div>
          <div class="meta-item">
            <span>浏览量：{{ newsDetail.viewCount || 0 }}</span>
          </div>
        </div>
        
        <!-- 封面图片 -->
        <div v-if="newsDetail.coverUrl" class="cover-section">
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
              <div class="image-error">
                <span>图片加载失败</span>
              </div>
            </template>
          </el-image>
        </div>
        
        <!-- 简介 -->
        <div v-if="newsDetail.intro" class="intro-section">
          <h3 class="section-title">简介</h3>
          <p class="intro-text">{{ newsDetail.intro }}</p>
        </div>
        
        <!-- 详细内容 -->
        <div v-if="newsDetail.content" class="content-section">
          <h3 class="section-title">详细内容</h3>
          <div class="content-text" v-html="newsDetail.content"></div>
        </div>
      </template>
      
      <!-- 空状态 -->
      <el-empty v-else-if="!loading" description="暂无数据" />
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.news-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  
  .detail-card {
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    
    .title-section {
      text-align: center;
      margin-bottom: 30px;
      padding-bottom: 20px;
      border-bottom: 2px solid #f0f0f0;
      
      .news-title {
        font-size: 32px;
        font-weight: bold;
        color: #2c3e50;
        margin: 0;
        line-height: 1.4;
      }
    }
    
    .meta-info {
      display: flex;
      justify-content: center;
      gap: 40px;
      margin-bottom: 30px;
      padding: 15px;
      background: #f8f9fa;
      border-radius: 8px;
      
      .meta-item {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #666;
        font-size: 14px;
        
        .el-icon {
          color: #409eff;
        }
      }
    }
    
    .cover-section {
      margin-bottom: 30px;
      text-align: center;
      
      .cover-image {
        max-width: 100%;
        max-height: 400px;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }
      
      .image-error {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 200px;
        background: #f5f5f5;
        border-radius: 8px;
        color: #999;
        
        .el-icon {
          font-size: 48px;
          margin-bottom: 10px;
        }
      }
    }
    
    .intro-section,
    .content-section {
      margin-bottom: 30px;
      
      .section-title {
        font-size: 20px;
        font-weight: 600;
        color: #2c3e50;
        margin-bottom: 15px;
        padding-bottom: 8px;
        border-bottom: 1px solid #e0e0e0;
      }
      
      .intro-text {
        font-size: 16px;
        line-height: 1.8;
        color: #555;
        text-align: justify;
        margin: 0;
      }
      
      .content-text {
        font-size: 15px;
        line-height: 1.8;
        color: #333;
        text-align: justify;
        
        :deep(img) {
          max-width: 100%;
          height: auto;
          border-radius: 4px;
          margin: 10px 0;
        }
        
        :deep(p) {
          margin-bottom: 15px;
        }
        
        :deep(h1, h2, h3, h4, h5, h6) {
          margin: 20px 0 10px 0;
          color: #2c3e50;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .news-detail-container {
    padding: 10px;
    
    .detail-card {
      .title-section {
        .news-title {
          font-size: 24px;
        }
      }
      
      .meta-info {
        flex-direction: column;
        gap: 15px;
        text-align: center;
      }
      
      .cover-section {
        .cover-image {
          max-height: 250px;
        }
      }
    }
  }
}
</style>