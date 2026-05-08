<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElDialog, ElForm, ElFormItem, ElInput, ElButton, ElTag, ElImage } from 'element-plus'
import { Calendar, Location, User, Phone, Edit, Clock, View } from '@element-plus/icons-vue'
import http from '../../utils/http'
import { getImageUrl } from '../../utils/system'
import useUserStore from '../../stores/userStore'
import PublisherInheritorRow from '../../components/PublisherInheritorRow.vue'

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
      <nav class="breadcrumb" aria-label="面包屑导航">
        <router-link to="/front/home">首页</router-link>
        <span class="breadcrumb-sep">/</span>
        <router-link to="/front/activity">活动中心</router-link>
        <span class="breadcrumb-sep">/</span>
        <span class="breadcrumb-current">活动详情</span>
      </nav>

      <!-- 活动信息区域 -->
      <div class="activity-info-section">
        <div class="section-kicker">非遗活动</div>
        <div class="activity-info-container">
          <!-- 左侧封面 -->
          <div class="activity-cover">
            <div class="cover-frame">
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
          </div>

          <!-- 右侧信息 -->
          <div class="activity-info">
            <div class="title-row">
              <h1 class="activity-title">{{ activityDetail.title }}</h1>
              <el-tag :type="getStatusType(activityDetail.status)" effect="dark" round class="status-pill">
                {{ getStatusText(activityDetail.status) }}
              </el-tag>
            </div>

            <p class="activity-lead">
              欢迎参与本次非遗文化主题活动，请留意时间与地点安排。
            </p>

            <div class="quick-stats">
              <div class="stat-chip">
                <Calendar class="stat-icon" />
                <div class="stat-text">
                  <span class="stat-label">活动时间</span>
                  <span class="stat-value">
                    {{ formatDate(activityDetail.startTime) }} — {{ formatDate(activityDetail.endTime) }}
                  </span>
                </div>
              </div>
              <div class="stat-chip">
                <Location class="stat-icon" />
                <div class="stat-text">
                  <span class="stat-label">活动地点</span>
                  <span class="stat-value">{{ activityDetail.address || '待定' }}</span>
                </div>
              </div>
              <div class="stat-chip">
                <User class="stat-icon" />
                <div class="stat-text">
                  <span class="stat-label">人数上限</span>
                  <span class="stat-value">{{ activityDetail.maxPeople != null ? activityDetail.maxPeople + ' 人' : '不限' }}</span>
                </div>
              </div>
              <div class="stat-chip stat-chip--publisher">
                <PublisherInheritorRow
                  :inheritor-id="activityDetail.creatorId"
                  :name="activityDetail.publisherName || '平台'"
                  :avatar="activityDetail.publisherAvatar"
                  :size="44"
                />
              </div>
              <div class="stat-chip" v-if="activityDetail.createTime">
                <Clock class="stat-icon" />
                <div class="stat-text">
                  <span class="stat-label">发布时间</span>
                  <span class="stat-value">{{ formatDate(activityDetail.createTime) }}</span>
                </div>
              </div>
              <div class="stat-chip">
                <View class="stat-icon" />
                <div class="stat-text">
                  <span class="stat-label">浏览量</span>
                  <span class="stat-value">{{ activityDetail.viewCount ?? 0 }}</span>
                </div>
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
                round
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
          <div class="content-head">
            <h2 class="content-title">活动详情</h2>
            <p class="content-sub">活动介绍与说明</p>
          </div>
          <div v-if="activityDetail.content" class="content-body" v-html="activityDetail.content" />
          <div v-else class="content-body content-body--empty">
            <p class="empty-placeholder">暂无更多图文介绍，如有疑问可联系发布人或平台客服。</p>
          </div>
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
  --accent-color: #c53d2e;
  --bg-primary: #ffffff;
  --bg-secondary: #f6f3ef;
  --text-primary: #1f1a17;
  --text-secondary: #5c534c;
  --border-color: rgba(31, 26, 23, 0.08);
  --shadow-light: 0 8px 32px rgba(31, 26, 23, 0.08);
  min-height: 100vh;
  position: relative;
  background: linear-gradient(180deg, #efe6dc 0%, #f6f3ef 28%, #f8f9fa 100%);
  padding-bottom: 48px;
}

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

.activity-detail {
  max-width: 1120px;
  margin: 0 auto;
  padding: 28px 24px 0;
}

.breadcrumb {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;

  a {
    color: var(--accent-color);
    font-weight: 500;
    transition: opacity 0.2s;

    &:hover {
      opacity: 0.85;
    }
  }

  .breadcrumb-sep {
    opacity: 0.45;
    user-select: none;
  }

  .breadcrumb-current {
    color: var(--text-primary);
    font-weight: 600;
  }
}

