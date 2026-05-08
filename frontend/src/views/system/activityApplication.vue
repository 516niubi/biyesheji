<script setup lang="ts">
import http from "../../utils/http";
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
import { useRouter } from "vue-router";
import { hasPermission, getImageUrl } from "../../utils/system";

const router = useRouter();

const initParams = {
  pageNum: 1,
  pageSize: 12,
  realName: "",
  phone: "",
  status: "" as string | number,
};

const params = ref({ ...initParams });
const tableData = ref<any[]>([]);
const total = ref(0);
const loading = ref(false);

const multipleSelection = ref<any[]>([]);
const selectedIds = computed(() => multipleSelection.value.map((item: any) => item.id));
const hasSelected = computed(() => multipleSelection.value.length > 0);

const statusDict: Record<number, string> = {
  0: "待审核",
  1: "已通过",
  2: "已拒绝",
};

const isEmpty = computed(() => !loading.value && total.value === 0);

const isRowSelected = (row: any) => selectedIds.value.includes(row.id);

const toggleRowSelect = (row: any, checked: boolean) => {
  if (checked) {
    if (!isRowSelected(row)) multipleSelection.value = [...multipleSelection.value, row];
  } else {
    multipleSelection.value = multipleSelection.value.filter((x: any) => x.id !== row.id);
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
  switch (status) {
    case 0:
      return "st-pending";
    case 1:
      return "st-ok";
    case 2:
      return "st-reject";
    default:
      return "st-unknown";
  }
};

const coverSrc = (row: any) => getImageUrl(row.activityCoverImage);

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

const handleSearch = async () => {
  params.value.pageNum = 1;
  await getTableData();
};

const handleReset = () => {
  params.value = { ...initParams };
  multipleSelection.value = [];
  getTableData();
};

const delRow = async (id: number) => {
  const res = await http.get(`/activityApplication/del?id=${id}`);
  if (res.code === 200) {
    ElMessage.success("删除成功");
    multipleSelection.value = multipleSelection.value.filter((x: any) => x.id !== id);
    await getTableData();
  } else {
    ElMessage.error("删除失败");
  }
};

const batchDel = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning("请先选择要删除的报名记录");
    return;
  }
  const res = await http.post("/activityApplication/batchDel", selectedIds.value);
  if (res.code === 200) {
    ElMessage.success("批量删除成功");
    multipleSelection.value = [];
    await getTableData();
  } else {
    ElMessage.error("批量删除失败");
  }
};

