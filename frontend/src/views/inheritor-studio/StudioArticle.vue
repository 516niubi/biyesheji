<script setup lang="ts">
import http from "@/utils/http";
import { Plus, Search, Refresh, Edit, Delete, Picture, View } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { getImageUrl, hasPermission } from "@/utils/system";
import Config from "@/config/index";
import { computed, onMounted, ref, onBeforeUnmount, shallowRef } from "vue";
import { Editor, Toolbar } from "@wangeditor/editor-for-vue";
import type { IEditorConfig } from "@wangeditor/editor";
import type { IToolbarConfig } from "@wangeditor/editor";
import "@wangeditor/editor/dist/css/style.css";

const initParams = { pageNum: 1, pageSize: 12, title: "", intro: "" };
const params = ref({ ...initParams });
const initForm = { title: "", intro: "", content: "", viewCount: 0, coverUrl: "" };
const form = ref({ ...initForm });
const tableData = ref<any[]>([]);
const total = ref(0);
const visible = ref(false);
const contentVisible = ref(false);
const currentContent = ref("");
const editorRef = shallowRef();
const toolbarConfig: Partial<IToolbarConfig> = {};
const editorConfig: Partial<IEditorConfig> = {
  MENU_CONF: {
    uploadImage: { server: `${Config.baseUrl}/file/wang/upload`, fieldName: "file" },
  },
};
const handleCreated = (editor: any) => {
  editorRef.value = editor;
};
const uploadUrl = ref(`${Config.baseUrl}/file/upload`);
const uploadData = { authorization: `${localStorage.getItem("token")}` };
const imageUrl = ref("");

const multipleSelection = ref<any[]>([]);
const selectedIds = computed(() => multipleSelection.value.map((i) => i.id));
const hasSelected = computed(() => multipleSelection.value.length > 0);
const isEdit = computed(() => (form.value as any).id > 0);
const dialogTitle = computed(() => (isEdit.value ? "编辑资讯" : "撰写资讯"));

const getTableData = async () => {
  let url = `/article/manage/page?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}`;
  if (params.value.title) url += `&title=${encodeURIComponent(params.value.title)}`;
  if (params.value.intro) url += `&intro=${encodeURIComponent(params.value.intro)}`;
  const res = await http.get(url);
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
const beforeAvatarUpload = (rawFile: File) => {
  if (!rawFile.type.includes("image")) {
    ElMessage.error("须为图片");
    return false;
  }
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error("不超过 2MB");
    return false;
  }
  return true;
};
const handleAvatarSuccess = (response: any) => {
  form.value.coverUrl = response.data;
  imageUrl.value = `${Config.baseUrl}${response.data}`;
};
const showContent = (html: string) => {
  currentContent.value = html;
  contentVisible.value = true;
};
const handleAdd = () => {
  form.value = { ...initForm };
  imageUrl.value = "";
  visible.value = true;
};
const handleEdit = (row: any) => {
  imageUrl.value = row.coverUrl ? getImageUrl(row.coverUrl) : "";
  form.value = { ...row };
  visible.value = true;
};
const delRow = async (id: number) => {
  const res = await http.get(`/article/del?id=${id}`);
  if (res.code === 200) {
    ElMessage.success("已删除");
    await getTableData();
  }
};
const batchDel = async () => {
  if (!multipleSelection.value.length) return ElMessage.warning("请先选择");
  const res = await http.post("/article/batchDel", selectedIds.value);
  if (res.code === 200) {
    ElMessage.success("批量删除成功");
    multipleSelection.value = [];
    await getTableData();
  }
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
  if (!form.value.title?.trim()) return ElMessage.warning("请填写标题");
  if (!form.value.intro?.trim()) return ElMessage.warning("请填写简介");
  if (!form.value.coverUrl) return ElMessage.warning("请上传封面");
  if (!form.value.content?.trim()) return ElMessage.warning("请填写正文");
  const fid = (form.value as any).id;
  if (isEdit.value) {
    const res = await http.post(`/article/edit?id=${fid}`, form.value);
    if (res.code === 200) {
      ElMessage.success("保存成功");
      visible.value = false;
      await getTableData();
    }
  } else {
    const res = await http.post(`/article/add`, form.value);
    if (res.code === 200) {
      ElMessage.success("发布成功");
      visible.value = false;
      await getTableData();
    }
  }
};

const handleSizeChange = (v: number) => {
  params.value.pageSize = v;
  getTableData();
};
const handleCurrentChange = (v: number) => {
  params.value.pageNum = v;
  getTableData();
};

onMounted(() => getTableData());
onBeforeUnmount(() => {
  editorRef.value?.destroy();
  editorRef.value = null;
});
</script>

