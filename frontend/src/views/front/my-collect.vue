<script setup lang="ts">
import http from "../../utils/http";
import { Delete } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { onMounted, ref, computed } from "vue";
import { useRouter } from "vue-router";
import useUserStore from "../../stores/userStore";

const router = useRouter();
const userStore = useUserStore();

const initParams = {
  pageNum: 1,
  pageSize: 10,
};

// 搜索条件
const params = ref({ ...initParams });

// 表格数据
const tableData = ref([]);
// 表格数据总数
const total = ref(0);
// 加载状态
const loading = ref(false);

// 获取当前用户信息
const currentUser = computed(() => {
  try {
    return typeof userStore.userInfo === 'string' ? JSON.parse(userStore.userInfo) : userStore.userInfo
  } catch {
    return {}
  }
})

// 获取表格数据
const getTableData = async () => {
  if (!currentUser.value?.id) {
    ElMessage.error("请先登录");
    return;
  }
  
  loading.value = true;
  try {
    const res = await http.get(
      `/collect/pageByUserId?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}&userId=${currentUser.value.id}`
    );
    if (res.code === 200) {
      tableData.value = res.data.records;
      total.value = res.data.total;
    } else {
      ElMessage.error("获取收藏列表失败");
    }
  } catch (error) {
    ElMessage.error("获取收藏列表失败");
  } finally {
    loading.value = false;
  }
};

// 取消收藏
const cancelCollect = async (id: number) => {
  try {
    const res = await http.get(`/collect/del?id=${id}`);
    if (res.code === 200) {
      ElMessage.success("取消收藏成功");
      await getTableData();
    } else {
      ElMessage.error("取消收藏失败");
    }
  } catch (error) {
    ElMessage.error("取消收藏失败");
  }
};

// 跳转到文物详情页
const goToHeritageDetail = (heritageId: number) => {
  router.push({
    path: '/front/heritageDetail',
    query: { id: heritageId }
  });
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
  <div class="my-collect-container">
    <div class="page-header">
      <h2>我的收藏</h2>
      <p class="page-desc">管理您收藏的非遗文物</p>
    </div>
    
    <main>
      <!-- 表格 -->
      <el-table
        :data="tableData"
        border
        v-loading="loading"
        empty-text="暂无收藏的文物"
      >
        <el-table-column prop="heritageName" label="文物名称" show-overflow-tooltip>
          <template #default="scope">
            <el-button 
              type="text" 
              @click="goToHeritageDetail(scope.row.heritageId)"
              class="heritage-name-btn"
            >
              {{ scope.row.heritageName }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="收藏时间" width="180" />
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="scope">
            <el-popconfirm
              title="确定要取消收藏吗?"
              @confirm="cancelCollect(scope.row.id)"
            >
              <template #reference>
                <el-button
                  type="danger"
                  :icon="Delete"
                  size="small"
                >
                  取消收藏
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="params.pageNum"
          v-model:page-size="params.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="sizes, prev, pager, next"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </main>
  </div>
</template>

<style lang="scss" scoped>
.my-collect-container {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.page-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
  
  h2 {
    margin: 0 0 8px 0;
    color: #303133;
    font-size: 24px;
    font-weight: 600;
  }
  
  .page-desc {
    margin: 0;
    color: #909399;
    font-size: 14px;
  }
}

.heritage-name-btn {
  color: #409eff;
  font-weight: 500;
  
  &:hover {
    color: #66b1ff;
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

:deep(.el-table) {
  --el-table-header-bg-color: var(--el-fill-color-light);
}

:deep(.el-button--text) {
  padding: 0;
  height: auto;
  border: none;
}
</style>