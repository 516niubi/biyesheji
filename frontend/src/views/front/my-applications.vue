<script setup lang="ts">
import http from "../../utils/http";
import { Search, Refresh } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { computed, onMounted, ref } from "vue";
import useUserStore from "../../stores/userStore";

const userStore = useUserStore();

const initParams = {
  pageNum: 1,
  pageSize: 10,
  realName: "",
};
// 搜索条件
const params = ref({ ...initParams });
// 表格数据
const tableData = ref([]);
// 表格数据总数
const total = ref(0);
// 加载状态
const loading = ref(false);

// 状态字典
const statusDict = {
  0: "待审核",
  1: "已通过",
  2: "已拒绝",
};

// 状态颜色
const getStatusType = (status: number) => {
  switch (status) {
    case 0:
      return "warning";
    case 1:
      return "success";
    case 2:
      return "danger";
    default:
      return "info";
  }
};

// 获取表格数据
const getTableData = async () => {
  if (!userStore.userInfo?.id) {
    ElMessage.error("请先登录");
    return;
  }
  
  loading.value = true;
  try {
    const res = await http.get(
      `/activityApplication/pageByUserId?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}&userId=${userStore.userInfo.id}&realName=${params.value.realName}`
    );
    if (res.code === 200) {
      tableData.value = res.data.records;
      total.value = res.data.total;
    } else {
      ElMessage.error(res.msg || "获取数据失败");
    }
  } catch (error) {
    console.error("获取报名数据失败:", error);
    ElMessage.error("获取数据失败");
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = async () => {
  params.value.pageNum = 1;
  await getTableData();
};

// 重置
const handleReset = () => {
  params.value = { ...initParams };
  getTableData();
};

const handleSizeChange = (val: number) => {
  params.value.pageSize = val;
  getTableData();
};

const handleCurrentChange = (val: number) => {
  params.value.pageNum = val;
  getTableData();
};

onMounted(async () => {
  await getTableData();
});
</script>

<template>
  <div class="my-applications-container">
    <div class="page-header">
      <h2 class="page-title">我的报名</h2>
      <p class="page-desc">查看您的活动报名记录和审核状态</p>
    </div>

    <div class="content-card">
      <!-- 搜索区域 -->
      <header class="search-header">
        <div class="search-form">
          <div class="form-item">
            <label class="label-text">姓名：</label>
            <el-input 
              v-model="params.realName" 
              placeholder="请输入真实姓名" 
              style="width: 200px;"
              clearable
            />
          </div>
          <div class="form-actions">
            <el-button type="primary" :icon="Search" @click="handleSearch">
              搜索
            </el-button>
            <el-button type="warning" :icon="Refresh" @click="handleReset">
              重置
            </el-button>
          </div>
        </div>
      </header>

      <!-- 表格区域 -->
      <main class="table-main">
        <el-table
          :data="tableData"
          border
          v-loading="loading"
          empty-text="暂无报名记录"
        >
          <el-table-column prop="activityTitle" label="活动名称" show-overflow-tooltip />
          <el-table-column prop="realName" label="真实姓名" width="100" />
          <el-table-column prop="phone" label="联系电话" width="130" />
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          <el-table-column prop="status" label="审核状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ statusDict[scope.row.status] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="报名时间" width="180" />
          <el-table-column prop="updateTime" label="更新时间" width="180" />
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="params.pageNum"
            v-model:page-size="params.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="sizes, prev, pager, next, total"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </main>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.my-applications-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
  
  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px 0;
  }
  
  .page-desc {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }
}

.content-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.search-header {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
  background-color: #fafafa;
}

.search-form {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.form-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label-text {
  color: #606266;
  font-size: 14px;
  white-space: nowrap;
}

.form-actions {
  display: flex;
  gap: 12px;
}

.table-main {
  padding: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

:deep(.el-table) {
  --el-table-header-bg-color: var(--el-fill-color-light);
}

:deep(.el-table th) {
  background-color: #f8f9fa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table td) {
  padding: 12px 0;
}

:deep(.el-tag) {
  font-weight: 500;
}

// 响应式设计
@media (max-width: 768px) {
  .my-applications-container {
    padding: 10px;
  }
  
  .search-form {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .form-item {
    flex-direction: column;
    align-items: stretch;
    gap: 5px;
  }
  
  .form-actions {
    justify-content: center;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
}
</style>