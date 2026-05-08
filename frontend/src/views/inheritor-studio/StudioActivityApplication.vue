<script setup lang="ts">
import http from "@/utils/http";
import {
  Calendar,
  Check,
  Clock,
  Close,
  Delete,
  Phone,
  Refresh,
  Search,
  User,
  View,
} from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { computed, onMounted, ref } from "vue";
import { hasPermission, getImageUrl } from "@/utils/system";

const initParams = { pageNum: 1, pageSize: 12, realName: "", phone: "", status: "" };
const params = ref({ ...initParams });
const tableData = ref<any[]>([]);
const total = ref(0);
const loading = ref(false);

const multipleSelection = ref<any[]>([]);
const selectedIds = computed(() => multipleSelection.value.map((i) => i.id));
const hasSelected = computed(() => multipleSelection.value.length > 0);

const statusDict: Record<number, string> = { 0: "待审核", 1: "已通过", 2: "已拒绝" };

const isRowSelected = (row: any) => selectedIds.value.includes(row.id);
const toggleRowSelect = (row: any, checked: boolean) => {
  if (checked) {
    if (!isRowSelected(row)) multipleSelection.value = [...multipleSelection.value, row];
  } else {
    multipleSelection.value = multipleSelection.value.filter((x) => x.id !== row.id);
  }
};

const isAllPageSelected = computed(() => {
  if (!tableData.value.length) return false;
  return tableData.value.every((r) => isRowSelected(r));
});

const isIndeterminate = computed(() => {
  const n = tableData.value.filter((r) => isRowSelected(r)).length;
  return n > 0 && n < tableData.value.length;
});

const toggleSelectAllPage = (val: boolean) => {
  if (val) {
    const ids = new Set(multipleSelection.value.map((x: any) => x.id));
    tableData.value.forEach((r) => {
      if (!ids.has(r.id)) multipleSelection.value.push(r);
    });
  } else {
    const pageIds = new Set(tableData.value.map((r) => r.id));
    multipleSelection.value = multipleSelection.value.filter((x: any) => !pageIds.has(x.id));
  }
};

const getStatusClass = (status: number) => {
  if (status === 0) return "st-pending";
  if (status === 1) return "st-ok";
  if (status === 2) return "st-reject";
  return "st-unknown";
};

const coverSrc = (row: any) => getImageUrl(row.activityCoverImage);

const isEmpty = computed(() => !loading.value && total.value === 0);

