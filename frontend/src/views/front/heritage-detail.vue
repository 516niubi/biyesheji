<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import http from '../../utils/http'
import { getImageUrl } from '../../utils/system'
import { ElMessage } from 'element-plus'
import { Star, StarFilled, Clock, View } from '@element-plus/icons-vue'
import useUserStore from '../../stores/userStore'
import PublisherInheritorRow from '../../components/PublisherInheritorRow.vue'
import CommentThread from '../../components/CommentThread.vue'
import Config from '@/config'
import { getStoredToken } from '@/utils/authToken'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const heritageDetail = ref<any>({})
const activeTab = ref('detail')
const isCollected = ref(false)
const collectLoading = ref(false)
const comments = ref<any[]>([])
const commentLoading = ref(false)
const newComment = ref('')
const commentImages = ref<string[]>([])
const commentSubmitting = ref(false)

const uploadUrl = `${Config.baseUrl}/file/upload`
const uploadHeaders = computed(() => ({ authorization: `${getStoredToken()}` }))

const totalCommentCount = computed(() => {
  const walk = (arr: any[]): number => {
    if (!arr?.length) return 0
    let n = 0
    for (const c of arr) {
      n += 1 + walk(c.children || [])
    }
    return n
  }
  return walk(comments.value)
})

const beforeCommentImage = (raw: File) => {
  if (!raw.type.includes('image')) {
    ElMessage.error('仅支持图片')
    return false
  }
  if (raw.size / 1024 / 1024 > 2) {
    ElMessage.error('单张图片不超过 2MB')
    return false
  }
  return true
}

const onCommentImageSuccess = (res: any) => {
  if (res?.code === 200 && res.data) {
    if (commentImages.value.length >= 9) {
      ElMessage.warning('最多 9 张图片')
      return
    }
    commentImages.value.push(res.data)
  }
}

const removeRootImage = (idx: number) => {
  commentImages.value.splice(idx, 1)
}

const currentUser = computed(() => {
  try {
    return typeof userStore.userInfo === 'string' ? JSON.parse(userStore.userInfo) : userStore.userInfo
  } catch {
    return {}
  }
})

