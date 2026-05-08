<script setup lang="ts">
import http from "@/utils/http";
import { Plus, Search, Refresh, Edit, Delete, VideoPlay } from "@element-plus/icons-vue";
import { ElMessage, type UploadProps } from "element-plus";
import { getImageUrl, hasPermission } from "@/utils/system";
import Config from "@/config/index";
import { computed, onMounted, ref } from "vue";

const initParams = { pageNum: 1, pageSize: 12, title: "" };
const params = ref({ ...initParams });
const initForm = { title: "", url: "", coverUrl: "", viewCount: 0 };
const form = ref({ ...initForm });
const tableData = ref<any[]>([]);
const total = ref(0);
const visible = ref(false);
const videoPreviewVisible = ref(false);
const videoOptions = ref({ title: "", src: "" });
const multipleSelection = ref<any[]>([]);
const selectedIds = computed(() => multipleSelection.value.map((item) => item.id));
const hasSelected = computed(() => multipleSelection.value.length > 0);
const isEdit = computed(() => (form.value as any).id > 0);
const dialogTitle = computed(() => (isEdit.value ? "编辑视频" : "上传视频"));

const uploadUrl = ref(`${Config.baseUrl}/file/upload`);
const uploadData = { authorization: `${localStorage.getItem("token")}` };
const coverImageUrl = ref("");

