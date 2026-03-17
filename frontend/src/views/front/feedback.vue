<script setup lang="ts">
import http from "../../utils/http";
import { Search, Refresh, Delete, Plus, Upload } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { computed, onMounted, ref } from "vue";
import { getImageUrl } from '../../utils/system'
import config from "../../config";
import useUserStore from "../../stores/userStore";

const userStore = useUserStore();

const initParams = {
  pageNum: 1,
  pageSize: 10,
  title: "",
  status: null,
  userId: userStore.userInfo.id
};

// 搜索条件
const params = ref({ ...initParams });
// 表格数据
const tableData = ref([]);
// 表格数据总数
const total = ref(0);
// 表格加载状态
const loading = ref(false);
// 控制新增弹窗显示
const addVisible = ref(false);
// 新增表单数据
const addForm = ref({
  title: "",
  content: "",
  img: "",
  userId: userStore.userInfo.id
});
// 新增表单引用
const addFormRef = ref(null);

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
  { label: '未回复', value: 0 },
  { label: '已回复', value: 1 }
];

// 表单验证规则
const addFormRules = {
  title: [
    { required: true, message: '请输入反馈标题', trigger: 'blur' },
  ],
  content: [
    { required: true, message: '请输入反馈内容', trigger: 'blur' },
  ]
};

// 图片上传地址
const uploadUrl = ref(config.baseUrl + '/file/upload');
// 文件上传携带的数据
const uploadData = {
  authorization: `${localStorage.getItem("token")}`,
};

// 获取表格数据
const getTableData = async () => {
  loading.value = true;
  try {
    // let url = `/feedback/pageByUserId?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}`;
    
    const res = await http.get("/feedback/pageByUserId", {
      params: params.value
    });
    if (res.code === 200) {
      tableData.value = res.data.records;
      total.value = res.data.total;
    }
  } catch (error) {
    console.error('获取反馈列表失败:', error);
    ElMessage.error('获取数据失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = async () => {
  params.value.pageNum = 1;
  await getTableData();
};

// 重置搜索
const handleReset = () => {
  params.value = { ...initParams };
  getTableData();
};

// 显示新增弹窗
const showAdd = () => {
  addForm.value = {
    title: "",
    content: "",
    img: ""
  };
  addVisible.value = true;
};

// 提交新增
const submitAdd = async () => {
  if (!addFormRef.value) return;
  
  try {
    const valid = await addFormRef.value.validate();
    if (!valid) return;
    
    const res = await http.post('/feedback/add', addForm.value);
    if (res.code === 200) {
      ElMessage.success("新增成功");
      addVisible.value = false;
      await getTableData();
    } else {
      ElMessage.error("新增失败");
    }
  } catch (error) {
    console.error('新增反馈失败:', error);
    ElMessage.error('新增失败');
  }
};

// 删除
const delRow = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条反馈吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const res = await http.get(`/feedback/del?id=${id}`);
    if (res.code === 200) {
      ElMessage.success("删除成功");
      await getTableData();
    } else {
      ElMessage.error("删除失败");
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除反馈失败:', error);
      ElMessage.error('删除失败');
    }
  }
};

// 批量删除
const batchDel = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning("请先选择要删除的数据");
    return;
  }
  try {
    await ElMessageBox.confirm('确定要删除选中的反馈吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const res = await http.post("/feedback/batchDel", selectedIds.value);
    if (res.code === 200) {
      ElMessage.success("批量删除成功");
      multipleSelection.value = [];
      await getTableData();
    } else {
      ElMessage.error("批量删除失败");
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error);
      ElMessage.error('批量删除失败');
    }
  }
};

// 多选
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection;
};

const handleSizeChange = (val: number) => {
  params.value.pageSize = val;
  params.value.pageNum = 1;
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

/** 文件上传前的钩子函数 */
const beforeImageUpload = (rawFile: any) => {
  if (!rawFile.type.includes("image")) {
    ElMessage.error("文件类型错误，只允许上传图片文件");
    return false;
  } else if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error("图片超过2MB限制");
    return false;
  }
  return true;
};

/** 文件上传成功回调 */
const handleUploadSuccess = (response: any) => {
  if (response.code === 200) {
    addForm.value.img = response.data;
    ElMessage.success('图片上传成功');
  } else {
    ElMessage.error('图片上传失败');
  }
};

// 图片上传失败回调
const handleUploadError = () => {
  ElMessage.error('图片上传失败');
};

onMounted(async () => {
  await getTableData();
});
</script>