const getHeritageDetail = async () => {
  const id = route.query.id
  if (!id) {
    ElMessage.error('参数错误')
    return
  }

  loading.value = true
  try {
    const res = await http.get(`/culturalHeritage/getById?id=${id}`)
    if (res.code === 200) {
      heritageDetail.value = res.data
      await checkCollectStatus()
    } else {
      ElMessage.error('获取详情失败')
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  } finally {
    loading.value = false
  }
}

const checkCollectStatus = async () => {
  if (!currentUser.value?.id || !heritageDetail.value?.id) return

  try {
    const res = await http.get(
      `/collect/isCollected?userId=${currentUser.value.id}&heritageId=${heritageDetail.value.id}`
    )
    if (res.code === 200) {
      isCollected.value = res.data
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

const toggleCollect = async () => {
  if (!currentUser.value?.id) {
    ElMessage.warning('请先登录')
    return
  }

  collectLoading.value = true
  try {
    if (isCollected.value) {
      const collectRes = await http.get(
        `/collect/pageByUserId?pageNum=1&pageSize=1000&userId=${currentUser.value.id}`
      )
      if (collectRes.code === 200) {
        const collectRecord = collectRes.data.records.find(
          (item: any) => item.heritageId === heritageDetail.value.id
        )
        if (collectRecord) {
          await http.get(`/collect/del?id=${collectRecord.id}`)
          isCollected.value = false
          ElMessage.success('取消收藏成功')
        }
      }
    } else {
      const res = await http.post('/collect/add', {
        heritageId: heritageDetail.value.id,
        userId: currentUser.value.id
      })
      if (res.code === 200) {
        isCollected.value = true
        ElMessage.success('收藏成功')
      }
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    collectLoading.value = false
  }
}

const getComments = async () => {
  if (!heritageDetail.value?.id) return

  commentLoading.value = true
  try {
    const res = await http.get(`/comment/selectByHeritageId/${heritageDetail.value.id}`)
    if (res.code === 200) {
      comments.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('获取评论失败')
  } finally {
    commentLoading.value = false
  }
}

const submitComment = async () => {
  if (!getStoredToken()) {
    ElMessage.warning('请先登录')
    return
  }

  const text = newComment.value.trim()
  if (!text && !commentImages.value.length) {
    ElMessage.warning('请输入评论内容或上传图片')
    return
  }

  commentSubmitting.value = true
  try {
    const res = await http.post('/comment/add', {
      heritageId: heritageDetail.value.id,
      content: text,
      images: [...commentImages.value]
    })
    if (res.code === 200) {
      ElMessage.success('评论成功')
      newComment.value = ''
      commentImages.value = []
      await getComments()
    }
  } catch (error) {
    ElMessage.error('评论失败')
  } finally {
    commentSubmitting.value = false
  }
}

const handleReplySubmit = async (payload: { parentId: number; content: string; images: string[] }) => {
  if (!getStoredToken()) {
    ElMessage.warning('请先登录')
    return
  }
  const text = payload.content?.trim() || ''
  if (!text && !payload.images?.length) {
    ElMessage.warning('请输入回复内容或上传图片')
    return
  }
  commentSubmitting.value = true
  try {
    const res = await http.post('/comment/add', {
      heritageId: heritageDetail.value.id,
      parentId: payload.parentId,
      content: text,
      images: payload.images || []
    })
    if (res.code === 200) {
      ElMessage.success('回复成功')
      await getComments()
    }
  } catch (e) {
    ElMessage.error('回复失败')
  } finally {
    commentSubmitting.value = false
  }
}

const formatDate = (dateStr: string) => {
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

const handleTabChange = (tabName: string) => {
  if (tabName === 'comment') {
    getComments()
  }
}

onMounted(() => {
  getHeritageDetail()
})
</script>

<template>
  <div class="heritage-detail-page">
    <div v-if="loading" class="loading-container">
      <div class="loading-text">加载中...</div>
    </div>

    <div v-else-if="heritageDetail.id" class="heritage-detail">
      <nav class="breadcrumb" aria-label="面包屑导航">
        <router-link to="/front/home">首页</router-link>
        <span class="breadcrumb-sep">/</span>
        <router-link to="/front/heritage">非遗文物</router-link>
        <span class="breadcrumb-sep">/</span>
        <span class="breadcrumb-current">文物详情</span>
      </nav>

      <!-- 主信息区（与活动详情同构） -->
      <section class="heritage-info-section">
        <div class="section-kicker">非遗文物</div>
        <div class="heritage-info-grid">
          <div class="heritage-cover">
            <div class="cover-frame">
              <el-image
                v-if="heritageDetail.coverImage"
                :src="getImageUrl(heritageDetail.coverImage)"
                :alt="heritageDetail.name"
                fit="cover"
                class="cover-image"
                :preview-src-list="[getImageUrl(heritageDetail.coverImage)]"
                :preview-teleported="true"
                :hide-on-click-modal="true"
              >
                <template #error>
                  <div class="image-error"><span>图片加载失败</span></div>
                </template>
              </el-image>
              <div v-else class="no-image"><span>暂无封面</span></div>
            </div>
          </div>

          <div class="heritage-info">
            <div class="title-row">
              <h1 class="heritage-title">{{ heritageDetail.name }}</h1>
            </div>

            <p class="heritage-lead">
              数字化档案便于浏览与分享，欢迎收藏并在评论区交流观感。
            </p>

            <div class="quick-stats">
              <div class="stat-chip stat-chip--publisher">
                <PublisherInheritorRow
                  :inheritor-id="heritageDetail.creatorId"
                  :name="heritageDetail.publisherName || '平台'"
                  :avatar="heritageDetail.publisherAvatar"
                  :size="44"
                />
              </div>
              <div class="stat-chip" v-if="heritageDetail.createTime">
                <Clock class="stat-icon" />
                <div class="stat-text">
                  <span class="stat-label">发布时间</span>
                  <span class="stat-value">{{ formatDate(heritageDetail.createTime) }}</span>
                </div>
              </div>
              <div class="stat-chip">
                <View class="stat-icon" />
                <div class="stat-text">
                  <span class="stat-label">浏览量</span>
                  <span class="stat-value">{{ heritageDetail.viewCount ?? 0 }}</span>
                </div>
              </div>
            </div>

            <div v-if="heritageDetail.intro" class="intro-card">
              <h2 class="intro-card-title">简介</h2>
              <p class="intro-card-text">{{ heritageDetail.intro }}</p>
            </div>

            <div class="action-section">
              <el-button
                class="collect-btn"
                :class="{ 'collect-btn--active': isCollected }"
                :icon="isCollected ? StarFilled : Star"
                :loading="collectLoading"
                size="large"
                round
                @click="toggleCollect"
              >
                {{ isCollected ? '已收藏' : '收藏文物' }}
              </el-button>
            </div>
          </div>
        </div>
      </section>

      <!-- 详情 / 评论 -->
      <section class="heritage-tabs-section">
        <div class="tabs-inner">
          <div class="tabs-head">
            <h2 class="tabs-title">更多内容</h2>
            <p class="tabs-sub">图文详情与观众评论</p>
          </div>
          <el-tabs v-model="activeTab" class="heritage-tabs" @tab-change="handleTabChange">
            <el-tab-pane label="详情" name="detail">
              <div v-if="heritageDetail.description" class="content-body" v-html="heritageDetail.description" />
              <div v-else class="content-body content-body--empty">
                <p class="empty-placeholder">暂无详细图文，欢迎稍后再来查看更新。</p>
              </div>
            </el-tab-pane>
            <el-tab-pane label="评论" name="comment">
              <div class="comment-section">
                <div class="add-comment">
                  <h3 class="comment-block-title">发表评论</h3>
                  <p class="comment-login-hint">
                    登录后可发表评论；传承人登录将以「传承人」身份回复。可在右上角头像菜单进入「我的评论」管理留言与查看回复。
                  </p>
                  <div class="comment-input-area">
                    <el-input
                      v-model="newComment"
                      type="textarea"
                      :rows="4"
                      placeholder="请输入您的评论..."
                      maxlength="500"
                      show-word-limit
                    />
                    <div class="comment-upload-row">
                      <span class="upload-label">添加图片（可选，最多9张）</span>
                      <el-upload
                        :action="uploadUrl"
                        :headers="uploadHeaders"
                        :show-file-list="false"
                        :before-upload="beforeCommentImage"
                        :on-success="onCommentImageSuccess"
                        accept="image/*"
                      >
                        <el-button size="small">上传图片</el-button>
                      </el-upload>
                    </div>
                    <div v-if="commentImages.length" class="root-thumbs">
                      <div v-for="(p, i) in commentImages" :key="i" class="thumb-cell">
                        <el-image :src="getImageUrl(p)" fit="cover" class="thumb-img" />
                        <el-button text type="danger" size="small" @click="removeRootImage(i)">移除</el-button>
                      </div>
                    </div>
                    <div class="comment-actions">
                      <el-button
                        class="heritage-submit-btn"
                        :loading="commentSubmitting"
                        round
                        @click="submitComment"
                      >
                        发表评论
                      </el-button>
                    </div>
                  </div>
                </div>
                <div class="comment-list" v-loading="commentLoading">
                  <h3 class="comment-block-title">评论列表（{{ totalCommentCount }}）</h3>
                  <div v-if="comments.length > 0" class="comments-tree">
                    <CommentThread
                      v-for="c in comments"
                      :key="c.id"
                      :comment="c"
                      :depth="0"
                      @submit-reply="handleReplySubmit"
                    />
                  </div>
                  <el-empty v-else description="暂无评论" />
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </section>
    </div>

    <div v-else-if="!loading" class="empty-container">
      <div class="empty-content">
        <div class="empty-icon">📦</div>
        <h3 class="empty-title">文物不存在</h3>
        <p class="empty-description">该内容可能已被删除或不存在</p>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.heritage-detail-page {
  --accent: #a82c28;
  --accent-light: #d0453c;
  --accent-deep: #6e1a1a;
  --ink: #1f1a17;
  --muted: #5c534c;
  --card: #ffffff;
  --line: rgba(31, 26, 23, 0.08);
  --shadow: 0 8px 32px rgba(62, 18, 18, 0.08);
  min-height: 100vh;
  position: relative;
  background: linear-gradient(180deg, #f0e4e2 0%, #f6ece9 30%, #faf6f4 100%);
  padding-bottom: 48px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 50vh;
  .loading-text {
    font-size: 16px;
    color: var(--muted);
  }
}

.heritage-detail {
  max-width: 1120px;
  margin: 0 auto;
  padding: 28px 24px 0;
}

.breadcrumb {
  font-size: 13px;
  color: var(--muted);
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;

  a {
    color: var(--accent);
    font-weight: 600;
    &:hover {
      opacity: 0.88;
    }
  }
  .breadcrumb-sep {
    opacity: 0.45;
    user-select: none;
  }
  .breadcrumb-current {
    color: var(--ink);
    font-weight: 700;
  }
}

.heritage-info-section {
  position: relative;
  background: var(--card);
  border-radius: 20px;
  padding: 32px 36px 36px;
  margin-bottom: 28px;
  box-shadow: var(--shadow);
  border: 1px solid var(--line);
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0 0 auto 0;
    height: 4px;
    background: linear-gradient(90deg, #5c1818, #a82c28, #d06052, #c9957a);
    opacity: 0.95;
  }

  .section-kicker {
    font-size: 12px;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    color: var(--accent);
    font-weight: 700;
    margin-bottom: 12px;
  }
}

.heritage-info-grid {
  display: grid;
  grid-template-columns: minmax(280px, 400px) 1fr;
  gap: 36px;
  align-items: start;
}

.cover-frame {
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid var(--line);
  box-shadow: 0 12px 40px rgba(31, 26, 23, 0.1);
  background: #f6ecea;
}

.cover-image {
  width: 100%;
  height: 300px;
  display: block;
}

.image-error,
.no-image {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 260px;
  background: #f6ecea;
  color: var(--muted);
  font-size: 14px;
}

.heritage-info {
  .title-row {
    margin-bottom: 12px;
  }

  .heritage-title {
    margin: 0;
    font-size: clamp(24px, 3vw, 34px);
    font-weight: 800;
    color: var(--ink);
    line-height: 1.25;
    letter-spacing: -0.02em;
  }

  .heritage-lead {
    margin: 0 0 22px;
    font-size: 15px;
    line-height: 1.65;
    color: var(--muted);
    max-width: 52em;
  }

  .quick-stats {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 12px;
    margin-bottom: 22px;
  }

  .stat-chip {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 14px 16px;
    border-radius: 14px;
    background: linear-gradient(135deg, rgba(168, 44, 40, 0.1), rgba(208, 69, 60, 0.06));
    border: 1px solid var(--line);
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
    color: var(--accent);
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
    color: var(--muted);
  }

  .stat-value {
    font-size: 14px;
    font-weight: 600;
    color: var(--ink);
    line-height: 1.45;
    word-break: break-word;
  }

  .intro-card {
    padding: 18px 20px;
    border-radius: 14px;
    background: rgba(168, 44, 40, 0.07);
    border: 1px solid var(--line);
    margin-bottom: 24px;
  }

  .intro-card-title {
    margin: 0 0 10px;
    font-size: 15px;
    font-weight: 800;
    color: var(--ink);
  }

  .intro-card-text {
    margin: 0;
    font-size: 15px;
    line-height: 1.75;
    color: var(--muted);
  }

  .action-section {
    .collect-btn {
      min-width: 200px;
      height: 48px;
      font-weight: 700;
      border: none;
      color: #fff;
      background: linear-gradient(135deg, var(--accent) 0%, var(--accent-light) 100%);
      box-shadow: 0 6px 22px rgba(110, 26, 26, 0.35);

      &:hover {
        opacity: 0.95;
        filter: brightness(1.05);
      }

      &.collect-btn--active {
        background: linear-gradient(135deg, #5a1518 0%, #8b2424 100%);
        box-shadow: 0 6px 22px rgba(40, 10, 10, 0.4);
      }
    }
  }
}

.heritage-tabs-section {
  background: var(--card);
  border-radius: 20px;
  padding: 28px 32px 36px;
  box-shadow: var(--shadow);
  border: 1px solid var(--line);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4px;
    background: linear-gradient(180deg, #6e1a1a, #c53d2e);
  }
}

.tabs-inner {
  padding-left: 8px;
}

.tabs-head {
  margin-bottom: 8px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--line);
}

.tabs-title {
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 800;
  color: var(--ink);
  letter-spacing: -0.02em;
}

.tabs-sub {
  margin: 0;
  font-size: 14px;
  color: var(--muted);
}

.content-body {
  padding-top: 20px;
  font-size: 16px;
  line-height: 1.9;
  color: #3d3833;

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
    color: var(--ink);
    margin: 24px 0 12px;
    font-weight: 700;
  }

  &.content-body--empty {
    margin-top: 16px;
    padding: 28px 20px;
    text-align: center;
    background: #faf4f3;
    border-radius: 14px;
    border: 1px dashed rgba(31, 26, 23, 0.12);
  }

  .empty-placeholder {
    margin: 0;
    color: var(--muted);
    font-size: 15px;
  }
}

.comment-section {
  padding-top: 8px;
}

.add-comment {
  margin-bottom: 28px;
  padding: 20px;
  background: #faf4f2;
  border-radius: 14px;
  border: 1px solid var(--line);
}

.comment-block-title {
  font-size: 17px;
  font-weight: 800;
  color: var(--ink);
  margin: 0 0 14px;
}

.comment-login-hint {
  font-size: 13px;
  color: var(--muted);
  margin: 0 0 14px;
  line-height: 1.55;
}

.comment-upload-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
  .upload-label {
    font-size: 12px;
    color: #7a6a62;
  }
}

.root-thumbs {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
}
.thumb-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}
.thumb-img {
  width: 88px;
  height: 88px;
  border-radius: 10px;
}

.comments-tree {
  margin-top: 8px;
}

.comment-actions {
  margin-top: 14px;
  text-align: right;
}

.heritage-submit-btn {
  border: none;
  color: #fff;
  font-weight: 700;
  background: linear-gradient(135deg, var(--accent), var(--accent-light));
  box-shadow: 0 4px 16px rgba(110, 26, 26, 0.28);
  &:hover {
    opacity: 0.95;
    filter: brightness(1.05);
  }
}

.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 55vh;
  .empty-content {
    text-align: center;
  }
  .empty-icon {
    font-size: 56px;
    margin-bottom: 12px;
  }
  .empty-title {
    font-size: 18px;
    font-weight: 700;
    color: var(--ink);
    margin: 0 0 8px;
  }
  .empty-description {
    font-size: 14px;
    color: var(--muted);
    margin: 0;
  }
}

:deep(.heritage-tabs.el-tabs) {
  .el-tabs__header {
    margin-bottom: 0;
  }
  .el-tabs__nav-wrap::after {
    height: 1px;
    background: var(--line);
  }
  .el-tabs__item {
    font-size: 15px;
    font-weight: 600;
    color: var(--muted);
    &.is-active {
      color: var(--accent);
    }
    &:hover {
      color: var(--accent-light);
    }
  }
  .el-tabs__active-bar {
    height: 3px;
    border-radius: 2px;
    background: linear-gradient(90deg, var(--accent), var(--accent-light));
  }
}

@media (max-width: 768px) {
  .heritage-detail {
    padding: 16px 16px 0;
  }
  .heritage-info-section {
    padding: 24px 20px 28px;
  }
  .heritage-info-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  .cover-image {
    height: 220px;
  }
  .heritage-tabs-section {
    padding: 22px 18px 28px;
  }
  .quick-stats {
    grid-template-columns: 1fr !important;
  }
  .action-section .collect-btn {
    width: 100%;
    min-width: 0;
  }
}
</style>
