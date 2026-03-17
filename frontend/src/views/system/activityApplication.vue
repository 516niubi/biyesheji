<script setup lang="ts">
import http from "../../utils/http";
import { Search, Refresh, Delete, Check, Close } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { computed, onMounted, ref } from "vue";
import { hasPermission } from "../../utils/system";

const initParams = {
  pageNum: 1,
  pageSize: 10,
  realName: "",
  phone: "",
  status: "",
};
// 搜索条件
const params = ref({ ...initParams });
// 表格数据
const tableData = ref([]);
// 表格数据总数
const total = ref(0);

// 批量删除相关
const multipleSelection = ref([]);
const selectedIds = computed(() => {
  return multipleSelection.value.map((item) => item.id);
});
const hasSelected = computed(() => {
  return multipleSelection.value.length > 0;
});

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
  const res = await http.get(
    `/activityApplication/page?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}&realName=${params.value.realName}&phone=${params.value.phone}&status=${params.value.status}`
  );
  if (res.code === 200) {
    tableData.value = res.data.records;
    total.value = res.data.total;
  }
};

// 搜索
const handleSearch = async () => {
  await getTableData();
};

// 重置
const handleReset = () => {
  params.value = { ...initParams };
  getTableData();
};

// 删除
const delRow = async (id: number) => {
  const res = await http.get(`/activityApplication/del?id=${id}`);
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
    ElMessage.warning("请先选择要删除的报名记录");
    return;
  }
  const res = await http.post("/activityApplication/batchDel", selectedIds.value);
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

// 审核通过
const handleApprove = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要通过 ${row.realName} 的报名申请吗？`,
      "审核确认",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }
    );
    
    const res = await http.post(`/activityApplication/edit?id=${row.id}`, {
      status: 1
    });
    if (res.code === 200) {
      ElMessage.success("审核通过成功");
      await getTableData();
    } else {
      ElMessage.error(res.msg || "审核通过失败");
    }
  } catch {
    // 用户取消操作
  }
};

// 审核拒绝
const handleReject = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要拒绝 ${row.realName} 的报名申请吗？`,
      "审核确认",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }
    );
    
    const res = await http.post(`/activityApplication/edit?id=${row.id}`, {
      status: 2
    });
    if (res.code === 200) {
      ElMessage.success("审核拒绝成功");
      await getTableData();
    } else {
      ElMessage.error(res.msg || "审核拒绝失败");
    }
  } catch {
    // 用户取消操作
  }
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
  <div>
    <header class="flex">
      <div class="flex form-item">
        <p class="label-text">姓名：</p>
        <el-input v-model="params.realName" placeholder="请输入真实姓名" />
      </div>
      <div class="flex form-item">
        <p class="label-text">手机号：</p>
        <el-input v-model="params.phone" placeholder="请输入手机号" />
      </div>
      <div class="flex form-item">
        <p class="label-text">状态：</p>
        <el-select v-model="params.status" placeholder="请选择状态" clearable style="width: 160px;">
          <el-option label="待审核" value="0" />
          <el-option label="已通过" value="1" />
          <el-option label="已拒绝" value="2" />
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
          v-if="hasPermission('activityApplication', '删除')"
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
        <el-table-column prop="username" label="用户账号" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="activityTitle" label="活动名称" show-overflow-tooltip />
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
        <el-table-column label="操作" fixed="right">
          <template #default="scope">
            <!-- 待审核状态显示审核按钮 -->
            <template v-if="scope.row.status === 0">
              <el-button
                v-if="hasPermission('activityApplication', '审核')"
                type="success"
                :icon="Check"
                size="small"
                @click="handleApprove(scope.row)"
                >通过</el-button
              >
              <el-button
                v-if="hasPermission('activityApplication', '审核')"
                type="danger"
                :icon="Close"
                size="small"
                @click="handleReject(scope.row)"
                >拒绝</el-button
              >
            </template>
            <!-- 删除按钮始终显示 -->
            <el-popconfirm
              v-if="hasPermission('activityApplication', '删除')"
              title="您确定要删除吗?"
              @confirm="delRow(scope.row.id)"
            >
              <template #reference>
                <el-button type="danger" :icon="Delete" size="small">删除</el-button>
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
.label-text {
  color: #606266;
  width: 90px;
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

:deep(.el-table) {
  --el-table-header-bg-color: var(--el-fill-color-light);
}
</style>