const getTableData = async () => {
  const res = await http.get(
    `/video/manage/page?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}&title=${params.value.title}`
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
  coverImageUrl.value = "";
  visible.value = true;
};
const handleEdit = (row: any) => {
  coverImageUrl.value = row.coverUrl ? getImageUrl(row.coverUrl) : "";
  form.value = { ...row };
  visible.value = true;
};
const delRow = async (id: number) => {
  const res = await http.get(`/video/del?id=${id}`);
  if (res.code === 200) {
    ElMessage.success("删除成功");
    await getTableData();
  }
};
const batchDel = async () => {
  if (!multipleSelection.value.length) return ElMessage.warning("请先选择");
  const res = await http.post("/video/batchDel", selectedIds.value);
  if (res.code === 200) {
    ElMessage.success("已批量删除");
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
  if (!form.value.url) return ElMessage.warning("请上传视频文件");
  if (!form.value.coverUrl) return ElMessage.warning("请上传封面");
  const fid = (form.value as any).id;
  if (isEdit.value) {
    const res = await http.post(`/video/edit?id=${fid}`, form.value);
    if (res.code === 200) {
      ElMessage.success("保存成功");
      visible.value = false;
      await getTableData();
    }
  } else {
    const res = await http.post(`/video/add`, form.value);
    if (res.code === 200) {
      ElMessage.success("发布成功");
      visible.value = false;
      await getTableData();
    }
  }
};

const previewVideo = (url: string) => {
  videoOptions.value.src = getImageUrl(url);
  videoPreviewVisible.value = true;
};

const beforeVideoUpload: UploadProps["beforeUpload"] = (rawFile) => {
  if (!rawFile.type.includes("video")) {
    ElMessage.error("仅支持视频文件");
    return false;
  }
  if (rawFile.size / 1024 / 1024 > 50) {
    ElMessage.error("视频不超过 50MB");
    return false;
  }
  return true;
};
const handleVideoSuccess: UploadProps["onSuccess"] = (response: any) => {
  form.value.url = response.data;
  ElMessage.success("视频已上传");
};
const beforeCoverUpload: UploadProps["beforeUpload"] = (rawFile) => {
  if (!rawFile.type.includes("image")) return ElMessage.error("封面须为图片"), false;
  if (rawFile.size / 1024 / 1024 > 2) return ElMessage.error("封面不超过 2MB"), false;
  return true;
};
const handleCoverSuccess: UploadProps["onSuccess"] = (response: any) => {
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

onMounted(() => getTableData());
</script>

<template>
  <div class="studio-page">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="params.title" placeholder="搜索视频标题…" clearable class="search-input" @keyup.enter="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" round @click="handleSearch">搜索</el-button>
        <el-button round @click="handleReset"><el-icon class="mr"><Refresh /></el-icon>重置</el-button>
      </div>
      <div class="toolbar-right">
        <el-button v-if="hasPermission('video', '新增')" type="primary" round class="cta" @click="handleAdd">
          <el-icon><Plus /></el-icon>上传视频
        </el-button>
        <el-button v-if="hasPermission('video', '删除')" round type="danger" plain :disabled="!hasSelected" @click="batchDel">
          <el-icon><Delete /></el-icon>批量删除
        </el-button>
      </div>
    </div>
    <p class="hint">共 {{ total }} 支视频</p>

    <div v-if="!tableData.length" class="empty"><el-empty description="暂无视频作品" /></div>
    <div v-else class="card-grid">
      <article v-for="row in tableData" :key="row.id" class="vid-card">
        <div class="select-corner">
          <el-checkbox
            v-if="hasPermission('video', '删除')"
            :model-value="isSelected(row)"
            @change="(v: boolean) => toggleSelect(row, v)"
          />
        </div>
        <div class="thumb" @click="previewVideo(row.url)">
          <el-image :src="getImageUrl(row.coverUrl)" fit="cover" class="thumb-img" />
          <div class="play-mask"><el-icon :size="36"><VideoPlay /></el-icon></div>
        </div>
        <div class="body">
          <h3>{{ row.title }}</h3>
          <div class="meta">
            <span>播放 {{ row.viewCount ?? 0 }}</span>
            <span>{{ row.createTime?.slice(0, 10) }}</span>
          </div>
          <div class="actions">
            <el-button v-if="hasPermission('video', '编辑')" type="primary" link :icon="Edit" @click="handleEdit(row)"
              >编辑</el-button
            >
            <el-popconfirm title="确定删除？" @confirm="delRow(row.id)">
              <template #reference>
                <el-button v-if="hasPermission('video', '删除')" type="danger" link :icon="Delete">删除</el-button>
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

    <el-dialog v-model="visible" :title="dialogTitle" width="560px" destroy-on-close append-to-body class="studio-dialog">
      <el-form :model="form" label-width="96px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="视频文件">
          <el-upload
            :action="uploadUrl"
            :headers="uploadData"
            :show-file-list="false"
            :on-success="handleVideoSuccess"
            :before-upload="beforeVideoUpload"
            accept="video/*"
          >
            <el-button type="primary" :icon="Plus">选择文件</el-button>
          </el-upload>
          <span v-if="form.url" class="ok">已上传</span>
        </el-form-item>
        <el-form-item label="封面">
          <el-upload
            class="cov"
            :action="uploadUrl"
            :headers="uploadData"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            :before-upload="beforeCoverUpload"
            accept="image/*"
          >
            <el-image v-if="coverImageUrl" :src="coverImageUrl" fit="cover" class="cov-img" />
            <el-icon v-else class="cov-ph"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="播放量"><el-input-number v-model="form.viewCount" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="videoPreviewVisible" title="预览" width="800px" append-to-body destroy-on-close @closed="videoOptions.src = ''">
      <video v-if="videoOptions.src" class="player" :src="videoOptions.src" controls preload="metadata" />
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
  .search-input {
    width: min(300px, 100%);
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
    padding: 36px 0;
  }
  .card-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 18px;
  }
  .vid-card {
    position: relative;
    border-radius: 16px;
    overflow: hidden;
    background: linear-gradient(180deg, #fff, #fffaf7);
    border: 1px solid rgba(92, 38, 36, 0.1);
    box-shadow: 0 12px 32px rgba(45, 28, 18, 0.07);
    transition: transform 0.2s ease;
    &:hover {
      transform: translateY(-4px);
    }
  }
  .select-corner {
    position: absolute;
    top: 10px;
    left: 10px;
    z-index: 2;
    background: rgba(255, 255, 255, 0.88);
    padding: 2px 8px;
    border-radius: 8px;
  }
  .thumb {
    position: relative;
    cursor: pointer;
  }
  .thumb-img {
    width: 100%;
    height: 152px;
    display: block;
  }
  .play-mask {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.35);
    color: #fff;
    opacity: 0;
    transition: opacity 0.2s;
  }
  .thumb:hover .play-mask {
    opacity: 1;
  }
  .body {
    padding: 12px 14px 16px;
  }
  .body h3 {
    margin: 0 0 8px;
    font-size: 16px;
    color: #3a2f2c;
  }
  .meta {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: #9a8f88;
    margin-bottom: 8px;
  }
  .actions {
    display: flex;
    gap: 4px;
  }
  .pagination-container {
    margin-top: 26px;
    display: flex;
    justify-content: center;
  }
  .cov {
    width: 140px;
    height: 140px;
    border: 2px dashed rgba(92, 38, 36, 0.2);
    border-radius: 12px;
    overflow: hidden;
  }
  .cov-img {
    width: 140px;
    height: 140px;
  }
  .cov-ph {
    width: 140px;
    height: 140px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: #b5a69a;
  }
  .ok {
    margin-left: 10px;
    color: #67c23a;
    font-size: 13px;
  }
  .player {
    width: 100%;
    max-height: 440px;
    border-radius: 10px;
    background: #000;
  }
}
</style>
