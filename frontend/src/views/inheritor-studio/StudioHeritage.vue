<script setup lang="ts">
import http from "@/utils/http";
import { Plus, Search, Refresh, Edit, Delete } from "@element-plus/icons-vue";
import { ElMessage, type UploadProps } from "element-plus";
import { getImageUrl, hasPermission } from "@/utils/system";
import Config from "@/config/index";
import { Picture as IconPicture } from "@element-plus/icons-vue";
import { computed, onMounted, ref, onBeforeUnmount, shallowRef } from "vue";
import { Editor, Toolbar } from "@wangeditor/editor-for-vue";
import type { IEditorConfig } from "@wangeditor/editor";
import type { IToolbarConfig } from "@wangeditor/editor";
import "@wangeditor/editor/dist/css/style.css";

const initParams = { pageNum: 1, pageSize: 12, name: "" };
const params = ref({ ...initParams });
const initForm = { name: "", categoryId: null, coverImage: "", intro: "", description: "" };
const form = ref({ ...initForm });
const tableData = ref<any[]>([]);
const total = ref(0);
const visible = ref(false);
const categoryList = ref<any[]>([]);
const editorRef = shallowRef();
const toolbarConfig: Partial<IToolbarConfig> = {};
const editorConfig: Partial<IEditorConfig> = {
  MENU_CONF: {
    uploadImage: {
      server: `${Config.baseUrl}/file/wang/upload`,
      fieldName: "file",
    },
  },
};
const handleCreated = (editor: any) => {
  editorRef.value = editor;
};
const uploadUrl = ref(`${Config.baseUrl}/file/upload`);
const uploadData = { authorization: `${localStorage.getItem("token")}` };
const imageUrl = ref(form.value?.coverImage ? `${Config.baseUrl}${form.value?.coverImage}` : "");

const multipleSelection = ref<any[]>([]);
const selectedIds = computed(() => multipleSelection.value.map((item) => item.id));
const hasSelected = computed(() => multipleSelection.value.length > 0);
const isEdit = computed(() => (form.value as any).id > 0);
const dialogTitle = computed(() => (isEdit.value ? "编辑非遗作品" : "发布非遗作品"));

const getCategoryList = async () => {
  const res = await http.get("/ichType/list");
  if (res.code === 200) categoryList.value = res.data;
};

const getTableData = async () => {
  const res = await http.get(
    `/culturalHeritage/manage/page?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}&name=${params.value.name}`
  );
  if (res.code === 200) {
    tableData.value = res.data.records;
    total.value = res.data.total;
  }
};

const handleSearch = () => getTableData();
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
  imageUrl.value = row.coverImage ? getImageUrl(row.coverImage) : "";
  form.value = { ...row };
  visible.value = true;
};

const delRow = async (id: number) => {
  const res = await http.get(`/culturalHeritage/del?id=${id}`);
  if (res.code === 200) {
    ElMessage.success("删除成功");
    await getTableData();
  } else ElMessage.error("删除失败");
};

const batchDel = async () => {
  if (!multipleSelection.value.length) {
    ElMessage.warning("请先选择要删除的作品");
    return;
  }
  const res = await http.post("/culturalHeritage/batchDel", selectedIds.value);
  if (res.code === 200) {
    ElMessage.success("批量删除成功");
    multipleSelection.value = [];
    await getTableData();
  } else ElMessage.error("批量删除失败");
};

const isSelected = (row: any) => multipleSelection.value.some((x) => x.id === row.id);
const toggleSelect = (row: any, checked: boolean) => {
  if (checked) {
    if (!isSelected(row)) multipleSelection.value.push(row);
  } else {
    multipleSelection.value = multipleSelection.value.filter((x) => x.id !== row.id);
  }
};