const handleApprove = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要通过 ${row.realName} 的报名申请吗？`, "审核确认", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
    const res = await http.post(`/activityApplication/edit?id=${row.id}`, { status: 1 });
    if (res.code === 200) {
      ElMessage.success("审核通过成功");
      await getTableData();
    } else {
      ElMessage.error(res.msg || "审核通过失败");
    }
  } catch {
    /* cancel */
  }
};

const handleReject = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要拒绝 ${row.realName} 的报名申请吗？`, "审核确认", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
    const res = await http.post(`/activityApplication/edit?id=${row.id}`, { status: 2 });
    if (res.code === 200) {
      ElMessage.success("审核拒绝成功");
      await getTableData();
    } else {
      ElMessage.error(res.msg || "审核拒绝失败");
    }
  } catch {
    /* cancel */
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

const goActivity = (activityId: number | string | null | undefined) => {
  if (activityId == null || activityId === "") return;
  router.push({ path: "/front/activityDetail", query: { id: String(activityId) } });
};

onMounted(() => getTableData());
</script>

<template>
  <div class="aa-page">
    <header class="aa-hero">
      <div class="aa-hero-inner">
        <div class="aa-hero-icon" aria-hidden="true">
          <el-icon :size="28"><Calendar /></el-icon>
        </div>
        <div class="aa-hero-text">
          <h1 class="aa-title">活动报名管理</h1>
          <p class="aa-sub">审核与维护用户活动报名记录</p>
        </div>
        <div v-if="total > 0" class="aa-count">
          共 <strong>{{ total }}</strong> 条报名
        </div>
      </div>
    </header>

    <div class="aa-filter">
      <div class="aa-filter-row">
        <el-input v-model="params.realName" placeholder="姓名" clearable class="aa-inp" @keyup.enter="handleSearch" />
        <el-input v-model="params.phone" placeholder="手机号" clearable class="aa-inp" @keyup.enter="handleSearch" />
        <el-select v-model="params.status" placeholder="状态" clearable class="aa-sel">
          <el-option label="待审核" value="0" />
          <el-option label="已通过" value="1" />
          <el-option label="已拒绝" value="2" />
        </el-select>
        <el-button type="primary" round :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button round :icon="Refresh" @click="handleReset">重置</el-button>
      </div>
    </div>

    <div v-if="hasPermission('activityApplication', '删除')" class="aa-toolbar">
      <el-checkbox
        :model-value="isAllPageSelected"
        :indeterminate="isIndeterminate"
        @change="(v: any) => toggleSelectAllPage(!!v)"
      >
        全选本页
      </el-checkbox>
      <el-button type="danger" plain :icon="Delete" :disabled="!hasSelected" @click="batchDel">
        批量删除
      </el-button>
    </div>

    <div class="aa-main" v-loading="loading">
      <el-empty v-if="isEmpty" class="aa-empty" description="暂无报名记录" :image-size="120" />

      <template v-else>
        <div class="aa-grid">
          <article v-for="row in tableData" :key="row.id" class="aa-card">
            <div class="aa-card-select" @click.stop>
              <el-checkbox
                v-if="hasPermission('activityApplication', '删除')"
                :model-value="isRowSelected(row)"
                @change="(v: any) => toggleRowSelect(row, !!v)"
              />
            </div>
            <div
              class="aa-card-media"
              role="button"
              tabindex="0"
              @click="goActivity(row.activityId)"
              @keydown.enter="goActivity(row.activityId)"
            >
              <el-image :src="coverSrc(row)" fit="cover" class="aa-card-img" :alt="row.activityTitle">
                <template #error>
                  <div class="aa-img-fallback">活动封面</div>
                </template>
              </el-image>
              <span class="status-pill" :class="getStatusClass(row.status)">
                {{ statusDict[row.status] ?? "未知" }}
              </span>
            </div>

            <div class="aa-card-body">
              <h2
                class="aa-card-title"
                role="button"
                tabindex="0"
                @click="goActivity(row.activityId)"
                @keydown.enter="goActivity(row.activityId)"
              >
                {{ row.activityTitle || "活动" }}
              </h2>

              <ul class="aa-meta">
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
                <li v-if="row.username" class="aa-meta-user">
                  <span class="meta-label">账号</span>
                  <span class="meta-val">{{ row.username }}</span>
                </li>
              </ul>

              <p v-if="row.remark" class="aa-remark">
                <span class="remark-label">备注</span>
                {{ row.remark }}
              </p>
              <p v-else class="aa-remark muted">无备注</p>

              <div class="aa-times">
                <span>
                  <el-icon><Clock /></el-icon>
                  报名 {{ row.createTime }}
                </span>
                <span v-if="row.updateTime && row.updateTime !== row.createTime"> 更新 {{ row.updateTime }} </span>
              </div>

              <div class="aa-card-foot">
                <el-button type="primary" link class="foot-link" @click.stop="goActivity(row.activityId)">
                  <el-icon class="btn-ic"><View /></el-icon>
                  查看活动
                </el-button>
                <div class="aa-actions">
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
                    title="确定删除该报名记录？"
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

        <div v-if="total > 0" class="aa-pagination">
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
      </template>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.aa-page {
  min-height: calc(100vh - 100px);
  padding: 0 16px 40px;
  max-width: 1280px;
  margin: 0 auto;
  background-color: #f3ebe8;
  background-image:
    radial-gradient(ellipse 90% 50% at 50% -20%, rgba(197, 61, 46, 0.1) 0%, transparent 55%),
    linear-gradient(180deg, #fdf9f7 0%, #f5ebe8 40%, #efe5e1 100%);
}

.aa-hero {
  padding: 20px 0 10px;
}

.aa-hero-inner {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px 24px;
  padding: 20px 24px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 251, 248, 0.98) 100%);
  border: 1px solid rgba(197, 61, 46, 0.12);
  border-radius: 20px;
  box-shadow:
    0 14px 40px rgba(62, 18, 18, 0.08),
    0 1px 0 rgba(255, 255, 255, 0.9) inset;
}

