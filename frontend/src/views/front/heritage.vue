<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElInput, ElButton, ElSelect, ElOption, ElPagination, ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import http from '../../utils/http'
import { getImageUrl } from '../../utils/system'
import HeritageCard from '../../components/HeritageCard.vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 初始搜索参数
const initParams = {
  pageNum: 1,
  pageSize: 12,
  name: "",
  categoryId: null
}

// 搜索条件
const params = ref({ ...initParams })

// 数据
const heritageList = ref([])
const total = ref(0)
const loading = ref(false)

// 获取非遗文物列表
const getHeritageList = async () => {
  loading.value = true
  try {
    const res = await http.get(
      `/culturalHeritage/page?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}&name=${params.value.name}`
    )
    if (res.code === 200) {
      heritageList.value = res.data.records.map(item => {
        item.coverImage = getImageUrl(item.coverImage)
        return item
      })
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取非遗文物列表失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = async () => {
  params.value.pageNum = 1
  await getHeritageList()
}

// 重置
const handleReset = () => {
  params.value = { ...initParams }
  getHeritageList()
}

// 分页大小改变
const handleSizeChange = (val: number) => {
  params.value.pageSize = val
  params.value.pageNum = 1
  getHeritageList()
}

// 当前页改变
const handleCurrentChange = (val: number) => {
  params.value.pageNum = val
  getHeritageList()
}

// 计算属性
const hasData = computed(() => heritageList.value.length > 0)

onMounted(async () => {
  await getHeritageList()
})
</script>

<template>
  <div class="heritage-page">
    <!-- 搜索表单 -->
    <div class="search-section">
      <div class="search-form">
        <div class="form-item">
          <label class="form-label">名称：</label>
          <el-input 
            v-model="params.name" 
            placeholder="请输入非遗文物名称" 
            clearable
            class="search-input"
          />
        </div>
        <div class="form-actions">
          <el-button type="primary" :icon="Search" @click="handleSearch" :loading="loading">
            搜索
          </el-button>
          <el-button type="default" :icon="Refresh" @click="handleReset">
            重置
          </el-button>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content-section">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-text">加载中...</div>
      </div>

      <!-- 数据展示 -->
      <div v-else-if="hasData" class="heritage-container">
        <div class="heritage-grid">
          <HeritageCard 
            v-for="item in heritageList" 
            :key="item.id"
            :heritage="item"
          />
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-container">
        <div class="empty-content">
          <div class="empty-icon">📦</div>
          <h3 class="empty-title">暂无数据</h3>
          <p class="empty-description">没有找到相关的非遗文物，请尝试调整搜索条件</p>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="hasData" class="pagination-section">
      <el-pagination
        v-model:current-page="params.pageNum"
        v-model:page-size="params.pageSize"
        :page-sizes="[12, 24, 36, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
// 系统主题色
:root {
  --primary-color: #3498db;
  --primary-light: #5dade2;
  --primary-dark: #2980b9;
  --secondary-color: #2c3e50;
  --accent-color: #e74c3c;
  --success-color: #27ae60;
  --warning-color: #f39c12;
  --text-primary: #2c3e50;
  --text-secondary: #7f8c8d;
  --text-light: #bdc3c7;
  --bg-primary: #ffffff;
  --bg-secondary: #f8f9fa;
  --bg-light: #ecf0f1;
  --border-color: #e9ecef;
  --shadow-light: 0 2px 12px rgba(0, 0, 0, 0.08);
  --shadow-medium: 0 4px 20px rgba(0, 0, 0, 0.12);
  --shadow-hover: 0 8px 32px rgba(0, 0, 0, 0.16);
}

.heritage-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 0;
}

// 搜索区域
.search-section {
  background: var(--bg-primary);
  padding: 32px 0;
  box-shadow: var(--shadow-light);
  position: relative;
  z-index: 2;
  border-radius: 20px 20px 0 0;
  
  .search-form {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px;
    display: flex;
    align-items: flex-end;
    gap: 24px;
    flex-wrap: wrap;
  }
  
  .form-item {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .form-label {
      font-size: 14px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0;
    }
    
    .search-input {
      width: 280px;
    }
    
    .search-select {
      width: 200px;
    }
  }
  
  .form-actions {
    display: flex;
    gap: 12px;
    margin-left: auto;
  }
}

// 内容区域
.content-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
}

// 加载状态
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  
  .loading-text {
    font-size: 16px;
    color: var(--text-secondary);
  }
}

// 非遗文物网格
.heritage-container {
  .heritage-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 32px;
    padding: 0;
  }
}

// 空状态
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  
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

// 分页区域
.pagination-section {
  display: flex;
  justify-content: center;
  padding: 40px 24px 60px;
  background: var(--bg-primary);
  
  :deep(.el-pagination) {
    .el-pager li {
      border-radius: 8px;
      margin: 0 4px;
      
      &.is-active {
        background: var(--primary-color);
        color: white;
      }
    }
    
    .btn-prev,
    .btn-next {
      border-radius: 8px;
      margin: 0 4px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .page-header {
    padding: 40px 0 60px;
    
    .page-title {
      font-size: 32px;
      flex-direction: column;
      gap: 8px;
      
      .title-icon {
        font-size: 36px;
      }
    }
    
    .page-subtitle {
      font-size: 16px;
    }
  }
  
  .search-section {
    padding: 24px 0;
    margin-top: -30px;
    
    .search-form {
      flex-direction: column;
      align-items: stretch;
      gap: 16px;
      
      .form-item {
        .search-input,
        .search-select {
          width: 100%;
        }
      }
      
      .form-actions {
        margin-left: 0;
        justify-content: center;
      }
    }
  }
  
  .content-section {
    padding: 24px 16px;
  }
  
  .heritage-container {
    .heritage-grid {
      grid-template-columns: 1fr;
      gap: 20px;
    }
  }
  
  .pagination-section {
    padding: 24px 16px 40px;
    
    :deep(.el-pagination) {
      .el-pagination__sizes,
      .el-pagination__jump {
        display: none;
      }
    }
  }
}

@media (max-width: 480px) {
  .page-header {
    .page-title {
      font-size: 28px;
    }
  }
  
  .heritage-container {
    .heritage-grid {
      grid-template-columns: 1fr;
      gap: 16px;
    }
  }
}
</style>