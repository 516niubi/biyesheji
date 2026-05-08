<script setup lang="ts">
import http from "@/utils/http";
import { Plus, Search, Refresh, Edit, Delete } from "@element-plus/icons-vue";
import { ElMessage, type UploadProps } from "element-plus";
import { getImageUrl, hasPermission } from "@/utils/system";
import Config from "@/config/index";
import { Picture as IconPicture } from "@element-plus/icons-vue";
import { computed, onMounted, ref } from "vue";

const initParams = { pageNum: 1, pageSize: 12, title: "" };
const params = ref({ ...initParams });
const initForm = {
  title: "",
  coverImage: "",
  content: "",
  startTime: "",
  endTime: "",
  address: "",
  maxPeople: 0,
  status: 1,
  viewCount: 0,
};
const form = ref({ ...initForm });
const tableData = ref<any[]>([]);
const total = ref(0);
const visible = ref(false);
const uploadUrl = ref(`${Config.baseUrl}/file/upload`);
const uploadData = { authorization: `${localStorage.getItem("token")}` };
const imageUrl = ref("");
const multipleSelection = ref<any[]>([]);
const selectedIds = computed(() => multipleSelection.value.map((i) => i.id));
const hasSelected = computed(() => multipleSelection.value.length > 0);
const isEdit = computed(() => (form.value as any).id > 0);
const dialogTitle = computed(() => (isEdit.value ? "编辑活动" : "发布活动"));
const statusDict: Record<number, string> = { 0: "未开始", 1: "进行中", 2: "已结束" };