.aa-hero-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(145deg, #c53d2e 0%, #8b2924 100%);
  box-shadow: 0 8px 20px rgba(197, 61, 46, 0.35);
  flex-shrink: 0;
}

.aa-hero-text {
  flex: 1;
  min-width: 200px;
}

.aa-title {
  margin: 0 0 6px;
  font-size: clamp(20px, 2.5vw, 26px);
  font-weight: 800;
  letter-spacing: 0.04em;
  background: linear-gradient(135deg, #c53d2e 0%, #4a1818 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
}

.aa-sub {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #6b5c55;
}

.aa-count {
  font-size: 14px;
  color: #5c4a45;
  padding: 8px 16px;
  border-radius: 999px;
  background: rgba(197, 61, 46, 0.08);
  border: 1px solid rgba(197, 61, 46, 0.15);
  font-weight: 500;

  strong {
    color: #c53d2e;
    font-weight: 800;
    margin: 0 2px;
  }
}

.aa-filter {
  padding: 14px 16px;
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(197, 61, 46, 0.1);
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(62, 18, 18, 0.06);
}

.aa-filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.aa-inp {
  width: 160px;
  min-width: 120px;
  flex: 1;
  max-width: 220px;

  :deep(.el-input__wrapper) {
    border-radius: 10px;
    box-shadow: 0 0 0 1px rgba(197, 61, 46, 0.12) inset;
  }
}

.aa-sel {
  width: 140px;
}

.aa-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  padding: 8px 4px 12px;
}

.aa-main {
  margin-top: 8px;
  min-height: 200px;
}

.aa-empty {
  padding: 48px 24px;
  background: rgba(255, 255, 255, 0.75);
  border-radius: 20px;
  border: 1px dashed rgba(197, 61, 46, 0.2);
}

.aa-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 22px;
}

.aa-card {
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

    .aa-card-img :deep(img) {
      transform: scale(1.04);
    }
  }
}

.aa-card-select {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 2;
  padding: 4px 6px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.aa-card-media {
  position: relative;
  height: 160px;
  cursor: pointer;
  background: #f0e8e4;
}

.aa-card-img {
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

.aa-img-fallback {
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

.aa-card-body {
  padding: 16px 18px 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.aa-card-title {
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

.aa-meta {
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

.aa-meta-user {
  padding-left: 22px;

  .meta-label {
    min-width: 2em;
  }
}

.meta-label {
  color: #8b7d78;
  min-width: 2em;
}

.meta-val {
  font-weight: 600;
  color: #3d2f2a;
}

.aa-remark {
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

.aa-times {
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
      opacity: 0.85;
    }
  }
}

.aa-card-foot {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid rgba(197, 61, 46, 0.08);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.foot-link {
  font-weight: 600;
}

.btn-ic {
  margin-right: 4px;
  vertical-align: middle;
}

.aa-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.aa-pagination {
  margin-top: 28px;
  display: flex;
  justify-content: center;
  padding: 16px;
  background: rgba(255, 255, 255, 0.65);
  border-radius: 16px;
  border: 1px solid rgba(197, 61, 46, 0.08);
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  background-color: #c53d2e;
}

:deep(.el-pagination.is-background .btn-next),
:deep(.el-pagination.is-background .btn-prev) {
  background-color: #fff;
}

@media (max-width: 768px) {
  .aa-page {
    padding: 0 12px 32px;
  }

  .aa-filter-row {
    flex-direction: column;
    align-items: stretch;

    .aa-inp,
    .aa-sel {
      max-width: none;
      width: 100%;
    }
  }
}
</style>