.activity-info-section {
  position: relative;
  background: var(--bg-primary);
  border-radius: 20px;
  padding: 32px 36px 36px;
  margin-bottom: 28px;
  box-shadow: var(--shadow-light);
  border: 1px solid var(--border-color);
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0 0 auto 0;
    height: 4px;
    background: linear-gradient(90deg, #c53d2e, #d4a574, #2d6a4f);
    opacity: 0.95;
  }

  .section-kicker {
    font-size: 12px;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    color: var(--accent-color);
    font-weight: 700;
    margin-bottom: 12px;
  }

  .activity-info-container {
    display: grid;
    grid-template-columns: minmax(280px, 400px) 1fr;
    gap: 36px;
    align-items: start;
  }
}

.activity-cover {
  .cover-frame {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 12px 40px rgba(31, 26, 23, 0.12);
    border: 1px solid var(--border-color);
    background: var(--bg-secondary);
  }

  .cover-image {
    width: 100%;
    height: 300px;
    display: block;

    .image-error {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 100%;
      height: 100%;
      min-height: 220px;
      background: var(--bg-secondary);
      color: var(--text-secondary);
      font-size: 14px;
    }
  }
}

.activity-info {
  .title-row {
    display: flex;
    flex-wrap: wrap;
    align-items: flex-start;
    gap: 12px 16px;
    margin-bottom: 12px;
  }

  .activity-title {
    flex: 1;
    min-width: 200px;
    font-size: clamp(24px, 3vw, 34px);
    font-weight: 800;
    color: var(--text-primary);
    margin: 0;
    line-height: 1.25;
    letter-spacing: -0.02em;
  }

  .status-pill {
    flex-shrink: 0;
    font-weight: 600;
  }

  .activity-lead {
    margin: 0 0 22px;
    font-size: 15px;
    line-height: 1.65;
    color: var(--text-secondary);
    max-width: 52em;
  }

  .quick-stats {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 12px;
    margin-bottom: 28px;
  }

  .stat-chip {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 14px 16px;
    border-radius: 14px;
    background: linear-gradient(135deg, rgba(197, 61, 46, 0.06), rgba(212, 165, 116, 0.08));
    border: 1px solid var(--border-color);
  }

  .stat-chip--publisher {
    align-items: center;

    :deep(.publisher-inheritor-row) {
      width: 100%;
      min-width: 0;
    }
  }

  .stat-icon {
    width: 22px;
    height: 22px;
    color: var(--accent-color);
    flex-shrink: 0;
    margin-top: 2px;
  }

  .stat-text {
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-width: 0;
  }

  .stat-label {
    font-size: 12px;
    font-weight: 600;
    color: var(--text-secondary);
    letter-spacing: 0.02em;
  }

  .stat-value {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
    line-height: 1.45;
    word-break: break-word;
  }

  .apply-section {
    .apply-button {
      min-width: 220px;
      height: 48px;
      font-size: 16px;
      font-weight: 700;
      padding: 0 28px;
    }
  }
}

.activity-content-section {
  background: var(--bg-primary);
  border-radius: 20px;
  padding: 32px 36px 40px;
  box-shadow: var(--shadow-light);
  border: 1px solid var(--border-color);

  .content-head {
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--border-color);
  }

  .content-title {
    font-size: 22px;
    font-weight: 800;
    color: var(--text-primary);
    margin: 0 0 6px;
    letter-spacing: -0.02em;
  }

  .content-sub {
    margin: 0;
    font-size: 14px;
    color: var(--text-secondary);
  }

  .content-body {
    color: var(--text-secondary);
    line-height: 1.9;
    font-size: 16px;

    :deep(img) {
      max-width: 100%;
      height: auto;
      border-radius: 12px;
      margin: 16px 0;
      box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
    }

    :deep(p) {
      margin: 16px 0;
    }

    :deep(h1, h2, h3, h4, h5, h6) {
      color: var(--text-primary);
      margin: 28px 0 14px;
      font-weight: 700;
    }

    &.content-body--empty {
      padding: 28px 20px;
      text-align: center;
      background: var(--bg-secondary);
      border-radius: 14px;
      border: 1px dashed rgba(31, 26, 23, 0.12);
    }

    .empty-placeholder {
      margin: 0;
      color: var(--text-secondary);
      font-size: 15px;
    }
  }
}

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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .activity-detail {
    padding: 16px 16px 0;
  }

  .activity-info-section {
    padding: 24px 20px 28px;

    .activity-info-container {
      grid-template-columns: 1fr;
      gap: 20px;
    }
  }

  .activity-cover .cover-image {
    height: 220px;
  }

  .activity-info {
    .apply-section .apply-button {
      width: 100%;
      min-width: 0;
    }

    .quick-stats {
      grid-template-columns: 1fr;
    }
  }

  .activity-content-section {
    padding: 22px 20px 28px;
  }
}
</style>