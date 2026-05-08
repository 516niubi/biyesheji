<script setup lang="ts">
import http from "../../utils/http";
import { Plus, Search, Refresh, Edit, Delete, Picture } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { computed, onMounted, ref } from "vue";
import { hasPermission, getImageUrl } from "../../utils/system";
import Config from "../../config/index";

const initParams = {
  pageNum: 1,
  pageSize: 10,
  title: "",
};

const params = ref({ ...initParams });
const initForm = {
  title: "",
  content: "",
  coverUrl: "",
  isDelete: 0,
};
const form = ref({ ...initForm });
const tableData = ref([]);
const total = ref(0);
const visible = ref(false);

const uploadUrl = ref(`${Config.baseUrl}/file/upload`);
const uploadData = {
  authorization: `${localStorage.getItem("token")}`,
};
const imageUrl = ref("");

const multipleSelection = ref([]);
const selectedIds = computed(() => multipleSelection.value.map((item) => item.id));
const hasSelected = computed(() => multipleSelection.value.length > 0);
const isEdit = computed(() => form.value.id > 0);
const dialogTitle = computed(() => (isEdit.value ? "编辑" : "新增"));

const getTableData = async () => {
  const res = await http.get(
    `/notice/page?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}&title=${params.value.title}`
  );
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

const handleAdd = () => {
  form.value = { ...initForm };
  imageUrl.value = "";
  visible.value = true;
};

const handleEdit = (row: any) => {
  form.value = { ...row };
  imageUrl.value = row.coverUrl ? getImageUrl(row.coverUrl) : "";
  visible.value = true;
};

const beforeImageUpload = (rawFile) => {
  if (!rawFile.type.includes("image")) {
    ElMessage.error("文件类型错误，只允许上传图片文件");
    return false;
  }
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error("图片超过2MB限制");
    return false;
  }
  return true;
};

const handleImageUploadSuccess = (response) => {
  form.value.coverUrl = response.data;
  imageUrl.value = `${Config.baseUrl}${response.data}`;
};

const delRow = async (id: number) => {
  const res = await http.get(`/notice/del?id=${id}`);
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
  const res = await http.post("/notice/batchDel", selectedIds.value);
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

const submit = async () => {
  if (!form.value.title?.trim()) {
    ElMessage.warning("请填写公告标题");
    return;
  }
  if (!form.value.content?.trim()) {
    ElMessage.warning("请填写公告内容");
    return;
  }

  if (isEdit.value) {
    const res = await http.post(`/notice/edit?id=${form.value.id}`, form.value);
    if (res.code === 200) {
      ElMessage.success("编辑成功");
      visible.value = false;
      await getTableData();
    } else {
      ElMessage.error("编辑失败");
    }
  } else {
    const res = await http.post(`/notice/add`, form.value);
    if (res.code === 200) {
      ElMessage.success("新增成功");
      visible.value = false;
      await getTableData();
    } else {
      ElMessage.error("新增失败");
    }
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
        <p class="label-text">标题：</p>
        <el-input v-model="params.title" placeholder="请输入公告标题" />
      </div>
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button type="warning" :icon="Refresh" @click="handleReset">重置</el-button>
    </header>
    <main>
      <div class="flex op-box">
        <el-button
          v-if="hasPermission('notice', '新增')"
          :icon="Plus"
          type="primary"
          @click="handleAdd"
        >
          新增
        </el-button>
        <el-button
          v-if="hasPermission('notice', '删除')"
          :icon="Delete"
          type="danger"
          :disabled="!hasSelected"
          @click="batchDel"
        >
          批量删除
        </el-button>
      </div>
      <el-table :data="tableData" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column prop="coverUrl" label="图片" width="110">
          <template #default="scope">
            <el-image
              v-if="scope.row.coverUrl"
              :src="getImageUrl(scope.row.coverUrl)"
              style="width: 60px; height: 40px"
              fit="cover"
              :preview-src-list="[getImageUrl(scope.row.coverUrl)]"
              :preview-teleported="true"
              :hide-on-click-modal="true"
            >
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <span v-else>无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="publishName" label="创建人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <el-button
              v-if="hasPermission('notice', '编辑')"
              type="primary"
              :icon="Edit"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-popconfirm title="您确定要删除吗?" @confirm="delRow(scope.row.id)">
              <template #reference>
                <el-button v-if="hasPermission('notice', '删除')" type="danger" :icon="Delete">
                  删除
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
    <el-dialog v-model="visible" :title="dialogTitle" width="650">
      <el-form :model="form" label-width="120">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            type="textarea"
            v-model="form.content"
            :autosize="{ minRows: 4, maxRows: 8 }"
            placeholder="请输入公告内容"
          />
        </el-form-item>
        <el-form-item label="公告图片" prop="coverUrl">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="uploadData"
            :show-file-list="false"
            :on-success="handleImageUploadSuccess"
            :before-upload="beforeImageUpload"
          >
            <el-image v-if="imageUrl" :src="imageUrl" style="width: 100%; height: 100%" fit="cover" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="form-tip">可选，建议尺寸 320x180，大小不超过 2MB</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submit()">确认</el-button>
          <el-button @click="visible = false">取消</el-button>
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

.avatar-uploader {
  width: 178px;
  height: 120px;
  overflow: hidden;
  border: 1px dashed var(--el-border-color);

  &:hover {
    border-color: var(--el-color-primary);
  }
}

.el-icon.avatar-uploader-icon {
  font-size: 24px;
  color: #8c939d;
  width: 178px;
  height: 120px;
  text-align: center;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
}

.form-tip {
  margin-top: 6px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

:deep(.el-table) {
  --el-table-header-bg-color: var(--el-fill-color-light);
}
</style>
