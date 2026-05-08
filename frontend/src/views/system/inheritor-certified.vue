<script setup lang="ts">
import http from "../../utils/http";
import Config from "../../config/index";
import {
  Search,
  Refresh,
  Edit,
  Delete,
  Plus,
  Document,
  Picture,
  Link,
} from "@element-plus/icons-vue";
import { Picture as IconPicture } from "@element-plus/icons-vue";
import type { UploadProps, UploadRawFile } from "element-plus";
import { ElMessage } from "element-plus";
import { computed, onMounted, ref } from "vue";
import { genderDict } from "../../config/dict";
import { getImageUrl, hasPermission } from "../../utils/system";

const APPROVED = 1;
const initParams = { pageNum: 1, pageSize: 10, username: "", phone: "", nickName: "" };
const params = ref({ ...initParams });
const tableData = ref<any[]>([]);
const total = ref(0);

const multipleSelection = ref<any[]>([]);
const selectedIds = computed(() => multipleSelection.value.map((item) => item.id));
const hasSelected = computed(() => multipleSelection.value.length > 0);

const visible = ref(false);
const editCertUrls = ref<string[]>([]);
const imageUrl = ref("");
const uploadUrl = `${Config.baseUrl}/file/upload`;
const uploadHeaders = {
  authorization: `${localStorage.getItem("token") || ""}`,
};

const initForm = {
  id: 0,
  username: "",
  nickName: "",
  phone: "",
  gender: 2,
  age: 18,
  avatar: "",
  profile: "",
};
const form = ref({ ...initForm });

function certKind(path: string): "pdf" | "image" | "other" {
  const lower = (path.split("?")[0] || "").toLowerCase();
  if (lower.endsWith(".pdf")) return "pdf";
  if (/\.(jpe?g|png|gif|webp|bmp|svg)$/.test(lower)) return "image";
  return "other";
}

function certFileLabel(path: string): string {
  const s = (path || "").replace(/^.*[/\\]/, "") || path || "附件";
  return s;
}

const parseCerts = (json: string) => {
  try {
    const arr = JSON.parse(json || "[]");
    return Array.isArray(arr) ? arr : [];
  } catch {
    return [];
  }
};

const getTableData = async () => {
  let url = `/inheritor/admin/page?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}&status=${APPROVED}`;
  if (params.value.username) url += `&username=${encodeURIComponent(String(params.value.username))}`;
  if (params.value.phone) url += `&phone=${encodeURIComponent(String(params.value.phone))}`;
  if (params.value.nickName) url += `&nickName=${encodeURIComponent(String(params.value.nickName))}`;
  const res = await http.get(url);
  if (res.code === 200) {
    tableData.value = res.data.records;
    total.value = res.data.total;
  }
};

const handleSelectionChange = (selection: any[]) => {
  multipleSelection.value = selection;
};

const handleEdit = (row: any) => {
  form.value = {
    id: row.id,
    username: row.username,
    nickName: row.nickName,
    phone: row.phone,
    gender: row.gender ?? 2,
    age: row.age ?? 18,
    avatar: row.avatar || "",
    profile: row.profile || "",
  };
  editCertUrls.value = [...parseCerts(row.certificateUrls)];
  imageUrl.value = row.avatar ? getImageUrl(row.avatar) : "";
  visible.value = true;
};

const delRow = async (id: number) => {
  const res = await http.get(`/inheritor/admin/del?id=${id}`);
  if (res.code === 200) {
    ElMessage.success("删除成功");
    await getTableData();
  }
};

const batchDel = async () => {
  if (!multipleSelection.value.length) {
    ElMessage.warning("请先选择要删除的记录");
    return;
  }
  const res = await http.post("/inheritor/admin/batchDel", selectedIds.value);
  if (res.code === 200) {
    ElMessage.success("批量删除成功");
    multipleSelection.value = [];
    await getTableData();
  }
};

const beforeAvatarUpload: UploadProps["beforeUpload"] = (rawFile) => {
  if (!rawFile.type.includes("image")) {
    ElMessage.error("头像仅支持图片");
    return false;
  }
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error("图片不超过 2MB");
    return false;
  }
  return true;
};