<template>
  <div class="studio-page">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="params.title" placeholder="标题关键词" clearable class="w220" @keyup.enter="handleSearch" />
        <el-input v-model="params.intro" placeholder="简介关键词" clearable class="w220" @keyup.enter="handleSearch" />
        <el-button type="primary" round :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button round :icon="Refresh" @click="handleReset">重置</el-button>
      </div>
      <div class="toolbar-right">
        <el-button v-if="hasPermission('article', '新增')" type="primary" round class="cta" :icon="Plus" @click="handleAdd"
          >写资讯</el-button
        >
        <el-button
          v-if="hasPermission('article', '删除')"
          round
          type="danger"
          plain
          :disabled="!hasSelected"
          :icon="Delete"
          @click="batchDel"
          >批量删除</el-button
        >
      </div>
    </div>
    <p class="hint">共 {{ total }} 篇</p>

    <div v-if="!tableData.length" class="empty"><el-empty description="暂无资讯" /></div>
    <div v-else class="grid">
      <article v-for="row in tableData" :key="row.id" class="news-card">
        <div class="corner">
          <el-checkbox
            v-if="hasPermission('article', '删除')"
            :model-value="isSelected(row)"
            @change="(v: boolean) => toggleSelect(row, v)"
          />
        </div>
        <div class="row">
          <div class="cov">
            <el-image v-if="row.coverUrl" :src="getImageUrl(row.coverUrl)" fit="cover" class="cov-img">
              <template #error><el-icon><Picture /></el-icon></template>
            </el-image>
            <div v-else class="no-cov"><el-icon><Picture /></el-icon></div>
          </div>
          <div class="txt">
            <h3>{{ row.title }}</h3>
            <p>{{ row.intro }}</p>
            <div class="meta">
              <span>阅读 {{ row.viewCount ?? 0 }}</span>
              <span>{{ row.updateTime?.slice(0, 10) }}</span>
            </div>
            <div class="acts">
              <el-button type="info" link :icon="View" @click="showContent(row.content)">预览正文</el-button>
              <el-button v-if="hasPermission('article', '编辑')" type="primary" link :icon="Edit" @click="handleEdit(row)"
                >编辑</el-button
              >
              <el-popconfirm title="删除该资讯？" @confirm="delRow(row.id)">
                <template #reference>
                  <el-button v-if="hasPermission('article', '删除')" type="danger" link :icon="Delete">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
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

    <el-dialog v-model="visible" :title="dialogTitle" width="840px" top="3vh" destroy-on-close append-to-body class="studio-dialog">
      <el-form :model="form" label-width="88px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.intro" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="封面">
          <el-upload
            class="up"
            :action="uploadUrl"
            :headers="uploadData"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <el-image v-if="imageUrl" :src="imageUrl" fit="cover" class="up-img" />
            <el-icon v-else class="up-ph"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="正文">
          <div class="editor-shell">
            <Toolbar style="border-bottom: 1px solid #e8e0d8" :editor="editorRef" :defaultConfig="toolbarConfig" mode="default" />
            <Editor
              style="height: 400px; overflow-y: auto"
              v-model="form.content"
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

    <el-dialog v-model="contentVisible" title="正文预览" width="720px" append-to-body destroy-on-close>
      <div class="html-preview" v-html="currentContent" />
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.studio-page {
  .toolbar {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 8px;
  }
  .toolbar-left,
  .toolbar-right {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    align-items: center;
  }
  .w220 {
    width: min(220px, 100%);
  }
  .cta {
    box-shadow: 0 8px 22px rgba(180, 58, 58, 0.28);
  }
  .hint {
    font-size: 13px;
    color: #7a6a62;
    margin: 0 0 14px;
  }
  .empty {
    padding: 36px 0;
  }
  .grid {
    display: flex;
    flex-direction: column;
    gap: 14px;
  }
  .news-card {
    position: relative;
    border-radius: 16px;
    padding: 16px 18px;
    background: linear-gradient(105deg, #fff 0%, #fff9f3 100%);
    border: 1px solid rgba(92, 38, 36, 0.1);
    box-shadow: 0 10px 28px rgba(45, 28, 18, 0.06);
  }
  .corner {
    position: absolute;
    top: 12px;
    right: 14px;
  }
  .row {
    display: flex;
    gap: 16px;
    align-items: stretch;
    padding-right: 36px;
  }
  .cov {
    flex-shrink: 0;
    width: 128px;
    border-radius: 12px;
    overflow: hidden;
  }
  .cov-img {
    width: 128px;
    height: 88px;
  }
  .no-cov {
    width: 128px;
    height: 88px;
    background: #f0e8df;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #b0a090;
    font-size: 28px;
  }
  .txt h3 {
    margin: 0 0 8px;
    font-size: 17px;
    color: #3a2f2c;
  }
  .txt p {
    margin: 0 0 10px;
    font-size: 13px;
    color: #6e625c;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  .meta {
    font-size: 12px;
    color: #9a8f88;
    display: flex;
    gap: 16px;
    margin-bottom: 8px;
  }
  .acts {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }
  .pagination-container {
    margin-top: 24px;
    display: flex;
    justify-content: center;
  }
  .up {
    width: 140px;
    height: 140px;
    border-radius: 12px;
    border: 2px dashed rgba(92, 38, 36, 0.2);
    overflow: hidden;
  }
  .up-img {
    width: 140px;
    height: 140px;
  }
  .up-ph {
    width: 140px;
    height: 140px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: #b5a69a;
  }
  .editor-shell {
    border: 1px solid #e8e0d8;
    border-radius: 12px;
    overflow: hidden;
    width: 100%;
  }
  .html-preview {
    max-height: 480px;
    overflow-y: auto;
    padding: 12px;
    border-radius: 10px;
    background: #faf7f4;
    border: 1px solid #ebe3d8;
  }
}
</style>