const submit = async () => {
  if (!form.value.name?.trim()) {
    ElMessage.warning("请填写名称");
    return;
  }
  if (!form.value.categoryId) {
    ElMessage.warning("请选择分类");
    return;
  }
  if (!form.value.coverImage) {
    ElMessage.warning("请上传封面图片");
    return;
  }
  if (!form.value.intro?.trim()) {
    ElMessage.warning("请填写简介");
    return;
  }
  if (!form.value.description?.trim()) {
    ElMessage.warning("请填写详细描述");
    return;
  }
  const fid = (form.value as any).id;
  if (isEdit.value) {
    const res = await http.post(`/culturalHeritage/edit?id=${fid}`, form.value);
    if (res.code === 200) {
      ElMessage.success("保存成功");
      visible.value = false;
      await getTableData();
    } else ElMessage.error("保存失败");
  } else {
    const res = await http.post(`/culturalHeritage/add`, form.value);
    if (res.code === 200) {
      ElMessage.success("发布成功");
      visible.value = false;
      await getTableData();
    } else ElMessage.error("发布失败");
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

const beforeAvatarUpload: UploadProps["beforeUpload"] = (rawFile) => {
  if (!rawFile.type.includes("image")) {
    ElMessage.error("仅支持图片");
    return false;
  }
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error("图片不超过 2MB");
    return false;
  }
  return true;
};

const handleAvatarSuccess: UploadProps["onSuccess"] = (response: any) => {
  form.value.coverImage = response.data;
  imageUrl.value = `${Config.baseUrl}${response.data}`;
};

onMounted(async () => {
  await getCategoryList();
  await getTableData();
});

onBeforeUnmount(() => {
  editorRef.value?.destroy();
  editorRef.value = null;
});
</script>

<template>
  <div class="studio-page">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="params.name"
          placeholder="搜索作品名称…"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" round @click="handleSearch">搜索</el-button>
        <el-button round @click="handleReset"><el-icon class="mr"><Refresh /></el-icon>重置</el-button>
      </div>
      <div class="toolbar-right">
        <el-button
          v-if="hasPermission('culturalHeritage', '新增')"
          type="primary"
          round
          class="cta"
          @click="handleAdd"
        >
          <el-icon><Plus /></el-icon>
          发布作品
        </el-button>
        <el-button
          v-if="hasPermission('culturalHeritage', '删除')"
          round
          type="danger"
          plain
          :disabled="!hasSelected"
          @click="batchDel"
        >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>
    </div>

    <p class="hint">共 {{ total }} 条 · 勾选卡片可进行批量删除</p>

    <div v-if="!tableData.length" class="empty">
      <el-empty description="暂无作品，点击「发布作品」开始创作" />
    </div>
    <div v-else class="card-grid">
      <article v-for="row in tableData" :key="row.id" class="work-card">
        <div class="select-corner">
          <el-checkbox
            v-if="hasPermission('culturalHeritage', '删除')"
            :model-value="isSelected(row)"
            @change="(v: boolean) => toggleSelect(row, v)"
          />
        </div>
        <div class="cover-wrap" @click="handleEdit(row)">
          <el-image :src="getImageUrl(row.coverImage)" fit="cover" class="cover-img">
            <template #error>
              <div class="img-fallback"><el-icon><IconPicture /></el-icon></div>
            </template>
          </el-image>
          <span class="cat-pill">{{ row.categoryName || "未分类" }}</span>
        </div>
        <div class="card-body">
          <h3 class="title" @click="handleEdit(row)">{{ row.name }}</h3>
          <p class="intro">{{ row.intro }}</p>
          <div class="meta">
            <span><i class="dot" />浏览 {{ row.viewCount ?? 0 }}</span>
            <span>{{ row.updateTime?.slice(0, 10) || row.createTime?.slice(0, 10) }}</span>
          </div>
          <div class="actions">
            <el-button
              v-if="hasPermission('culturalHeritage', '编辑')"
              type="primary"
              link
              :icon="Edit"
              @click="handleEdit(row)"
              >编辑</el-button
            >
            <el-popconfirm title="确定删除该作品？" @confirm="delRow(row.id)">
              <template #reference>
                <el-button v-if="hasPermission('culturalHeritage', '删除')" type="danger" link :icon="Delete"
                  >删除</el-button
                >
              </template>
            </el-popconfirm>
          </div>
        </div>
      </article>
    </div>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="params.pageNum"
        v-model:page-size="params.pageSize"
        :page-sizes="[12, 24, 48]"
        layout="sizes, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog
      v-model="visible"
      :title="dialogTitle"
      width="820px"
      top="3vh"
      class="studio-dialog"
      destroy-on-close
      append-to-body
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="作品名称" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="uploadData"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <el-image v-if="imageUrl" :src="imageUrl" fit="cover" class="upload-preview" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.intro" type="textarea" :autosize="{ minRows: 2, maxRows: 5 }" placeholder="一句话介绍" />
        </el-form-item>
        <el-form-item label="详情正文">
          <div class="editor-shell">
            <Toolbar
              style="border-bottom: 1px solid #e8e0d8"
              :editor="editorRef"
              :defaultConfig="toolbarConfig"
              mode="default"
            />
            <Editor
              style="height: 420px; overflow-y: auto"
              v-model="form.description"
              :defaultConfig="editorConfig"
              mode="default"
              @onCreated="handleCreated"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.studio-page {
  --studio-radius: 16px;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 8px;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.search-input {
  width: min(320px, 100%);
}

.cta {
  box-shadow: 0 8px 22px rgba(180, 58, 58, 0.28);
}

.mr {
  margin-right: 4px;
}

.hint {
  font-size: 13px;
  color: #7a6a62;
  margin: 0 0 16px;
}

.empty {
  padding: 40px 0;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(268px, 1fr));
  gap: 18px;
}

.work-card {
  position: relative;
  border-radius: var(--studio-radius);
  overflow: hidden;
  background: linear-gradient(180deg, #fff 0%, #fffaf5 100%);
  border: 1px solid rgba(92, 38, 36, 0.1);
  box-shadow: 0 12px 36px rgba(45, 28, 18, 0.06);
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 18px 44px rgba(45, 28, 18, 0.12);
  }
}

.select-corner {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 2;
  padding: 2px 6px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.85);
}

.cover-wrap {
  position: relative;
  cursor: pointer;
}

.cover-img {
  width: 100%;
  height: 168px;
}

.img-fallback {
  height: 168px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3ece4;
  color: #a89888;
  font-size: 36px;
}

.cat-pill {
  position: absolute;
  bottom: 10px;
  left: 10px;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(92, 38, 36, 0.88);
  color: #fff;
}

.card-body {
  padding: 14px 16px 16px;
}

.title {
  margin: 0 0 8px;
  font-size: 17px;
  cursor: pointer;
  color: #3a2f2c;
  line-height: 1.35;

  &:hover {
    color: var(--el-color-primary);
  }
}

.intro {
  margin: 0 0 12px;
  font-size: 13px;
  color: #6e625c;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.8em;
}

.meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #9a8f88;
  margin-bottom: 10px;
}

.dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #c9a227;
  margin-right: 6px;
  vertical-align: middle;
}

.actions {
  display: flex;
  gap: 6px;
}

.pagination-container {
  margin-top: 28px;
  display: flex;
  justify-content: center;
}

.avatar-uploader {
  width: 160px;
  height: 160px;
  border-radius: 12px;
  overflow: hidden;
  border: 2px dashed rgba(92, 38, 36, 0.25);
}

.upload-preview {
  width: 160px;
  height: 160px;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #b5a69a;
  width: 160px;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.editor-shell {
  border: 1px solid #e8e0d8;
  border-radius: 12px;
  overflow: hidden;
  width: 100%;
}
</style>