const handleAvatarSuccess: UploadProps["onSuccess"] = (response: { code?: number; data?: string }) => {
  if (response?.code === 200 && response.data) {
    form.value.avatar = response.data;
    imageUrl.value = `${Config.baseUrl}${response.data}`;
  }
};

const onCertSuccess = (response: { code?: number; data?: string }) => {
  if (response?.code === 200 && response.data) {
    editCertUrls.value.push(response.data);
    ElMessage.success("材料已上传");
  }
};

const beforeCertUpload = (raw: UploadRawFile) => {
  const ok =
    raw.type.includes("image") ||
    raw.type === "application/pdf" ||
    raw.name?.toLowerCase().endsWith(".pdf");
  if (!ok) {
    ElMessage.error("认证资料仅支持图片或 PDF");
    return false;
  }
  if (raw.size / 1024 / 1024 > 15) {
    ElMessage.error("单文件不超过 15MB");
    return false;
  }
  return true;
};

const removeCert = (url: string) => {
  editCertUrls.value = editCertUrls.value.filter((u) => u !== url);
};

const submit = async () => {
  if (!form.value.phone?.trim()) {
    ElMessage.warning("请填写手机号");
    return;
  }
  if (!editCertUrls.value.length) {
    ElMessage.warning("请至少保留一份认证资料");
    return;
  }
  const res = await http.post("/inheritor/admin/update", {
    id: form.value.id,
    nickName: form.value.nickName,
    phone: form.value.phone.trim(),
    gender: form.value.gender,
    age: form.value.age,
    avatar: form.value.avatar || undefined,
    profile: form.value.profile,
    certificateUrls: editCertUrls.value,
  });
  if (res.code === 200) {
    ElMessage.success("保存成功");
    visible.value = false;
    await getTableData();
  }
};