const getTableData = async () => {
  loading.value = true;
  try {
    const res = await http.get(
      `/activityApplication/manage/page?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}&realName=${encodeURIComponent(params.value.realName || "")}&phone=${encodeURIComponent(params.value.phone || "")}&status=${params.value.status ?? ""}`
    );
    if (res.code === 200) {
      tableData.value = res.data.records || [];
      total.value = res.data.total ?? 0;
    }
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  params.value.pageNum = 1;
  getTableData();
};

const handleReset = () => {
  params.value = { ...initParams };
  multipleSelection.value = [];
  getTableData();
};

const delRow = async (id: number) => {
  const res = await http.get(`/activityApplication/del?id=${id}`);
  if (res.code === 200) {
    ElMessage.success("已删除");
    multipleSelection.value = multipleSelection.value.filter((x: any) => x.id !== id);
    await getTableData();
  }
};

const batchDel = async () => {
  if (!multipleSelection.value.length) {
    ElMessage.warning("请先选择");
    return;
  }
  const res = await http.post("/activityApplication/batchDel", selectedIds.value);
  if (res.code === 200) {
    ElMessage.success("批量删除成功");
    multipleSelection.value = [];
    await getTableData();
  }
};

const handleApprove = async (row: any) => {
  try {
    await ElMessageBox.confirm(`通过 ${row.realName} 的报名？`, "审核", { type: "warning" });
    const res = await http.post(`/activityApplication/edit?id=${row.id}`, { status: 1 });
    if (res.code === 200) {
      ElMessage.success("已通过");
      await getTableData();
    }
  } catch {
    /* cancel */
  }
};

const handleReject = async (row: any) => {
  try {
    await ElMessageBox.confirm(`拒绝 ${row.realName} 的报名？`, "审核", { type: "warning" });
    const res = await http.post(`/activityApplication/edit?id=${row.id}`, { status: 2 });
    if (res.code === 200) {
      ElMessage.success("已拒绝");
      await getTableData();
    }
  } catch {
    /* cancel */
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

const goActivity = (activityId: number | string | null | undefined) => {
  if (activityId == null || activityId === "") return;
  window.open(`/front/activityDetail?id=${encodeURIComponent(String(activityId))}`, "_blank");
};

onMounted(() => getTableData());
</script>

<template>
  <div class="studio-app">
    <div class="studio-app-head">
      <div class="head-icon" aria-hidden="true">
        <el-icon :size="22"><Calendar /></el-icon>
      </div>
      <div class="head-text">
        <h2 class="head-title">报名审核</h2>
        <p class="head-sub">卡片视图与前台「我的报名」一致，便于核对封面与状态</p>
      </div>
      <span v-if="total > 0" class="head-count">共 {{ total }} 条</span>
    </div>

    <div class="studio-app-filter">
      <el-input v-model="params.realName" placeholder="姓名" clearable class="f-inp" @keyup.enter="handleSearch" />
      <el-input v-model="params.phone" placeholder="手机" clearable class="f-inp" @keyup.enter="handleSearch" />
      <el-select v-model="params.status" placeholder="状态" clearable class="f-sel">
        <el-option label="待审核" value="0" />
        <el-option label="已通过" value="1" />
        <el-option label="已拒绝" value="2" />
      </el-select>
      <el-button type="primary" round :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button round :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <div v-if="hasPermission('activityApplication', '删除')" class="studio-app-toolbar">
      <el-checkbox
        :model-value="isAllPageSelected"
        :indeterminate="isIndeterminate"
        @change="(v: any) => toggleSelectAllPage(!!v)"
      >
        全选本页
      </el-checkbox>
      <el-button type="danger" plain round :disabled="!hasSelected" :icon="Delete" @click="batchDel">
        批量删除
      </el-button>
    </div>

    <div v-loading="loading" class="studio-app-main">
      <el-empty v-if="isEmpty" description="暂无报名记录" />

      <div v-else class="card-grid">
        <article v-for="row in tableData" :key="row.id" class="app-card">
          <div class="card-select" @click.stop>
            <el-checkbox
              v-if="hasPermission('activityApplication', '删除')"
              :model-value="isRowSelected(row)"
              @change="(v: any) => toggleRowSelect(row, !!v)"
            />
          </div>
          <div
            class="card-media"
            role="button"
            tabindex="0"
            @click="goActivity(row.activityId)"
            @keydown.enter="goActivity(row.activityId)"
          >
            <el-image :src="coverSrc(row)" fit="cover" class="card-img" :alt="row.activityTitle">
              <template #error>
                <div class="img-fallback">活动封面</div>
              </template>
            </el-image>
            <span class="status-pill" :class="getStatusClass(row.status)">{{ statusDict[row.status] }}</span>
          </div>

          <div class="card-body">
            <h3
              class="card-title"
              role="button"
              tabindex="0"
              @click="goActivity(row.activityId)"
              @keydown.enter="goActivity(row.activityId)"
            >
              {{ row.activityTitle || "活动" }}
            </h3>

            <ul class="meta-list">
              <li>
                <el-icon><User /></el-icon>
                <span class="meta-label">姓名</span>
                <span class="meta-val">{{ row.realName || "—" }}</span>
              </li>
              <li>
                <el-icon><Phone /></el-icon>
                <span class="meta-label">电话</span>
                <span class="meta-val">{{ row.phone || "—" }}</span>
              </li>
              <li v-if="row.username" class="meta-account">
                <span class="meta-label">账号</span>
                <span class="meta-val">{{ row.username }}</span>
              </li>
            </ul>

            <p v-if="row.remark" class="remark-box">
              <span class="remark-label">备注</span>
              {{ row.remark }}
            </p>
            <p v-else class="remark-box muted">无备注</p>

            <div class="time-row">
              <span>
                <el-icon><Clock /></el-icon>
                报名 {{ row.createTime }}
              </span>
              <span v-if="row.updateTime && row.updateTime !== row.createTime">更新 {{ row.updateTime }}</span>
            </div>

            <div class="card-foot">
              <el-button type="primary" link class="link-view" @click.stop="goActivity(row.activityId)">
                <el-icon class="ic"><View /></el-icon>
                查看活动
              </el-button>
              <div class="card-actions">
                <template v-if="row.status === 0">
                  <el-button
                    v-if="hasPermission('activityApplication', '审核')"
                    type="success"
                    size="small"
                    round
                    :icon="Check"
                    @click="handleApprove(row)"
                  >
                    通过
                  </el-button>
                  <el-button
                    v-if="hasPermission('activityApplication', '审核')"
                    type="danger"
                    size="small"
                    round
                    plain
                    :icon="Close"
                    @click="handleReject(row)"
                  >
                    拒绝
                  </el-button>
                </template>
                <el-popconfirm
                  v-if="hasPermission('activityApplication', '删除')"
                  title="确定删除该报名？"
                  @confirm="delRow(row.id)"
                >
                  <template #reference>
                    <el-button type="danger" link size="small" :icon="Delete">删除</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </div>
        </article>
      </div>

      <div v-if="total > 0" class="pag-wrap">
        <el-pagination
          v-model:current-page="params.pageNum"
          v-model:page-size="params.pageSize"
          :page-sizes="[12, 24, 48]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.studio-app-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
  padding: 16px 18px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 251, 248, 0.98) 100%);
  border: 1px solid rgba(197, 61, 46, 0.12);
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(62, 18, 18, 0.06);
}

