<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElDialog, ElForm, ElFormItem, ElInput, ElButton, ElTag, ElImage } from 'element-plus'
import { Calendar, Location, User, Phone, Edit } from '@element-plus/icons-vue'
import http from '../../utils/http'
import { getImageUrl } from '../../utils/system'
import useUserStore from '../../stores/userStore'

const route = useRoute()
const userStore = useUserStore();

// 活动详情数据
const activityDetail = ref(null)
const loading = ref(false)

// 报名弹窗相关
const showApplyDialog = ref(false)
const applyForm = ref({
  realName: '',
  phone: '',
  remark: ''
})
const applyLoading = ref(false)
const activityId = route.query.id

// 获取活动详情
const getActivityDetail = async () => {
  if (!activityId) {
    ElMessage.error('活动ID不能为空')
    return
  }
  
  loading.value = true
  try {
    const res = await http.get(`/activity/getById?id=${activityId}`)
    if (res.code === 200) {
      activityDetail.value = res.data
    } else {
      ElMessage.error('获取活动详情失败')
    }
  } catch (error) {
    console.error('获取活动详情失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取活动状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '未开始',
    1: '进行中', 
    2: '已结束'
  }
  return statusMap[status] || '未知'
}

// 获取活动状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'info'
  }
  return typeMap[status] || 'info'
}

// 判断是否可以报名
const canApply = computed(() => {
  if (!activityDetail.value) return false
  
  const now = new Date()
  const startTime = new Date(activityDetail.value.startTime)
  const endTime = new Date(activityDetail.value.endTime)
  
  // 活动状态为进行中且当前时间在活动时间范围内
  return activityDetail.value.status === 1 && now >= startTime && now <= endTime
})

// 获取报名按钮文本
const getApplyButtonText = computed(() => {
  if (!activityDetail.value) return '加载中...'
  
  const now = new Date()
  const startTime = new Date(activityDetail.value.startTime)
  const endTime = new Date(activityDetail.value.endTime)
  
  if (activityDetail.value.status === 0) {
    return '活动未开始'
  } else if (activityDetail.value.status === 2) {
    return '活动已结束'
  } else if (now < startTime) {
    return '报名未开始'
  } else if (now > endTime) {
    return '报名已结束'
  } else {
    return '立即报名'
  }
})

// 打开报名弹窗
const openApplyDialog = () => {
  if (!canApply.value) {
    ElMessage.warning('当前无法报名')
    return
  }
  showApplyDialog.value = true
}

// 提交报名
const submitApply = async () => {
  if (!applyForm.value.realName.trim()) {
    ElMessage.warning('请输入真实姓名')
    return
  }
  if (!applyForm.value.phone.trim()) {
    ElMessage.warning('请输入联系电话')
    return
  }
  
  applyLoading.value = true
  try {
    const userId = userStore.userInfo.id;
    const res = await http.post('/activityApplication/apply', {
      userId: parseInt(userId),
      activityId: parseInt(activityId),
      realName: applyForm.value.realName,
      phone: applyForm.value.phone,
      remark: applyForm.value.remark
    })
    
    if (res.code === 200) {
      ElMessage.success('报名成功，请等待审核')
      showApplyDialog.value = false
      // 重置表单
      applyForm.value = {
        realName: '',
        phone: '',
        remark: ''
      }
    } else {
      ElMessage.error(res.message || '报名失败')
    }
  } catch (error) {
    console.error('报名失败:', error)
    ElMessage.error('报名失败，请稍后重试')
  } finally {
    applyLoading.value = false
  }
}

// 取消报名
const cancelApply = () => {
  showApplyDialog.value = false
  applyForm.value = {
    realName: '',
    phone: '',
    remark: ''
  }
}

onMounted(() => {
  getActivityDetail()
})
</script>

<template>
  <div class="activity-detail-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-text">加载中...</div>
    </div>

    <!-- 活动详情 -->
    <div v-else-if="activityDetail" class="activity-detail">
      <!-- 活动信息区域 -->
      <div class="activity-info-section">
        <div class="activity-info-container">
          <!-- 左侧封面 -->
          <div class="activity-cover">
            <el-image
              :src="getImageUrl(activityDetail.coverImage)"
              :alt="activityDetail.title"
              fit="cover"
              class="cover-image"
            >
              <template #error>
                <div class="image-error">
                  <span>暂无封面</span>
                </div>
              </template>
            </el-image>
          </div>

          <!-- 右侧信息 -->
          <div class="activity-info">
            <h1 class="activity-title">{{ activityDetail.title }}</h1>
            
            <div class="activity-meta">
              <div class="meta-item">
                <el-tag :type="getStatusType(activityDetail.status)" size="large">
                  {{ getStatusText(activityDetail.status) }}
                </el-tag>
              </div>
              
              <div class="meta-item">
                <Calendar class="meta-icon" />
                <span class="meta-label">活动时间：</span>
                <span class="meta-value">
                  {{ formatDate(activityDetail.startTime) }} 至 {{ formatDate(activityDetail.endTime) }}
                </span>
              </div>
              
              <div class="meta-item">
                <Location class="meta-icon" />
                <span class="meta-label">活动地点：</span>
                <span class="meta-value">{{ activityDetail.address || '待定' }}</span>
              </div>
              
              <div class="meta-item">
                <User class="meta-icon" />
                <span class="meta-label">限制人数：</span>
                <span class="meta-value">{{ activityDetail.maxPeople || '不限' }}人</span>
              </div>
              
              <div class="meta-item">
                <span class="meta-label">浏览量：</span>
                <span class="meta-value">{{ activityDetail.viewCount || 0 }}</span>
              </div>
            </div>

            <!-- 报名按钮 -->
            <div class="apply-section">
              <el-button 
                type="primary" 
                size="large" 
                :disabled="!canApply"
                @click="openApplyDialog"
                class="apply-button"
              >
                {{ getApplyButtonText }}
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 活动内容 -->
      <div class="activity-content-section">
        <div class="content-container">
          <h2 class="content-title">活动详情</h2>
          <div class="content-body" v-html="activityDetail.content"></div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-container">
      <div class="empty-content">
        <div class="empty-icon">🎭</div>
        <h3 class="empty-title">活动不存在</h3>
        <p class="empty-description">该活动可能已被删除或不存在</p>
      </div>
    </div>

    <!-- 报名弹窗 -->
    <el-dialog
      v-model="showApplyDialog"
      title="活动报名"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="applyForm" label-width="80px">
        <el-form-item label="真实姓名" required>
          <el-input
            v-model="applyForm.realName"
            placeholder="请输入您的真实姓名"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item label="联系电话" required>
          <el-input
            v-model="applyForm.phone"
            placeholder="请输入您的联系电话"
            :prefix-icon="Phone"
          />
        </el-form-item>
        
        <el-form-item label="备注信息">
          <el-input
            v-model="applyForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（选填）"
            :prefix-icon="Edit"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelApply">取消</el-button>
          <el-button type="primary" :loading="applyLoading" @click="submitApply">
            确认报名
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.activity-detail-page {
  min-height: 100vh;
  background: var(--bg-secondary);
}

