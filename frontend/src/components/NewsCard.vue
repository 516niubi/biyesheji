<script setup lang="ts">
import { computed } from 'vue'
import { ElImage } from 'element-plus'
import { getImageUrl } from '../utils/system'
import { useRouter } from 'vue-router'
import PublisherInheritorRow from './PublisherInheritorRow.vue'

// 定义 props
interface NewsItem {
  id: number
  title: string
  intro: string
  coverUrl: string
  createTime?: string
  viewCount?: number
  publisherName?: string
  publisherAvatar?: string
  creatorId?: number | null
}

interface Props {
  news: NewsItem
  size?: 'large' | 'small'
}

const props = withDefaults(defineProps<Props>(), {
  size: 'large'
})

const router = useRouter()

// 处理点击事件
const handleClick = () => {
  router.push({
    path: '/front/newsDetail',
    query: {
      id: props.news.id
    }
  })
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 计算样式类
const cardClass = computed(() => {
  return {
    'news-card': true,
    'news-card--large': props.size === 'large',
    'news-card--small': props.size === 'small'
  }
})
</script>

<template>
  <div :class="cardClass" @click="handleClick">
    <!-- 大卡片布局 -->
    <template v-if="size === 'large'">
      <el-image 
        :src="getImageUrl(news.coverUrl)" 
        :alt="news.title"
        class="news-image"
        fit="cover"
      >
        <template #error>
          <div class="image-error">
            <i class="el-icon-picture-outline"></i>
          </div>
        </template>
      </el-image>
      <div class="news-content">
        <h3 class="news-title">{{ news.title }}</h3>
        <p class="news-summary">{{ news.intro }}</p>
        <div class="news-meta">
          <div class="news-publisher-cell" @click.stop>
            <PublisherInheritorRow
              :inheritor-id="news.creatorId"
              :name="news.publisherName || '平台'"
              :avatar="news.publisherAvatar"
              :size="34"
              :show-caption="false"
            />
          </div>
          <div class="news-meta-rest">
            <span class="news-date" v-if="news.createTime">{{ formatDate(news.createTime) }}</span>
            <span class="news-views" v-if="news.viewCount">{{ news.viewCount }} 次浏览</span>
          </div>
        </div>
      </div>
    </template>

    <!-- 小卡片布局 -->
    <template v-else>
      <div class="news-cover-box">
        <img :src="getImageUrl(news.coverUrl)" :alt="news.title" />
      </div>
      <div class="news-content">
        <h4 class="news-title">{{ news.title }}</h4>
        <p class="news-summary">{{ news.intro }}</p>
        <div class="news-meta news-meta--small">
          <div class="news-publisher-cell" @click.stop>
            <PublisherInheritorRow
              :inheritor-id="news.creatorId"
              :name="news.publisherName || '平台'"
              :avatar="news.publisherAvatar"
              :size="28"
              :show-caption="false"
            />
          </div>
          <span v-if="news.createTime" class="news-date">{{ formatDate(news.createTime) }}</span>
        </div>
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.news-card {
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  // 大卡片样式
  &--large {
    background: var(--background-white, #ffffff);
    border-radius: 20px;
    overflow: hidden;
    box-shadow: var(--shadow-light, 0 2px 12px rgba(0, 0, 0, 0.08));
    border: 1px solid var(--border-light, rgba(0, 0, 0, 0.06));
    
    &:hover {
      transform: translateY(-8px);
      box-shadow: var(--shadow-hover, 0 12px 32px rgba(0, 0, 0, 0.15));
      border-color: var(--primary-color, #409eff);
    }
    
    .news-image {
      width: 100%;
      height: 280px;
      object-fit: cover;
      background: linear-gradient(135deg, var(--background-light, #f8f9fa) 0%, #e9ecef 100%);
    }
    
    .news-content {
      padding: 20px;
      
      .news-title {
        font-size: 18px;
        font-weight: bold;
        color: var(--text-primary, #2c3e50);
        margin-bottom: 10px;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      
      .news-summary {
        font-size: 14px;
        color: var(--text-secondary, #7f8c8d);
        line-height: 1.5;
        display: -webkit-box;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
        overflow: hidden;
        margin-bottom: 12px;
      }
      
      .news-meta {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 10px 14px;
        padding-top: 4px;
        border-top: 1px solid rgba(0, 0, 0, 0.06);
        margin-top: 4px;

        .news-publisher-cell {
          flex: 1;
          min-width: 0;
        }

        .news-meta-rest {
          display: flex;
          flex-wrap: wrap;
          align-items: center;
          gap: 8px 12px;
          margin-left: auto;
        }

        .news-date {
          font-size: 12px;
          color: var(--text-secondary, #7f8c8d);
        }
        
        .news-views {
          font-size: 12px;
          color: var(--primary-color, #409eff);
        }

        :deep(.publisher-name) {
          font-size: 13px;
        }
      }
    }
  }
  
  // 小卡片样式
  &--small {
    display: flex;
    background: var(--background-white, #ffffff);
    border-radius: 16px;
    overflow: hidden;
    box-shadow: var(--shadow-light, 0 2px 12px rgba(0, 0, 0, 0.08));
    border: 1px solid var(--border-light, rgba(0, 0, 0, 0.06));
    
    &:hover {
      transform: translateX(8px);
      box-shadow: var(--shadow-hover, 0 12px 32px rgba(0, 0, 0, 0.15));
      border-color: var(--primary-color, #409eff);
    }
    
    .news-cover-box {
      flex-shrink: 0;
      width: 80px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 10px;
      
      img {
        width: 60px;
        height: 60px;
        border-radius: 8px;
        object-fit: cover;
        background: linear-gradient(135deg, var(--background-light, #f8f9fa) 0%, #e9ecef 100%);
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
        color: var(--text-primary, #2c3e50);
        line-height: 1.5;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        margin-bottom: 8px;
      }
      
      .news-summary {
        font-size: 12px;
        color: var(--text-secondary, #7f8c8d);
        line-height: 1.4;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        margin-bottom: 8px;
      }
      
      .news-meta {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 8px;
        margin-top: auto;

        &.news-meta--small {
          padding-top: 6px;
          border-top: 1px solid rgba(0, 0, 0, 0.06);
        }

        .news-publisher-cell {
          min-width: 0;
        }

        .news-date {
          font-size: 11px;
          color: var(--primary-color, #409eff);
          font-weight: 600;
          margin-left: auto;
        }

        :deep(.publisher-name) {
          font-size: 12px;
        }
      }
    }
  }
}

// 图片错误占位符
.image-error {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--background-light, #f8f9fa) 0%, #e9ecef 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary, #7f8c8d);
  font-size: 14px;
  font-weight: 500;
}

// 响应式设计
@media (max-width: 768px) {
  .news-card {
    &--large {
      .news-content {
        padding: 16px;
        
        .news-title {
          font-size: 16px;
        }
        
        .news-summary {
          font-size: 13px;
        }
      }
    }
    
    &--small {
      .news-cover-box {
        width: 70px;
        padding: 8px;
        
        img {
          width: 50px;
          height: 50px;
        }
      }
      
      .news-content {
        padding: 12px 16px;
        
        .news-title {
          font-size: 14px;
        }
        
        .news-summary {
          font-size: 11px;
        }
      }
    }
  }
}
</style>