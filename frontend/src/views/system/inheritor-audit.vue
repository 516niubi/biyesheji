<script setup lang="ts">
import http from "../../utils/http";
import {
  Search,
  Refresh,
  CircleCheck,
  CircleClose,
  Document,
  Picture,
  Link,
} from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { onMounted, ref } from "vue";
import { getImageUrl, hasPermission } from "../../utils/system";

function certKind(path: string): "pdf" | "image" | "other" {
  const lower = (path.split("?")[0] || "").toLowerCase();
  if (lower.endsWith(".pdf")) return "pdf";
  if (/\.(jpe?g|png|gif|webp|bmp|svg)$/.test(lower)) return "image";
  return "other";
}

function certFileLabel(path: string): string {
  return (path || "").replace(/^.*[/\\]/, "") || path || "附件";
}

const initParams = { pageNum: 1, pageSize: 10, username: "", phone: "", status: "" as string | number };
const params = ref({ ...initParams });
const tableData = ref<any[]>([]);
const total = ref(0);

const statusDict: Record<number, string> = { 0: "待审核", 1: "已通过", 2: "已拒绝" };

const getTableData = async () => {
  let url = `/inheritor/admin/page?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}`;
  if (params.value.username) url += `&username=${encodeURIComponent(String(params.value.username))}`;
  if (params.value.phone) url += `&phone=${encodeURIComponent(String(params.value.phone))}`;
  if (params.value.status !== "" && params.value.status != null) url += `&status=${params.value.status}`;
  const res = await http.get(url);
  if (res.code === 200) {
    tableData.value = res.data.records;
    total.value = res.data.total;
  }
};


const parseCerts = (json: string) => {
  try {
    const arr = JSON.parse(json || "[]");
    return Array.isArray(arr) ? arr : [];
  } catch {
    return [];
  }
};

const audit = async (row: any, status: number) => {
  if (!hasPermission("inheritorAudit", "审核")) return;
  try {
    let remark = "";
    if (status === 2) {
      const { value } = await ElMessageBox.prompt("请输入拒绝原因（可选）", "拒绝该申请？", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        inputPlaceholder: "拒绝原因",
      });
      remark = value || "";
    } else {
      const { value } = await ElMessageBox.prompt(
        "确定通过该传承人注册？可在下方填写审核说明（选填），将写入「审核备注」。",
        "审核通过",
        {
          confirmButtonText: "确定通过",
          cancelButtonText: "取消",
          inputPlaceholder: "审核备注（选填）",
        }
      );
      remark = (value || "").trim();
    }
    const res = await http.post("/inheritor/admin/audit", {
      id: row.id,
      status,
      remark,
    });
    if (res.code === 200) {
      ElMessage.success("操作成功");
      await getTableData();
    }
  } catch {
    /* cancel */
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
        <span class="label">手机</span>
        <el-input v-model="params.phone" placeholder="手机号" clearable />
      </div>
      <div class="flex form-item">
        <span class="label">状态</span>
        <el-select v-model="params.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </el-select>
      </div>
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button type="warning" :icon="Refresh" @click="handleReset">重置</el-button>
    </header>

    <el-table :data="tableData" border>
      <el-table-column prop="username" label="账号" width="110" />
      <el-table-column prop="nickName" label="昵称" width="100" />
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column prop="profile" label="个人简介" min-width="200" show-overflow-tooltip />
      <el-table-column label="认证材料" min-width="140">
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
      <el-table-column label="状态" width="90">
        <template #default="{ row }">{{ statusDict[row.status] }}</template>
      </el-table-column>
      <el-table-column label="审核备注" show-overflow-tooltip min-width="140">
        <template #default="{ row }">
          <span>{{ row.auditRemark?.trim() ? row.auditRemark : "—" }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" width="170" />
      <el-table-column label="操作" fixed="right" width="200">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button
              v-if="hasPermission('inheritorAudit', '审核')"
              type="success"
              :icon="CircleCheck"
              link
              @click="audit(row, 1)"
              >通过</el-button
            >
            <el-button
              v-if="hasPermission('inheritorAudit', '审核')"
              type="danger"
              :icon="CircleClose"
              link
              @click="audit(row, 2)"
              >拒绝</el-button
            >
          </template>
          <span v-else class="muted">已处理</span>
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
.muted {
  color: #909399;
  font-size: 13px;
}
.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>