// 加载状态
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  
  .loading-text {
    font-size: 16px;
    color: var(--text-secondary);
  }
}

// 活动详情
.activity-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
}

// 活动信息区域
.activity-info-section {
  background: var(--bg-primary);
  border-radius: 16px;
  padding: 40px;
  margin-bottom: 32px;
  box-shadow: var(--shadow-light);
  
  .activity-info-container {
    display: grid;
    grid-template-columns: 400px 1fr;
    gap: 40px;
    align-items: start;
  }
}

// 封面图片
.activity-cover {
  .cover-image {
    width: 100%;
    height: 300px;
    border-radius: 12px;
    overflow: hidden;
    
    .image-error {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 100%;
      height: 100%;
      background: var(--bg-secondary);
      color: var(--text-secondary);
      font-size: 14px;
    }
  }
}

// 活动信息
.activity-info {
  .activity-title {
    font-size: 32px;
    font-weight: 700;
    color: var(--text-primary);
    margin: 0 0 24px 0;
    line-height: 1.3;
  }
  
  .activity-meta {
    display: flex;
    flex-direction: column;
    gap: 16px;
    margin-bottom: 32px;
    
    .meta-item {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .meta-icon {
        width: 16px;
        height: 16px;
        color: var(--accent-color);
      }
      
      .meta-label {
        font-weight: 600;
        color: var(--text-primary);
        min-width: 80px;
      }
      
      .meta-value {
        color: var(--text-secondary);
        flex: 1;
      }
    }
  }
  
  .apply-section {
    .apply-button {
      width: 200px;
      height: 48px;
      font-size: 16px;
      font-weight: 600;
      border-radius: 24px;
    }
  }
}

// 活动内容区域
.activity-content-section {
  background: var(--bg-primary);
  border-radius: 16px;
  padding: 40px;
  box-shadow: var(--shadow-light);
  
  .content-container {
    .content-title {
      font-size: 24px;
      font-weight: 700;
      color: var(--text-primary);
      margin: 0 0 24px 0;
      padding-bottom: 16px;
      border-bottom: 2px solid var(--border-color);
    }
    
    .content-body {
      color: var(--text-secondary);
      line-height: 1.8;
      font-size: 16px;
      
      :deep(img) {
        max-width: 100%;
        height: auto;
        border-radius: 8px;
        margin: 16px 0;
      }
      
      :deep(p) {
        margin: 16px 0;
      }
      
      :deep(h1, h2, h3, h4, h5, h6) {
        color: var(--text-primary);
        margin: 24px 0 16px 0;
      }
    }
  }
}

// 空状态
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  
  .empty-content {
    text-align: center;
    
    .empty-icon {
      font-size: 64px;
      margin-bottom: 16px;
    }
    
    .empty-title {
      font-size: 20px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0 0 8px 0;
    }
    
    .empty-description {
      font-size: 14px;
      color: var(--text-secondary);
      margin: 0;
    }
  }
}

// 弹窗样式
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 响应式设计
@media (max-width: 768px) {
  .activity-detail {
    padding: 20px 16px;
  }
  
  .activity-info-section {
    padding: 24px;
    
    .activity-info-container {
      grid-template-columns: 1fr;
      gap: 24px;
    }
  }
  
  .activity-cover {
    .cover-image {
      height: 200px;
    }
  }
  
  .activity-info {
    .activity-title {
      font-size: 24px;
    }
    
    .apply-section {
      .apply-button {
        width: 100%;
      }
    }
  }
  
  .activity-content-section {
    padding: 24px;
    
    .content-container {
      .content-title {
        font-size: 20px;
      }
    }
  }
}

@media (max-width: 480px) {
  .activity-info {
    .activity-meta {
      .meta-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 4px;
        
        .meta-label {
          min-width: auto;
        }
      }
    }
  }
}
</style>