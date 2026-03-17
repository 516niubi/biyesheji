<template>
  <div class="heritage-card" @click="handleClick">
    <div class="card-image-wrapper">
      <el-image 
        :src="heritage.coverImage" 
        :alt="heritage.name"
        class="heritage-image"
        fit="cover"
      >
        <template #error>
          <div class="image-error">
            <i class="el-icon-picture-outline"></i>
          </div>
        </template>
      </el-image>
      <div class="card-overlay">
        <div class="view-count">
          <i class="el-icon-view"></i>
          {{ heritage.viewCount || 0 }}
        </div>
      </div>
    </div>
    <div class="card-content">
      <h3 class="heritage-name">{{ heritage.name }}</h3>
      <p class="heritage-category">{{ heritage.categoryName }}</p>
      <div class="heritage-description" v-if="heritage.intro">
        {{ heritage.intro }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElImage } from 'element-plus'
import { useRouter } from 'vue-router'

const props = defineProps({
  heritage: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const handleClick = () => {
  router.push({
    path: "/front/heritageDetail",
    query: {
      id: props.heritage.id,
    },
  })
}
</script>

<style lang="scss" scoped>
.heritage-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.06);
  
  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
    border-color: #3498db;
    
    .card-overlay {
      opacity: 1;
    }
    
    .heritage-image {
      transform: scale(1.05);
    }
  }
  
  .card-image-wrapper {
    position: relative;
    height: 200px;
    overflow: hidden;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    
    .heritage-image {
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
      background: linear-gradient(to bottom, rgba(0,0,0,0.1) 0%, transparent 50%, rgba(0,0,0,0.3) 100%);
      opacity: 0;
      transition: opacity 0.3s ease;
      display: flex;
      align-items: flex-end;
      justify-content: flex-end;
      padding: 12px;
      
      .view-count {
        background: rgba(255, 255, 255, 0.95);
        color: #666;
        padding: 6px 12px;
        border-radius: 20px;
        font-size: 12px;
        font-weight: 500;
        display: flex;
        align-items: center;
        gap: 4px;
        backdrop-filter: blur(10px);
        
        i {
          font-size: 14px;
        }
      }
    }
  }
  
  .card-content {
    padding: 20px;
    
    .heritage-name {
      font-size: 16px;
      font-weight: 600;
      color: #2c3e50;
      margin: 0 0 8px 0;
      line-height: 1.4;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .heritage-category {
      font-size: 13px;
      color: #3498db;
      margin: 0 0 12px 0;
      font-weight: 500;
      background: rgba(52, 152, 219, 0.1);
      padding: 4px 8px;
      border-radius: 12px;
      display: inline-block;
    }
    
    .heritage-description {
      font-size: 12px;
      color: #7f8c8d;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }
}

.image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  color: #bdc3c7;
  font-size: 32px;
}

@media (max-width: 768px) {
  .heritage-card {
    .card-image-wrapper {
      height: 160px;
    }
    
    .card-content {
      padding: 16px;
      
      .heritage-name {
        font-size: 15px;
      }
      
      .heritage-category {
        font-size: 12px;
      }
      
      .heritage-description {
        font-size: 11px;
      }
    }
  }
}
</style>