const handleSearch = () => getTableData();
const handleReset = () => {
  params.value = { ...initParams };
  getTableData();
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
</script>

<template>
  <div>
    <header class="flex header-row">
      <div class="flex form-item">
        <span class="label">账号</span>
        <el-input v-model="params.username" placeholder="用户名" clearable />
      </div>
      <div class="flex form-item">
        <span class="label">昵称</span>
        <el-input v-model="params.nickName" placeholder="昵称" clearable />
      </div>
      <div class="flex form-item">
        <span class="label">手机</span>
        <el-input v-model="params.phone" placeholder="手机号" clearable />
      </div>
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button type="warning" :icon="Refresh" @click="handleReset">重置</el-button>
    </header>

    <div class="op-box flex">
      <el-button
        v-if="hasPermission('inheritorCertified', '删除')"
        type="danger"
        :icon="Delete"
        :disabled="!hasSelected"
        @click="batchDel"
      >
        批量删除
      </el-button>
    </div>

    <el-table :data="tableData" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="username" label="账号" width="120" />
      <el-table-column prop="nickName" label="昵称" width="100" />
      <el-table-column prop="age" label="年龄" width="72" />
      <el-table-column prop="gender" label="性别" width="80">
        <template #default="{ row }">{{ genderDict[row.gender] ?? "—" }}</template>
      </el-table-column>
      <el-table-column label="头像" width="88">
        <template #default="{ row }">
          <el-image
            v-if="row.avatar"
            :src="getImageUrl(row.avatar)"
            :preview-src-list="[getImageUrl(row.avatar)]"
            preview-teleported
            fit="cover"
            class="avatar-thumb"
          />
          <el-icon v-else class="avatar-placeholder"><IconPicture /></el-icon>
        </template>
      </el-table-column>
      <el-table-column prop="profile" label="个人简介" min-width="160" show-overflow-tooltip />
      <el-table-column label="认证资料" min-width="140">
        <template #default="{ row }">
          <template v-if="parseCerts(row.certificateUrls).length">
            <a
              v-for="(u, i) in parseCerts(row.certificateUrls)"
              :key="i"
              :href="getImageUrl(u)"
              target="_blank"
              rel="noopener noreferrer"
              class="cert-icon-link"
            >
              <el-tooltip :content="certFileLabel(u)" placement="top">
                <span class="cert-icon-hit">
                  <el-icon v-if="certKind(u) === 'pdf'" :size="22" class="cert-pdf"><Document /></el-icon>
                  <el-icon v-else-if="certKind(u) === 'image'" :size="22" class="cert-img"><Picture /></el-icon>
                  <el-icon v-else :size="22" class="cert-other"><Link /></el-icon>
                </span>
              </el-tooltip>
            </a>
          </template>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="auditTime" label="审核通过时间" width="170" />
      <el-table-column prop="createTime" label="申请时间" width="170" />
      <el-table-column label="操作" fixed="right" width="200">
        <template #default="{ row }">
          <el-button
            v-if="hasPermission('inheritorCertified', '编辑')"
            type="primary"
            :icon="Edit"
            link
            @click="handleEdit(row)"
            >编辑</el-button
          >
          <el-popconfirm
            v-if="hasPermission('inheritorCertified', '删除')"
            title="确定删除该传承人账号？"
            @confirm="delRow(row.id)"
          >
            <template #reference>
              <el-button type="danger" :icon="Delete" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="params.pageNum"
        v-model:page-size="params.pageSize"
        :page-sizes="[10, 20, 50]"
        layout="sizes, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog v-model="visible" title="编辑传承人" width="620px" destroy-on-close @closed="editCertUrls = []">
      <el-form :model="form" label-width="100px">
        <el-form-item label="账号">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickName" placeholder="昵称" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="form.age" :min="1" :max="120" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="0">男</el-radio>
            <el-radio :value="1">女</el-radio>
            <el-radio :value="2">保密</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="form.profile" type="textarea" :autosize="{ minRows: 3, maxRows: 8 }" />
        </el-form-item>
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <el-image v-if="imageUrl" :src="imageUrl" fit="cover" class="avatar-preview" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="认证资料">
          <div class="cert-edit-row">
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="onCertSuccess"
              :before-upload="beforeCertUpload"
            >
              <el-button type="primary" size="small" :icon="Plus">上传图片/PDF</el-button>
            </el-upload>
          </div>
          <div class="cert-edit-list">
            <div v-for="(u, i) in editCertUrls" :key="i" class="cert-edit-item">
              <a :href="getImageUrl(u)" target="_blank" rel="noopener noreferrer" class="cert-icon-link">
                <el-tooltip :content="certFileLabel(u)" placement="top">
                  <span class="cert-icon-hit">
                    <el-icon v-if="certKind(u) === 'pdf'" :size="22" class="cert-pdf"><Document /></el-icon>
                    <el-icon v-else-if="certKind(u) === 'image'" :size="22" class="cert-img"><Picture /></el-icon>
                    <el-icon v-else :size="22" class="cert-other"><Link /></el-icon>
                  </span>
                </el-tooltip>
              </a>
              <el-button type="danger" link size="small" @click="removeCert(u)">移除</el-button>
            </div>
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

<style scoped>
.header-row {
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}
.form-item {
  align-items: center;
  gap: 8px;
}
.label {
  color: #606266;
  white-space: nowrap;
}
.op-box {
  margin-bottom: 12px;
}
.muted {
  color: #909399;
  font-size: 13px;
}
.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
.cert-icon-link {
  display: inline-flex;
  align-items: center;
  margin-right: 10px;
  text-decoration: none;
  vertical-align: middle;
}
.cert-icon-hit {
  display: inline-flex;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  transition: background-color 0.15s;
}
.cert-icon-hit:hover {
  background-color: var(--el-fill-color-light);
}
.cert-pdf {
  color: #e53935;
}
.cert-img {
  color: #2e7d32;
}
.cert-other {
  color: #1565c0;
}
.avatar-thumb {
  width: 48px;
  height: 48px;
  border-radius: 4px;
}
.avatar-placeholder {
  font-size: 28px;
  color: var(--el-text-color-placeholder);
}
.avatar-uploader {
  width: 120px;
  height: 120px;
  border: 1px dashed var(--el-border-color);
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar-uploader:hover {
  border-color: var(--el-color-primary);
}
.avatar-preview {
  width: 120px;
  height: 120px;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}
.cert-edit-row {
  margin-bottom: 8px;
}
.cert-edit-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.cert-edit-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
