<script setup lang="ts">
import http from "../../utils/http";
import { Search, Refresh, Delete, Edit } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { computed, onMounted, ref } from "vue";
import { hasPermission } from "../../utils/system";

const initParams = {
  pageNum: 1,
  pageSize: 10,
  userName: "",
};

const params = ref({ ...initParams });
const tableData = ref([]);
const total = ref(0);

const editVisible = ref(false);
const editForm = ref({ id: 0, content: "" });

const multipleSelection = ref([]);
const selectedIds = computed(() => {
  return multipleSelection.value.map((item) => item.id);
});
const hasSelected = computed(() => {
  return multipleSelection.value.length > 0;
});

const getTableData = async () => {
  let url = `/comment/manage/selectPage?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}`;
  if (params.value.userName) {
    url += `&userName=${encodeURIComponent(params.value.userName)}`;
  }
  const res = await http.get(url);
  if (res.code === 200) {
    tableData.value = res.data.records;
    total.value = res.data.total;
  }
};

const handleSearch = async () => {
  await getTableData();
};

const handleReset = () => {
  params.value = { ...initParams };
  getTableData();
};

const openEdit = (row: { id: number; content: string }) => {
  editForm.value = { id: row.id, content: row.content || "" };
  editVisible.value = true;
};

const submitEdit = async () => {
  const res = await http.put("/comment/update", {
    id: editForm.value.id,
    content: editForm.value.content,
  });
  if (res.code === 200) {
    ElMessage.success("修改成功");
    editVisible.value = false;
    await getTableData();
  }
};

const delRow = async (id: number) => {
  const res = await http.delete(`/comment/delete/${id}`);
  if (res.code === 200) {
    ElMessage.success("删除成功");
    await getTableData();
  } else {
    ElMessage.error("删除失败");
  }
};

const batchDel = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning("请先选择要删除的数据");
    return;
  }
  const res = await http.delete("/comment/delete/batch", {
    data: selectedIds.value,
  });
  if (res.code === 200) {
    ElMessage.success("批量删除成功");
    multipleSelection.value = [];
    await getTableData();
  } else {
    ElMessage.error("批量删除失败");
  }
};

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

onMounted(async () => {
  await getTableData();
});
</script>

<template>
  <div>
    <header class="flex header-row">
      <div class="flex form-item">
        <p class="label-text">用户名：</p>
        <el-input v-model="params.userName" placeholder="昵称模糊查询" clearable />
      </div>
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button type="warning" :icon="Refresh" @click="handleReset">重置</el-button>
    </header>
    <main>
      <div class="flex op-box">
        <el-button
          v-if="hasPermission('comment', '删除')"
          :icon="Delete"
          type="danger"
          :disabled="!hasSelected"
          @click="batchDel"
          >批量删除</el-button
        >
      </div>
      <el-table
        :data="tableData"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="userName" label="用户名" width="120" />
        <el-table-column prop="heritageName" label="文物名称" show-overflow-tooltip />
        <el-table-column prop="content" label="评论内容" show-overflow-tooltip />
        <el-table-column prop="createTime" label="评论时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <el-button
              v-if="hasPermission('comment', '编辑')"
              type="primary"
              :icon="Edit"
              link
              @click="openEdit(scope.row)"
              >编辑</el-button
            >
            <el-popconfirm
              title="您确定要删除吗?"
              @confirm="delRow(scope.row.id)"
            >
              <template #reference>
                <el-button
                  v-if="hasPermission('comment', '删除')"
                  type="danger"
                  :icon="Delete"
                  link
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

    <el-dialog v-model="editVisible" title="编辑评论" width="520" destroy-on-close>
      <el-input v-model="editForm.content" type="textarea" :rows="5" maxlength="255" show-word-limit />
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.header-row {
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}
.label-text {
  color: #606266;
  width: 72px;
}

.form-item {
  margin-right: 12px;
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