.head-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(145deg, #c53d2e 0%, #8b2924 100%);
  flex-shrink: 0;
}

.head-text {
  flex: 1;
  min-width: 200px;
}

.head-title {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 800;
  color: #2d2420;
}

.head-sub {
  margin: 0;
  font-size: 13px;
  color: #6b5c55;
  line-height: 1.5;
}

.head-count {
  font-size: 13px;
  color: #5c4a45;
  padding: 6px 14px;
  border-radius: 999px;
  background: rgba(197, 61, 46, 0.08);
  border: 1px solid rgba(197, 61, 46, 0.12);
}

.studio-app-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
}

.f-inp {
  width: 140px;
}

.f-sel {
  width: 130px;
}

.studio-app-toolbar {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 12px;
}

.studio-app-main {
  min-height: 200px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.app-card {
  position: relative;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #ffffff 0%, #fffdfb 100%);
  border-radius: 18px;
  overflow: hidden;
  border: 1px solid rgba(197, 61, 46, 0.1);
  box-shadow: 0 10px 28px rgba(45, 28, 18, 0.08);
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 18px 44px rgba(62, 18, 18, 0.12);
    border-color: rgba(197, 61, 46, 0.22);

    .card-img :deep(img) {
      transform: scale(1.04);
    }
  }
}

.card-select {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 2;
  padding: 4px 6px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.card-media {
  position: relative;
  height: 160px;
  cursor: pointer;
  background: #f0e8e4;
}

.card-img {
  width: 100%;
  height: 100%;
  display: block;

  :deep(.el-image__inner) {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.35s ease;
  }
}

.img-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #9a8a84;
  background: linear-gradient(145deg, #efe5e1 0%, #e5d9d4 100%);
}

.status-pill {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 5px 12px;
  font-size: 12px;
  font-weight: 700;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12);

  &.st-pending {
    color: #9a5b16;
    background: rgba(255, 236, 210, 0.95);
  }

  &.st-ok {
    color: #1b6b3a;
    background: rgba(214, 247, 223, 0.95);
  }

  &.st-reject {
    color: #a82c28;
    background: rgba(255, 228, 224, 0.95);
  }

  &.st-unknown {
    color: #5c4a45;
    background: rgba(255, 255, 255, 0.92);
  }
}

.card-body {
  padding: 16px 18px 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.card-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #2d2420;
  line-height: 1.35;
  cursor: pointer;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s;

  &:hover {
    color: #c53d2e;
  }
}

.meta-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;

  li {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: #5c4a45;

    .el-icon {
      color: #c53d2e;
      opacity: 0.85;
    }
  }
}

.meta-account {
  padding-left: 22px;
}

.meta-label {
  color: #8b7d78;
  min-width: 2em;
}

.meta-val {
  font-weight: 600;
  color: #3d2f2a;
}

.remark-box {
  margin: 0;
  font-size: 13px;
  line-height: 1.55;
  color: #6b5c55;
  padding: 10px 12px;
  background: rgba(197, 61, 46, 0.05);
  border-radius: 10px;
  border: 1px solid rgba(197, 61, 46, 0.08);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;

  .remark-label {
    font-weight: 700;
    color: #8b2924;
    margin-right: 6px;
  }

  &.muted {
    color: #a89892;
    font-style: italic;
    background: rgba(0, 0, 0, 0.02);
    border-style: dashed;
  }
}

.time-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #8b7d78;

  span {
    display: inline-flex;
    align-items: center;
    gap: 6px;

    .el-icon {
      font-size: 14px;
    }
  }
}

.card-foot {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid rgba(197, 61, 46, 0.08);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.link-view {
  font-weight: 600;
}

.link-view .ic {
  margin-right: 4px;
  vertical-align: middle;
}

.card-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.pag-wrap {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  padding: 14px;
  background: rgba(255, 255, 255, 0.65);
  border-radius: 14px;
  border: 1px solid rgba(197, 61, 46, 0.08);
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  background-color: #c53d2e;
}

@media (max-width: 640px) {
  .f-inp,
  .f-sel {
    width: 100%;
  }
}
</style>
