<script setup>
import { ElImage } from 'element-plus'
import { getImageUrl } from '../utils/system'
import { useRouter } from 'vue-router'

const props = defineProps({
  activity: {
    type: Object,
    required: true
  },
  size: {
    type: String,
    default: 'small', // 'large' | 'small'
    validator: (value) => ['large', 'small'].includes(value)
  }
})

const router = useRouter()

const handleClick = () => {
  router.push({
    path: '/front/activityDetail',
    query: {
      id: props.activity.id
    }
  })
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const getStatusText = () => {
  const now = new Date()
  const startTime = new Date(props.activity.startTime)
  const endTime = new Date(props.activity.endTime)
  
  if (now < startTime) {
    return '即将开始'
  } else if (now >= startTime && now <= endTime) {
    return '进行中'
  } else {
    return '已结束'
  }
}

const getStatusClass = () => {
  const now = new Date()
  const startTime = new Date(props.activity.startTime)
  const endTime = new Date(props.activity.endTime)
  
  if (now < startTime) {
    return 'upcoming'
  } else if (now >= startTime && now <= endTime) {
    return 'ongoing'
  } else {
    return 'ended'
  }
}
</script>

<template>
  <div class="activity-card" :class="{ 'large': size === 'large' }" @click="handleClick">
    <div class="card-image-wrapper">
      <el-image 
        :src="getImageUrl(activity.coverImage)" 
        :alt="activity.title"
        type="card"
        class="activity-image"
        fit="cover"
      >
        <template #error>
          <div class="image-error">
            <i class="el-icon-picture-outline"></i>
          </div>
        </template>
      </el-image>
      <div class="card-overlay">
        <div class="activity-status" :class="getStatusClass()">
          {{ getStatusText() }}
        </div>
        <div class="activity-meta" v-if="size === 'large'">
          <div class="activity-date" v-if="activity.startTime">
            <i class="el-icon-date"></i>
            {{ formatDate(activity.startTime) }}
          </div>
          <div class="view-count">
            <i class="el-icon-view"></i>
            {{ activity.viewCount || 0 }}
          </div>
        </div>
      </div>
    </div>
    <div class="card-content" v-if="size === 'large'">
      <h3 class="activity-title">{{ activity.title }}</h3>
      <p class="activity-description" v-if="activity.content">
        {{ activity.content }}
      </p>
      <div class="activity-info">
        <span class="activity-location" v-if="activity.address">
          <i class="el-icon-location"></i>
          {{ activity.address }}
        </span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.activity-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.06);
  height: 100%;
  
  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
    border-color: #e74c3c;
    
    .card-overlay {
      opacity: 1;
    }
    
    .activity-image {
      transform: scale(1.05);
    }
  }
  
  &.large {
    .card-image-wrapper {
      height: 280px;
    }
  }
  
  .card-image-wrapper {
    position: relative;
    height: 120px;
    overflow: hidden;
    background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 50%, #fecfef 100%);
    
    .activity-image {
      width: 100%;
      height: 100%;
      transition: transform 0.3s ease;
    }
    
    .card-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: linear-gradient(to bottom, rgba(0,0,0,0.2) 0%, transparent 50%, rgba(0,0,0,0.4) 100%);
      opacity: 0;
      transition: opacity 0.3s ease;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      padding: 12px;
      
      .activity-status {
        align-self: flex-end;
        padding: 4px 12px;
        border-radius: 20px;
        font-size: 11px;
        font-weight: 600;
        backdrop-filter: blur(10px);
        
        &.upcoming {
          background: rgba(52, 152, 219, 0.9);
          color: white;
        }
        
        &.ongoing {
          background: rgba(46, 204, 113, 0.9);
          color: white;
        }
        
        &.ended {
          background: rgba(149, 165, 166, 0.9);
          color: white;
        }
      }
      
      .activity-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .activity-date,
        .view-count {
          background: rgba(255, 255, 255, 0.95);
          color: #666;
          padding: 4px 8px;
          border-radius: 12px;
          font-size: 11px;
          font-weight: 500;
          display: flex;
          align-items: center;
          gap: 3px;
          backdrop-filter: blur(10px);
          
          i {
            font-size: 12px;
          }
        }
      }
    }
  }
  
  .card-content {
    padding: 20px;
    
    .activity-title {
      font-size: 18px;
      font-weight: 600;
      color: #2c3e50;
      margin: 0 0 12px 0;
      line-height: 1.4;
    }
    
    .activity-description {
      font-size: 13px;
      color: #7f8c8d;
      line-height: 1.5;
      margin: 0 0 16px 0;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    
    .activity-info {
      .activity-location {
        font-size: 12px;
        color: #e74c3c;
        font-weight: 500;
        display: flex;
        align-items: center;
        gap: 4px;
        
        i {
          font-size: 14px;
        }
      }
    }
  }
}

.image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  color: rgba(255, 255, 255, 0.8);
  font-size: 32px;
}

@media (max-width: 768px) {
  .activity-card {
    &.large {
      .card-image-wrapper {
        height: 220px;
      }
    }
    
    .card-image-wrapper {
      height: 100px;
    }
    
    .card-content {
      padding: 16px;
      
      .activity-title {
        font-size: 16px;
      }
      
      .activity-description {
        font-size: 12px;
      }
    }
  }
}
</style>