<script setup lang="ts">
import http from "../../utils/http";
import { Search, Refresh, Delete, ChatDotRound } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { computed, onMounted, ref } from "vue";
import { getImageUrl, hasPermission } from "../../utils/system";

const initParams = {
  pageNum: 1,
  pageSize: 10,
  title: "",
  status: null,
};

// 搜索条件
const params = ref({ ...initParams });

// 表格数据
const tableData = ref([]);
// 表格数据总数
const total = ref(0);
// 控制回复弹窗显示
const replyVisible = ref(false);
// 当前回复的反馈
const currentFeedback = ref({});
// 回复内容
const replyContent = ref("");

// 批量删除相关
const multipleSelection = ref([]);
const selectedIds = computed(() => {
  return multipleSelection.value.map((item) => item.id);
});
const hasSelected = computed(() => {
  return multipleSelection.value.length > 0;
});

// 状态选项
const statusOptions = [
  { label: "未回复", value: 0 },
  { label: "已回复", value: 1 },
];

// 获取表格数据
const getTableData = async () => {
  let url = `/feedback/page?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}`;
  if (params.value.title) url += `&title=${params.value.title}`;
  if (params.value.status !== null) url += `&status=${params.value.status}`;
  
  const res = await http.get(url);
  if (res.code === 200) {
    tableData.value = res.data.records;
    total.value = res.data.total;
  }
};

// 搜索
const handleSearch = async () => {
  await getTableData();
};

// 重置搜索
const handleReset = () => {
  params.value = { ...initParams };
  getTableData();
};

// 显示回复弹窗
const showReply = (row) => {
  currentFeedback.value = row;
  replyContent.value = row.reply || "";
  replyVisible.value = true;
};

// 提交回复
const submitReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning("请输入回复内容");
    return;
  }
  
  const updateData = {
    ...currentFeedback.value,
    reply: replyContent.value,
    status: 1, // 设置为已回复状态
  };
  
  const res = await http.post(`/feedback/edit?id=${currentFeedback.value.id}`, updateData);
  if (res.code === 200) {
    ElMessage.success("回复成功");
    replyVisible.value = false;
    await getTableData();
  } else {
    ElMessage.error("回复失败");
  }
};

// 删除
const delRow = async (id: number) => {
  const res = await http.get(`/feedback/del?id=${id}`);
  if (res.code === 200) {
    ElMessage.success("删除成功");
    await getTableData();
  } else {
    ElMessage.error("删除失败");
  }
};

// 批量删除
const batchDel = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning("请先选择要删除的数据");
    return;
  }
  const res = await http.post("/feedback/batchDel", selectedIds.value);
  if (res.code === 200) {
    ElMessage.success("批量删除成功");
    multipleSelection.value = [];
    await getTableData();
  } else {
    ElMessage.error("批量删除失败");
  }
};

// 多选
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection;
};

const handleSizeChange = (val: number) => {
  params.value.pageSize = val;
  getTableData();
};

const handleCurrentChange = (val: number) => {
  params.value.pageNum = val;
  getTableData();
};

// 获取状态标签类型
const getStatusType = (status) => {
  return status === 1 ? "success" : "warning";
};

// 获取状态文本
const getStatusText = (status) => {
  return status === 1 ? "已回复" : "未回复";
};

onMounted(async () => {
  await getTableData();
});
</script>

<template>
  <div>
    <header class="flex">
      <div class="flex form-item">
        <p class="label-text">标题：</p>
        <el-input v-model="params.title" placeholder="请输入反馈标题" />
      </div>
      <div class="flex form-item">
        <p class="label-text">状态：</p>
        <el-select v-model="params.status" placeholder="请选择状态" clearable style="width: 120px;">
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </div>
      <el-button type="primary" :icon="Search" @click="handleSearch"
        >搜索</el-button
      >
      <el-button type="warning" :icon="Refresh" @click="handleReset"
        >重置</el-button
      >
    </header>
    <main>
      <div class="flex op-box">
        <el-button
          v-if="hasPermission('feedback', '删除')"
          :icon="Delete"
          type="danger"
          :disabled="!hasSelected"
          @click="batchDel"
          >批量删除</el-button
        >
      </div>
      <!-- 表格 -->
      <el-table
        :data="tableData"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="反馈标题" show-overflow-tooltip />
        <el-table-column prop="content" label="反馈内容" show-overflow-tooltip />
        <el-table-column prop="userName" label="反馈用户" width="120" />
        <el-table-column prop="img" label="反馈图片" width="100">
          <template #default="scope">
            <el-image
              v-if="getImageUrl(scope.row.img)"
              :src="getImageUrl(scope.row.img)"
              style="width: 60px; height: 40px"
              fit="cover"
              :preview-src-list="getImageUrl(scope.row.img)"
              :preview-teleported="true"
              :hide-on-click-modal="true"
              :lazy="true"
            >
              <template #error>
                <div class="image-slot">
                  <span>无图片</span>
                </div>
              </template>
            </el-image>
            <span v-else>无图片</span>
          </template>
        </el-table-column>
        <el-table-column prop="reply" label="回复内容" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.reply">{{ scope.row.reply }}</span>
            <span v-else class="no-reply">暂无回复</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <el-button
              v-if="scope.row.status !== 1"
              type="primary"
              :icon="ChatDotRound"
              @click="showReply(scope.row)"
              >回复</el-button
            >
            <el-popconfirm
              title="您确定要删除吗?"
              @confirm="delRow(scope.row.id)"
            >
              <template #reference>
                <el-button
                  v-if="hasPermission('feedback', '删除')"
                  type="danger"
                  :icon="Delete"
                  >删除</el-button
                >
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
    
    <!-- 回复弹窗 -->
    <el-dialog
      v-model="replyVisible"
      title="回复反馈"
      width="600px"
      :before-close="() => replyVisible = false"
    >
      <div class="feedback-info">
        <h4>反馈信息</h4>
        <p><strong>标题：</strong>{{ currentFeedback.title }}</p>
        <p><strong>内容：</strong>{{ currentFeedback.content }}</p>
        <p><strong>用户：</strong>{{ currentFeedback.userName }}</p>
      </div>
      <el-form label-width="80px">
        <el-form-item label="回复内容">
          <el-input
            v-model="replyContent"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitReply">确认回复</el-button>
          <el-button @click="replyVisible = false">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.label-text {
  color: #606266;
  width: 80px;
}

.form-item {
  margin-right: 20px;
}

.op-box {
  margin: 10px 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.no-reply {
  color: #909399;
  font-style: italic;
}

.feedback-info {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
  
  h4 {
    margin: 0 0 10px 0;
    color: #303133;
  }
  
  p {
    margin: 5px 0;
    color: #606266;
    
    strong {
      color: #303133;
    }
  }
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

:deep(.el-table) {
  --el-table-header-bg-color: var(--el-fill-color-light);
}
</style>