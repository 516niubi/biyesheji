<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import http from '../../utils/http'
import { getImageUrl } from '../../utils/system'
import { ElMessage } from 'element-plus'
import "vue3-video-play/dist/style.css"
import videoPlay from "vue3-video-play"

const route = useRoute()
const loading = ref(false)
const videoDetail = ref<any>({})

// 视频播放器配置
const videoOptions = ref({
  width: "100%",
  height: "500px",
  color: "#409eff",
  title: "",
  src: "",
  muted: false,
  webFullScreen: false,
  speedRate: ["0.75", "1.0", "1.25", "1.5", "2.0"],
  autoPlay: false,
  loop: false,
  mirror: false,
  ligthOff: false,
  volume: 0.3,
  control: true,
  controlBtns: [
    "audioTrack",
    "quality",
    "speedRate",
    "volume",
    "setting",
    "pip",
    "pageFullScreen",
    "fullScreen",
  ],
})

// 获取视频详情
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
      // 设置视频播放器
      videoOptions.value.src = getImageUrl(res.data.url)
      videoOptions.value.title = res.data.title
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
  getVideoDetail()
})
</script>

<template>
  <div class="video-detail-container">
    <el-card v-loading="loading" class="detail-card">
      <template v-if="!loading && videoDetail.id">
        <!-- 标题居中 -->
        <div class="title-section">
          <h1 class="video-title">{{ videoDetail.title }}</h1>
        </div>
        
        <!-- 创建时间和播放量 -->
        <div class="meta-info">
          <div class="meta-item">
            <span>{{ formatDate(videoDetail.createTime) }}</span>
          </div>
          <div class="meta-item">
            <span>播放量：{{ videoDetail.viewCount || 0 }}</span>
          </div>
        </div>
        
        <!-- 视频播放器 -->
        <div class="video-section">
          <videoPlay v-bind="videoOptions" />
        </div>
      </template>
      
      <!-- 空状态 -->
      <el-empty v-else-if="!loading" description="暂无数据" />
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.video-detail-container {
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
      
      .video-title {
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
    
    .video-section {
      margin-bottom: 20px;
      
      :deep(.d-player-video-wrap) {
        border-radius: 8px;
        overflow: hidden;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .video-detail-container {
    padding: 10px;
    
    .detail-card {
      .title-section {
        .video-title {
          font-size: 24px;
        }
      }
      
      .meta-info {
        flex-direction: column;
        gap: 15px;
        text-align: center;
      }
      
      .video-section {
        :deep(.d-player) {
          height: 250px !important;
        }
      }
    }
  }
}
</style>