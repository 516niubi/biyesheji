<script setup lang="ts">
import http from "../../utils/http";
import { Plus, Search, Refresh, Edit, Delete } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox, type UploadProps } from "element-plus";
import { getImageUrl } from "../../utils/system";
import Config from "../../config/index";
import { computed, onMounted, ref, reactive } from "vue";
import { hasPermission } from "../../utils/system";

const initParams = {
  pageNum: 1,
  pageSize: 10,
  title: "",
};
// 搜索条件
const params = ref({ ...initParams });
// 初始参数
const initForm = {
  title: "",
  url: "",
  coverUrl: "",
  viewCount: 0,
};
// 表单数据
const form = ref({ ...initForm });
// 表格数据
const tableData = ref([]);
// 表格数据总数
const total = ref(0);
// 控制弹窗显示
const visible = ref(false);
// 控制视频预览弹窗
const videoPreviewVisible = ref(false);
const videoOptions = ref({
  title: "",
  src: "",
});

// 批量删除相关
const multipleSelection = ref([]);
const selectedIds = computed(() => {
  return multipleSelection.value.map((item) => item.id);
});
const hasSelected = computed(() => {
  return multipleSelection.value.length > 0;
});

const isEdit = computed(() => {
  return form.value.id > 0;
});
const dialogTitle = computed(() => {
  return isEdit.value ? "编辑" : "新增";
});

// 文件上传地址
const uploadUrl = ref(`${Config.baseUrl}/file/upload`);
// 文件上传携带的数据
const uploadData = {
  authorization: `${localStorage.getItem("token")}`,
};
// 封面图片上传后的回调地址
const coverImageUrl = ref(
  form.value?.coverUrl ? `${Config.baseUrl}${form.value?.coverUrl}` : ""
);