const getTableData = async () => {
  const res = await http.get(
    `/activity/manage/page?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}&title=${params.value.title}`
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
  const res = await http.get(`/activity/del?id=${id}`);
  if (res.code === 200) {
    ElMessage.success("已删除");
    await getTableData();
  }
};
const batchDel = async () => {
  if (!multipleSelection.value.length) return ElMessage.warning("请先选择");
  const res = await http.post("/activity/batchDel", selectedIds.value);
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
  if (!form.value.content?.trim()) return ElMessage.warning("请填写内容");
  if (!form.value.startTime || !form.value.endTime) return ElMessage.warning("请选择时间");
  if (new Date(form.value.startTime).getTime() > new Date(form.value.endTime).getTime())
    return ElMessage.warning("结束时间不能早于开始");
  if (!form.value.address?.trim()) return ElMessage.warning("请填写地址");
  if (!form.value.maxPeople || form.value.maxPeople <= 0) return ElMessage.warning("人数须大于 0");
  if (!form.value.coverImage) return ElMessage.warning("请上传封面");
  const fid = (form.value as any).id;
  if (isEdit.value) {
    const res = await http.post(`/activity/edit?id=${fid}`, form.value);
    if (res.code === 200) {
      ElMessage.success("保存成功");
      visible.value = false;
      await getTableData();
    }
  } else {
    const res = await http.post(`/activity/add`, form.value);
    if (res.code === 200) {
      ElMessage.success("发布成功");
      visible.value = false;
      await getTableData();
    }
  }
};

const beforeAvatarUpload: UploadProps["beforeUpload"] = (rawFile) => {
  if (!rawFile.type.includes("image")) return ElMessage.error("须为图片"), false;
  if (rawFile.size / 1024 / 1024 > 2) return ElMessage.error("不超过 2MB"), false;
  return true;
};
const handleAvatarSuccess: UploadProps["onSuccess"] = (response: any) => {
  form.value.coverImage = response.data;
  imageUrl.value = `${Config.baseUrl}${response.data}`;
};

const handleSizeChange = (v: number) => {
  params.value.pageSize = v;
  getTableData();
};
const handleCurrentChange = (v: number) => {
  params.value.pageNum = v;
  getTableData();
};

const statusTag = (s: number) => {
  if (s === 0) return "info";
  if (s === 1) return "success";
  return "warning";
};

onMounted(() => getTableData());
</script>

<template>
  <div class="studio-page">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="params.title" placeholder="活动标题…" clearable class="w260" @keyup.enter="handleSearch" />
        <el-button type="primary" round :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button round :icon="Refresh" @click="handleReset">重置</el-button>
      </div>
      <div class="toolbar-right">
        <el-button v-if="hasPermission('activity', '新增')" type="primary" round class="cta" :icon="Plus" @click="handleAdd"
          >发起活动</el-button
        >
        <el-button
          v-if="hasPermission('activity', '删除')"
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
    <p class="hint">共 {{ total }} 场活动</p>

    <div v-if="!tableData.length" class="empty"><el-empty description="暂无活动" /></div>
    <div v-else class="grid">
      <article v-for="row in tableData" :key="row.id" class="act-card">
        <div class="corner">
          <el-checkbox
            v-if="hasPermission('activity', '删除')"
            :model-value="isSelected(row)"
            @change="(v: boolean) => toggleSelect(row, v)"
          />
        </div>
        <div class="hero" @click="handleEdit(row)">
          <el-image :src="getImageUrl(row.coverImage)" fit="cover" class="hero-img">
            <template #error><div class="ph"><el-icon><IconPicture /></el-icon></div></template>
          </el-image>
          <el-tag class="tag" :type="statusTag(row.status)" effect="dark">{{ statusDict[row.status] }}</el-tag>
        </div>
        <div class="body">
          <h3 @click="handleEdit(row)">{{ row.title }}</h3>
          <p class="content-preview">{{ row.content }}</p>
          <ul class="facts">
            <li>{{ row.startTime?.slice(0, 16) }} ~ {{ row.endTime?.slice(0, 16) }}</li>
            <li>{{ row.address }}</li>
            <li>限额 {{ row.maxPeople }} 人 · 浏览 {{ row.viewCount ?? 0 }}</li>
          </ul>
          <div class="acts">
            <el-button v-if="hasPermission('activity', '编辑')" type="primary" link :icon="Edit" @click="handleEdit(row)"
              >编辑</el-button
            >
            <el-popconfirm title="删除该活动？" @confirm="delRow(row.id)">
              <template #reference>
                <el-button v-if="hasPermission('activity', '删除')" type="danger" link :icon="Delete">删除</el-button>
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

    <el-dialog v-model="visible" :title="dialogTitle" width="720px" destroy-on-close append-to-body>
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :autosize="{ minRows: 4, maxRows: 10 }" />
        </el-form-item>
        <el-form-item label="开始">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="人数"><el-input-number v-model="form.maxPeople" :min="1" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">未开始</el-radio>
            <el-radio :value="1">进行中</el-radio>
            <el-radio :value="2">已结束</el-radio>
          </el-radio-group>
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
  .w260 {
    width: min(280px, 100%);
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
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 18px;
  }
  .act-card {
    position: relative;
    border-radius: 18px;
    overflow: hidden;
    background: #fff;
    border: 1px solid rgba(92, 38, 36, 0.1);
    box-shadow: 0 14px 36px rgba(45, 28, 18, 0.08);
    transition: transform 0.2s ease;
    &:hover {
      transform: translateY(-4px);
    }
  }
  .corner {
    position: absolute;
    top: 10px;
    left: 10px;
    z-index: 2;
    background: rgba(255, 255, 255, 0.9);
    padding: 2px 8px;
    border-radius: 8px;
  }
  .hero {
    position: relative;
    cursor: pointer;
  }
  .hero-img {
    width: 100%;
    height: 160px;
    display: block;
  }
  .ph {
    height: 160px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #eee8e0;
    font-size: 36px;
    color: #b0a090;
  }
  .tag {
    position: absolute;
    top: 12px;
    right: 12px;
  }
  .body {
    padding: 14px 16px 18px;
  }
  .body h3 {
    margin: 0 0 8px;
    font-size: 17px;
    cursor: pointer;
    color: #3a2f2c;
    &:hover {
      color: var(--el-color-primary);
    }
  }
  .content-preview {
    margin: 0 0 10px;
    font-size: 13px;
    color: #7a6f68;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    min-height: 2.6em;
    line-height: 1.45;
  }
  .facts {
    margin: 0 0 12px;
    padding-left: 18px;
    font-size: 12px;
    color: #9a8f88;
    line-height: 1.55;
  }
  .acts {
    display: flex;
    gap: 6px;
  }
  .pagination-container {
    margin-top: 26px;
    display: flex;
    justify-content: center;
  }
  .up {
    width: 160px;
    height: 160px;
    border-radius: 12px;
    border: 2px dashed rgba(92, 38, 36, 0.2);
    overflow: hidden;
  }
  .up-img {
    width: 160px;
    height: 160px;
  }
  .up-ph {
    width: 160px;
    height: 160px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: #b5a69a;
  }
}
</style>