<template>
  <div class="feedback-page">
    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-form">
        <div class="form-item">
          <p class="form-label">反馈标题</p>
          <el-input
            v-model="params.title"
            placeholder="请输入反馈标题"
            class="search-input"
            clearable
          />
        </div>
        
        <div class="form-item">
          <p class="form-label">状态</p>
          <el-select
            v-model="params.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </div>
        
        <div class="form-actions">
          <el-button 
            type="primary" 
            :icon="Search" 
            @click="handleSearch"
            :loading="loading"
          >
            搜索
          </el-button>
          
          <el-button 
            type="warning" 
            :icon="Refresh" 
            @click="handleReset"
          >
            重置
          </el-button>
          
          <el-button type="success" :icon="Plus" @click="showAdd">
            新增反馈
          </el-button>
          <el-button type="danger" :icon="Delete" @click="batchDel" :disabled="!hasSelected">
            批量删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="table-section">
      <el-table
        :data="tableData"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="反馈标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="content" label="反馈内容" min-width="300" show-overflow-tooltip />
        <el-table-column label="反馈图片" width="120">
          <template #default="{ row }">
            <div v-if="row.img" class="image-preview">
              <el-image
                :src="getImageUrl(row.img)"
                :preview-src-list="[getImageUrl(row.img)]"
                fit="cover"
                style="width: 60px; height: 60px; border-radius: 4px;"
                :preview-teleported="true"
                :hide-on-click-modal="true"
                :lazy="true"
              >
                <template #error>
                  <div class="image-slot">
                    <span>加载失败</span>
                  </div>
                </template>
              </el-image>
            </div>
            <span v-else class="no-image">无图片</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reply" label="回复内容" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.reply">{{ row.reply }}</span>
            <span v-else class="no-reply">暂无回复</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-popconfirm
              title="您确定要删除吗?"
              @confirm="delRow(row.id)"
            >
              <template #reference>
                <el-button
                  type="danger"
                  size="small"
                  :icon="Delete"
                >
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="params.pageNum"
        v-model:page-size="params.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增反馈弹窗 -->
    <el-dialog
      v-model="addVisible"
      title="新增反馈"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="addFormRef"
        :model="addForm"
        :rules="addFormRules"
        label-width="80px"
      >
        <el-form-item label="反馈标题" prop="title">
          <el-input
            v-model="addForm.title"
            placeholder="请输入反馈标题"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="反馈内容" prop="content">
          <el-input
            v-model="addForm.content"
            type="textarea"
            placeholder="请详细描述您的问题或建议"
            :rows="6"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="反馈图片">
          <el-upload
            class="image-uploader"
            :action="uploadUrl"
            :headers="uploadData"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeImageUpload"
            accept="image/*"
          >
            <el-image
              v-if="addForm.img"
              :src="getImageUrl(addForm.img)"
              style="width: 100%; height: 100%"
              fit="cover"
            />
            <el-icon v-else class="image-uploader-icon">
              <Plus />
            </el-icon>
          </el-upload>
          <div class="upload-tip">支持 jpg、png 格式，文件大小不超过 2MB</div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAdd">提交</el-button>
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

.feedback-page {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

// 搜索区域
.search-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  
  .search-form {
    display: flex;
    align-items: flex-end;
    gap: 20px;
    flex-wrap: wrap;
  }
  
  .form-item {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .form-label {
      font-size: 14px;
      font-weight: 500;
      color: #333;
      margin: 0;
    }
    
    .search-input {
      width: 200px;
    }
  }
  
  .form-actions {
    display: flex;
    gap: 10px;
    margin-left: auto;
  }
}

// 表格区域
.table-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  
  .image-preview {
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  .no-image {
    color: #999;
    font-size: 12px;
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

// 分页区域
.pagination-section {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

// 图片上传器
.image-uploader {
  width: 148px;
  height: 148px;
  overflow: hidden;
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  transition: var(--el-transition-duration-fast);

  &:hover {
    border-color: var(--el-color-primary);
  }
}

.el-icon.image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 148px;
  height: 148px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

:deep(.el-table) {
  --el-table-header-bg-color: var(--el-fill-color-light);
}

// 响应式设计
@media (max-width: 768px) {
  .feedback-page {
    padding: 10px;
  }
  
  .search-section {
    padding: 15px;
    
    .search-form {
      flex-direction: column;
      align-items: stretch;
      gap: 15px;
      
      .form-item {
        .search-input {
          width: 100%;
        }
      }
      
      .form-actions {
        margin-left: 0;
        justify-content: center;
        flex-wrap: wrap;
      }
    }
  }
  
  .table-section {
    padding: 15px;
    overflow-x: auto;
  }
  
  .pagination-section {
    padding: 15px;
    
    :deep(.el-pagination) {
      .el-pagination__sizes,
      .el-pagination__jump {
        display: none;
      }
    }
  }
}
</style>