// 获取表格数据
const getTableData = async () => {
  const res = await http.get(
    `/video/manage/page?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}&title=${params.value.title}`
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

// 新增
const handleAdd = () => {
  form.value = { ...initForm };
  coverImageUrl.value = "";
  visible.value = true;
};

// 编辑
const handleEdit = (row: any) => {
  coverImageUrl.value = row.coverUrl ? getImageUrl(row.coverUrl) : "";
  form.value = { ...row };
  visible.value = true;
};

// 删除
const delRow = async (id: number) => {
  const res = await http.get(`/video/del?id=${id}`);
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
    ElMessage.warning("请先选择要删除的视频");
    return;
  }
  const res = await http.post("/video/batchDel", selectedIds.value);
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

// 确认按钮
const submit = async () => {
  if (!form.value.title?.trim()) {
    ElMessage.warning("请填写视频标题");
    return;
  }
  if (!form.value.url) {
    ElMessage.warning("请先上传视频文件");
    return;
  }
  if (!form.value.coverUrl) {
    ElMessage.warning("请上传封面图片");
    return;
  }

  if (isEdit.value) {
    const res = await http.post(`/video/edit?id=${form.value.id}`, form.value);
    if (res.code === 200) {
      ElMessage.success("编辑成功");
      visible.value = false;
      await getTableData();
    } else {
      ElMessage.error("编辑失败");
    }
  } else {
    const res = await http.post(`/video/add`, form.value);
    if (res.code === 200) {
      ElMessage.success("新增成功");
      visible.value = false;
      await getTableData();
    } else {
      ElMessage.error("新增失败");
    }
  }
};

// 处理关闭弹窗
const handleClose = () => {
  visible.value = false;
};

// 预览视频
const previewVideo = (url: string) => {
  videoOptions.value.src = getImageUrl(url);
  videoPreviewVisible.value = true;
};

// 关闭视频预览
const closeVideoPreview = () => {
  videoPreviewVisible.value = false;
};

/** 视频文件上传前的钩子函数 */
const beforeVideoUpload: UploadProps["beforeUpload"] = (rawFile) => {
  if (!rawFile.type.includes("video")) {
    ElMessage.error("文件类型错误，只允许上传视频文件");
    return false;
  } else if (rawFile.size / 1024 / 1024 > 50) {
    ElMessage.error("视频文件超过50MB限制");
    return false;
  }
  return true;
};

/** 视频文件上传成功回调 */
const handleVideoSuccess: UploadProps["onSuccess"] = (response) => {
  form.value.url = response.data;
  ElMessage.success("视频上传成功");
};

/** 封面图片上传前的钩子函数 */
const beforeCoverUpload: UploadProps["beforeUpload"] = (rawFile) => {
  if (!rawFile.type.includes("image")) {
    ElMessage.error("文件类型错误，只允许上传图片文件");
    return false;
  } else if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error("图片超过2MB限制");
    return false;
  }
  return true;
};

/** 封面图片上传成功回调 */
const handleCoverSuccess: UploadProps["onSuccess"] = (response) => {
  form.value.coverUrl = response.data;
  coverImageUrl.value = `${Config.baseUrl}${response.data}`;
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
        <el-input v-model="params.title" placeholder="请输入视频标题" />
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
          v-if="hasPermission('video', '新增')"
          :icon="Plus"
          type="primary"
          @click="handleAdd"
          >新增</el-button
        >
        <el-button
          v-if="hasPermission('video', '删除')"
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
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column prop="url" label="视频" width="120">
          <template #default="scope">
            <el-button
              type="primary"
              :icon="VideoPlay"
              size="small"
              @click="previewVideo(scope.row.url)"
              >预览</el-button
            >
          </template>
        </el-table-column>
        <el-table-column prop="coverUrl" label="封面" width="100">
          <template #default="scope">
            <el-image
              v-if="scope.row.coverUrl"
              :src="getImageUrl(scope.row.coverUrl)"
              :preview-src-list="[getImageUrl(scope.row.coverUrl)]"
              :preview-teleported="true"
              :hide-on-click-modal="true"
              :lazy="true"
              :initial-index="0"
              fit="cover"
            >
            </el-image>
            <span v-else>无封面</span>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="播放量" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" fixed="right">
          <template #default="scope">
            <el-button
              v-if="hasPermission('video', '编辑')"
              type="primary"
              :icon="Edit"
              @click="handleEdit(scope.row)"
              >编辑</el-button
            >
            <el-popconfirm
              v-if="hasPermission('video', '删除')"
              title="您确定要删除吗?"
              @confirm="delRow(scope.row.id)"
            >
              <template #reference>
                <el-button type="danger" :icon="Delete">删除</el-button>
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
    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="visible"
      :title="dialogTitle"
      width="600"
      :before-close="handleClose"
    >
      <el-form :model="form" label-width="120">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入视频标题" />
        </el-form-item>
        <el-form-item label="视频文件" prop="url">
          <div class="upload-container">
            <el-upload
              :action="uploadUrl"
              :headers="uploadData"
              :show-file-list="false"
              :on-success="handleVideoSuccess"
              :before-upload="beforeVideoUpload"
              accept="video/*"
            >
              <el-button type="primary" :icon="Plus">上传视频</el-button>
            </el-upload>
            <span v-if="form.url" class="upload-success">✓ 视频已上传</span>
          </div>
        </el-form-item>
        <el-form-item label="封面图片" prop="coverUrl">
          <el-upload
            class="cover-uploader"
            :action="uploadUrl"
            :headers="uploadData"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            :before-upload="beforeCoverUpload"
            accept="image/*"
          >
            <el-image
              v-if="coverImageUrl"
              :src="coverImageUrl"
              style="width: 100%; height: 100%"
            />
            <el-icon v-else class="cover-uploader-icon">
              <Plus />
            </el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="播放量" prop="viewCount">
          <el-input-number v-model="form.viewCount" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submit()"> 确认 </el-button>
          <el-button @click="visible = false">取消</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 视频预览弹窗 -->
    <el-dialog
      v-model="videoPreviewVisible"
      title="视频预览"
      width="800"
      :before-close="closeVideoPreview"
    >
      <div class="video-preview-container">
        <video
          v-if="videoOptions.src"
          class="video-player"
          :src="videoOptions.src"
          controls
          preload="metadata"
        />
      </div>
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

.upload-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.upload-success {
  color: #67c23a;
  font-size: 14px;
}

.cover-uploader {
  width: 178px;
  height: 178px;
  overflow: hidden;
  border: 1px dashed var(--el-border-color);

  &:hover {
    border-color: var(--el-color-primary);
  }
}

.el-icon.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}

.video-preview-container {
  text-align: center;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-player {
  width: 100%;
  max-width: 760px;
  max-height: 430px;
  border-radius: 8px;
  background: #000;
}

:deep(.el-image) {
  width: 55px;
  height: 55px;
}

:deep(.el-table) {
  --el-table-header-bg-color: var(--el-fill-color-light);
}
</style>