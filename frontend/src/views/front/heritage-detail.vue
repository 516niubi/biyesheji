<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import http from '../../utils/http'
import { getImageUrl } from '../../utils/system'
import { ElMessage } from 'element-plus'
import { Star, StarFilled, User } from '@element-plus/icons-vue'
import useUserStore from '../../stores/userStore'

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
const commentSubmitting = ref(false)

// 获取当前用户信息
const currentUser = computed(() => {
  try {
    return typeof userStore.userInfo === 'string' ? JSON.parse(userStore.userInfo) : userStore.userInfo
  } catch {
    return {}
  }
})

// 获取非遗文物详情
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
      // 获取收藏状态
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

// 检查收藏状态
const checkCollectStatus = async () => {
  if (!currentUser.value?.id || !heritageDetail.value?.id) return
  
  try {
    const res = await http.get(`/collect/isCollected?userId=${currentUser.value.id}&heritageId=${heritageDetail.value.id}`)
    if (res.code === 200) {
      isCollected.value = res.data
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

// 收藏/取消收藏
const toggleCollect = async () => {
  if (!currentUser.value?.id) {
    ElMessage.warning('请先登录')
    return
  }
  
  collectLoading.value = true
  try {
    if (isCollected.value) {
      // 取消收藏 - 需要先查询收藏记录ID
      const collectRes = await http.get(`/collect/pageByUserId?pageNum=1&pageSize=1000&userId=${currentUser.value.id}`)
      if (collectRes.code === 200) {
        const collectRecord = collectRes.data.records.find((item: any) => item.heritageId === heritageDetail.value.id)
        if (collectRecord) {
          await http.get(`/collect/del?id=${collectRecord.id}`)
          isCollected.value = false
          ElMessage.success('取消收藏成功')
        }
      }
    } else {
      // 添加收藏
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

// 获取评论列表
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

// 提交评论
const submitComment = async () => {
  if (!currentUser.value?.id) {
    ElMessage.warning('请先登录')
    return
  }
  
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  commentSubmitting.value = true
  try {
    const res = await http.post('/comment/add', {
      heritageId: heritageDetail.value.id,
      userId: currentUser.value.id,
      content: newComment.value.trim()
    })
    if (res.code === 200) {
      ElMessage.success('评论成功')
      newComment.value = ''
      await getComments()
    }
  } catch (error) {
    ElMessage.error('评论失败')
  } finally {
    commentSubmitting.value = false
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

// Tab切换时加载评论
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
  <div class="heritage-detail-container">
    <el-card v-loading="loading" class="detail-card">
      <template v-if="!loading && heritageDetail.id">
        <!-- 头部信息区域 -->
        <div class="header-section">
          <!-- 左侧封面图片 -->
          <div class="cover-section">
            <el-image
              v-if="heritageDetail.coverImage"
              :src="getImageUrl(heritageDetail.coverImage)"
              :alt="heritageDetail.name"
              class="cover-image"
              fit="cover"
              :preview-src-list="[getImageUrl(heritageDetail.coverImage)]"
              :preview-teleported="true"
              :hide-on-click-modal="true"
            >
              <template #error>
                <div class="image-error">
                  <span>图片加载失败</span>
                </div>
              </template>
            </el-image>
            <div v-else class="no-image">
              <span>暂无图片</span>
            </div>
          </div>
          
          <!-- 右侧基本信息 -->
          <div class="info-section">
            <h1 class="heritage-title">{{ heritageDetail.name }}</h1>
            
            <div class="meta-info">
              <div class="meta-item">
                <span class="label">发布时间：</span>
                <span>{{ formatDate(heritageDetail.createTime) }}</span>
              </div>
              <div class="meta-item">
                <span class="label">浏览量：</span>
                <span>{{ heritageDetail.viewCount || 0 }}</span>
              </div>
            </div>
            
            <!-- 简介 -->
            <div v-if="heritageDetail.intro" class="intro-section">
              <h3 class="section-title">简介</h3>
              <p class="intro-text">{{ heritageDetail.intro }}</p>
            </div>
            
            <!-- 收藏按钮 -->
            <div class="action-section">
              <el-button
                :type="isCollected ? 'warning' : 'primary'"
                :icon="isCollected ? StarFilled : Star"
                :loading="collectLoading"
                @click="toggleCollect"
                size="large"
              >
                {{ isCollected ? '已收藏' : '收藏' }}
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- Tabs区域 -->
        <div class="tabs-section">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <!-- 详情Tab -->
            <el-tab-pane label="详情" name="detail">
              <div v-if="heritageDetail.description" class="content-section">
                <div class="content-text" v-html="heritageDetail.description"></div>
              </div>
              <el-empty v-else description="暂无详细描述" />
            </el-tab-pane>
            
            <!-- 评论Tab -->
            <el-tab-pane label="评论" name="comment">
              <div class="comment-section">
                <!-- 新增评论 -->
                <div class="add-comment">
                  <h3 class="comment-title">发表评论</h3>
                  <div class="comment-input-area">
                    <el-input
                      v-model="newComment"
                      type="textarea"
                      :rows="4"
                      placeholder="请输入您的评论..."
                      maxlength="500"
                      show-word-limit
                    />
                    <div class="comment-actions">
                      <el-button
                        type="primary"
                        :loading="commentSubmitting"
                        @click="submitComment"
                      >
                        发表评论
                      </el-button>
                    </div>
                  </div>
                </div>
                
                <!-- 评论列表 -->
                <div class="comment-list" v-loading="commentLoading">
                  <h3 class="comment-title">评论列表 ({{ comments.length }})</h3>
                  <div v-if="comments.length > 0" class="comments">
                    <div v-for="comment in comments" :key="comment.id" class="comment-item">
                      <div class="comment-avatar">
                        <el-avatar :src="getImageUrl(comment.avatar)" size="large" />
                      </div>
                      <div class="comment-content">
                        <div class="comment-header">
                          <span class="comment-author">{{ comment.userName || '匿名用户' }}</span>
                          <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                        </div>
                        <div class="comment-text">{{ comment.content }}</div>
                      </div>
                    </div>
                  </div>
                  <el-empty v-else description="暂无评论" />
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </template>
      
      <!-- 空状态 -->
      <el-empty v-else-if="!loading" description="暂无数据" />
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.heritage-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  
  .detail-card {
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    
    // 头部区域
    .header-section {
      display: flex;
      gap: 30px;
      margin-bottom: 30px;
      padding-bottom: 30px;
      border-bottom: 2px solid #f0f0f0;
      
      // 左侧封面
      .cover-section {
        flex: 0 0 400px;
        
        .cover-image {
          width: 100%;
          height: 300px;
          border-radius: 12px;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        
        .image-error,
        .no-image {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 100%;
          height: 300px;
          background: #f5f5f5;
          border-radius: 12px;
          color: #999;
          font-size: 16px;
        }
      }
      
      // 右侧信息
      .info-section {
        flex: 1;
        display: flex;
        flex-direction: column;
        
        .heritage-title {
          font-size: 28px;
          font-weight: bold;
          color: #2c3e50;
          margin: 0 0 20px 0;
          line-height: 1.4;
        }
        
        .meta-info {
          margin-bottom: 20px;
          
          .meta-item {
            display: flex;
            align-items: center;
            margin-bottom: 10px;
            color: #666;
            font-size: 14px;
            
            .label {
              font-weight: 600;
              margin-right: 8px;
              color: #333;
            }
          }
        }
        
        .intro-section {
          margin-bottom: 20px;
          flex: 1;
          
          .section-title {
            font-size: 18px;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 12px;
            padding-bottom: 6px;
            border-bottom: 1px solid #e0e0e0;
          }
          
          .intro-text {
            font-size: 15px;
            line-height: 1.6;
            color: #555;
            text-align: justify;
            margin: 0;
          }
        }
        
        .action-section {
          margin-top: auto;
          
          .el-button {
            padding: 12px 24px;
            font-size: 16px;
          }
        }
      }
    }
    
    // Tabs区域
    .tabs-section {
      .content-section {
        padding: 20px 0;
        
        .content-text {
          font-size: 15px;
          line-height: 1.8;
          color: #333;
          text-align: justify;
          
          :deep(img) {
            max-width: 100%;
            height: auto;
            border-radius: 8px;
            margin: 15px 0;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
          }
          
          :deep(p) {
            margin-bottom: 15px;
          }
          
          :deep(h1, h2, h3, h4, h5, h6) {
            margin: 25px 0 15px 0;
            color: #2c3e50;
          }
        }
      }
      
      // 评论区域
      .comment-section {
        .add-comment {
          margin-bottom: 30px;
          padding: 20px;
          background: #f8f9fa;
          border-radius: 12px;
          
          .comment-title {
            font-size: 18px;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 15px;
          }
          
          .comment-input-area {
            .comment-actions {
              margin-top: 15px;
              text-align: right;
            }
          }
        }
        
        .comment-list {
          .comment-title {
            font-size: 18px;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 1px solid #e0e0e0;
          }
          
          .comments {
            .comment-item {
              display: flex;
              gap: 15px;
              padding: 20px 0;
              border-bottom: 1px solid #f0f0f0;
              
              &:last-child {
                border-bottom: none;
              }
              
              .comment-avatar {
                flex: 0 0 auto;
              }
              
              .comment-content {
                flex: 1;
                
                .comment-header {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  margin-bottom: 8px;
                  
                  .comment-author {
                    font-weight: 600;
                    color: #2c3e50;
                    font-size: 14px;
                  }
                  
                  .comment-time {
                    color: #999;
                    font-size: 12px;
                  }
                }
                
                .comment-text {
                  color: #555;
                  line-height: 1.6;
                  font-size: 14px;
                }
              }
            }
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .heritage-detail-container {
    padding: 10px;
    
    .detail-card {
      .header-section {
        flex-direction: column;
        gap: 20px;
        
        .cover-section {
          flex: none;
          
          .cover-image,
          .image-error,
          .no-image {
            height: 200px;
          }
        }
        
        .info-section {
          .heritage-title {
            font-size: 22px;
          }
          
          .meta-info {
            .meta-item {
              flex-direction: column;
              align-items: flex-start;
              gap: 5px;
            }
          }
        }
      }
      
      .tabs-section {
        .comment-section {
          .comment-list {
            .comments {
              .comment-item {
                .comment-content {
                  .comment-header {
                    flex-direction: column;
                    align-items: flex-start;
                    gap: 5px;
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
</style>