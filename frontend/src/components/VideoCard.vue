<script setup lang="ts">
import { computed } from 'vue'
import { getImageUrl } from '../utils/system'
import { useRouter } from 'vue-router'

// 定义 props
interface VideoProps {
  video: {
    id: number
    title: string
    url: string
    coverUrl?: string
    viewCount: number
    createTime: string
  }
  size?: 'large' | 'small'
}

const props = withDefaults(defineProps<VideoProps>(), {
  size: 'large'
})

const router = useRouter()

// 处理点击事件
const handleClick = () => {
  router.push({
    path: '/front/videosDetail',
    query: {
      id: props.video.id
    }
  })
}

// 格式化日期
const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 格式化播放量
const formatViewCount = (count: number) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count.toString()
}

// 计算封面图片 URL
const coverImageUrl = computed(() => {
  return props.video.coverUrl ? getImageUrl(props.video.coverUrl) : ''
})

// 计算卡片类名
const cardClass = computed(() => {
  return [
    'video-card',
    `video-card--${props.size}`
  ]
})
</script>

<template>
  <div :class="cardClass" @click="handleClick">
    <!-- 视频封面区域 -->
    <div class="video-cover">
      <div class="cover-image">
        <img 
          v-if="coverImageUrl" 
          :src="coverImageUrl" 
          :alt="video.title"
          loading="lazy"
        />
        <div v-else class="no-cover">
          <Play class="play-icon" />
        </div>
      </div>
      
      <!-- 播放按钮覆盖层 -->
      <div class="play-overlay">
        <div class="play-button">
        </div>
      </div>
      
      <!-- 播放量显示 -->
      <div class="view-count">
        <span>{{ formatViewCount(video.viewCount) }}</span>
      </div>
    </div>
    
    <!-- 视频信息区域 -->
    <div class="video-info">
      <h3 class="video-title">{{ video.title }}</h3>
      <div class="video-meta">
        <span class="create-time">{{ formatDate(video.createTime) }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.video-card {
  background: var(--bg-primary);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: var(--shadow-light);
  transition: all 0.3s ease;
  cursor: pointer;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-medium);
    
    .play-overlay {
      opacity: 1;
    }
  }
}

// 视频封面区域
.video-cover {
  position: relative;
  width: 100%;
  padding-bottom: 56.25%; // 16:9 宽高比
  overflow: hidden;
  background: var(--bg-secondary);
  
  .cover-image {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }
    
    .no-cover {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      
      .play-icon {
        font-size: 48px;
        color: rgba(255, 255, 255, 0.8);
      }
    }
  }
  
  // 播放按钮覆盖层
  .play-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s ease;
    
    .play-button {
      width: 60px;
      height: 60px;
      background: rgba(255, 255, 255, 0.9);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      transform: scale(0.8);
      transition: transform 0.3s ease;
      
      .play-icon {
        font-size: 24px;
        color: var(--accent-color);
        margin-left: 2px; // 视觉居中调整
      }
    }
  }
  
  // 播放量显示
  .view-count {
    position: absolute;
    bottom: 8px;
    right: 8px;
    background: rgba(0, 0, 0, 0.7);
    color: white;
    padding: 4px 8px;
    border-radius: 12px;
    font-size: 12px;
    display: flex;
    align-items: center;
    gap: 4px;
    
    .eye-icon {
      font-size: 12px;
    }
  }
}

// 视频信息区域
.video-info {
  padding: 16px;
  
  .video-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 8px 0;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .video-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .create-time {
      font-size: 13px;
      color: var(--text-secondary);
    }
  }
}

// 大尺寸样式
.video-card--large {
  .video-info {
    .video-title {
      font-size: 18px;
    }
  }
}

// 小尺寸样式
.video-card--small {
  .video-cover {
    padding-bottom: 50%; // 调整宽高比
  }
  
  .video-info {
    padding: 12px;
    
    .video-title {
      font-size: 14px;
      -webkit-line-clamp: 1;
    }
    
    .video-meta {
      .create-time {
        font-size: 12px;
      }
    }
  }
  
  .play-overlay {
    .play-button {
      width: 40px;
      height: 40px;
      
      .play-icon {
        font-size: 16px;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .video-card {
    .video-info {
      padding: 12px;
      
      .video-title {
        font-size: 15px;
      }
    }
  }
}

@media (max-width: 480px) {
  .video-card {
    .video-cover {
      .play-overlay {
        .play-button {
          width: 50px;
          height: 50px;
          
          .play-icon {
            font-size: 20px;
          }
        }
      }
    }
    
    .video-info {
      padding: 10px;
      
      .video-title {
        font-size: 14px;
      }
    }
  }
}
</style>