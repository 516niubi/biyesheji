<script setup lang="ts">
import http from "../../utils/http";
import {
  Calendar,
  Clock,
  Phone,
  Refresh,
  Search,
  User,
  View,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import useUserStore from "../../stores/userStore";
import { getImageUrl } from "../../utils/system";

const router = useRouter();
const userStore = useUserStore();

const initParams = {
  pageNum: 1,
  pageSize: 12,
  realName: "",
};

const params = ref({ ...initParams });
const tableData = ref<any[]>([]);
const total = ref(0);
const loading = ref(true);

const statusDict: Record<number, string> = {
  0: "待审核",
  1: "已通过",
  2: "已拒绝",
};

const currentUser = computed(() => {
  const raw = userStore.userInfo as any;
  if (raw == null) return {};
  if (typeof raw === "string") {
    try {
      return JSON.parse(raw);
    } catch {
      return {};
    }
  }
  return raw;
});

const isEmpty = computed(() => !loading.value && total.value === 0);

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
  if (!currentUser.value?.id) {
    ElMessage.error("请先登录");
    loading.value = false;
    tableData.value = [];
    total.value = 0;
    return;
  }

  loading.value = true;
  try {
    const q = encodeURIComponent(params.value.realName || "");
    const res = await http.get(
      `/activityApplication/pageByUserId?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}&userId=${currentUser.value.id}&realName=${q}`
    );
    if (res.code === 200) {
      tableData.value = res.data.records || [];
      total.value = res.data.total ?? 0;
    } else {
      ElMessage.error(res.msg || "获取数据失败");
    }
  } catch (e) {
    console.error("获取报名数据失败:", e);
    ElMessage.error("获取数据失败");
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
  getTableData();
};

const handleSizeChange = (val: number) => {
  params.value.pageSize = val;
  getTableData();
};

const handleCurrentChange = (val: number) => {
  params.value.pageNum = val;
  getTableData();
};

const goActivity = (activityId: number | string) => {
  if (activityId == null || activityId === "") return;
  router.push({
    path: "/front/activityDetail",
    query: { id: String(activityId) },
  });
};

onMounted(async () => {
  await getTableData();
});
</script>

<template>
  <div class="my-app-page">
    <header class="app-hero">
      <div class="app-hero-inner">
        <div class="app-hero-icon" aria-hidden="true">
          <el-icon :size="28"><Calendar /></el-icon>
        </div>
        <div class="app-hero-text">
          <h1 class="app-title">我的报名</h1>
          <p class="app-sub">查看活动报名记录与审核进度</p>
        </div>
        <div v-if="total > 0" class="app-count">
          共 <strong>{{ total }}</strong> 条
        </div>
      </div>
    </header>

    <div class="app-filter">
      <el-input
        v-model="params.realName"
        placeholder="按真实姓名筛选"
        clearable
        class="filter-input"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon class="filter-ic"><User /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" round :icon="Search" @click="handleSearch">
        搜索
      </el-button>
      <el-button round :icon="Refresh" @click="handleReset"> 重置 </el-button>
    </div>

    <div class="app-main" v-loading="loading">
      <el-empty
        v-if="isEmpty"
        class="app-empty"
        description="暂无报名记录"
        :image-size="120"
      >
        <p class="empty-hint">发现感兴趣的活动，前往详情页即可报名</p>
        <el-button type="primary" round @click="router.push('/front/activity')">
          浏览活动中心
        </el-button>
      </el-empty>

      <template v-else>
        <div class="app-grid">
          <article
            v-for="row in tableData"
            :key="row.id"
            class="app-card"
          >
            <div
              class="app-card-media"
              role="button"
              tabindex="0"
              @click="goActivity(row.activityId)"
              @keydown.enter="goActivity(row.activityId)"
            >
              <el-image
                :src="coverSrc(row)"
                fit="cover"
                class="app-card-img"
                :alt="row.activityTitle"
              >
                <template #error>
                  <div class="app-img-fallback">活动封面</div>
                </template>
              </el-image>
              <span
                class="status-pill"
                :class="getStatusClass(row.status)"
              >{{ statusDict[row.status] ?? "未知" }}</span>
            </div>

            <div class="app-card-body">
              <h2
                class="app-card-title"
                role="button"
                tabindex="0"
                @click="goActivity(row.activityId)"
                @keydown.enter="goActivity(row.activityId)"
              >
                {{ row.activityTitle || "活动" }}
              </h2>

              <ul class="app-meta">
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
              </ul>

              <p v-if="row.remark" class="app-remark">
                <span class="remark-label">备注</span>
                {{ row.remark }}
              </p>
              <p v-else class="app-remark muted">无备注</p>

              <div class="app-times">
                <span>
                  <el-icon><Clock /></el-icon>
                  报名 {{ row.createTime }}
                </span>
                <span v-if="row.updateTime && row.updateTime !== row.createTime">
                  更新 {{ row.updateTime }}
                </span>
              </div>

              <div class="app-card-foot">
                <el-button
                  type="primary"
                  link
                  class="foot-link"
                  @click.stop="goActivity(row.activityId)"
                >
                  <el-icon class="btn-ic"><View /></el-icon>
                  查看活动
                </el-button>
              </div>
            </div>
          </article>
        </div>

        <div v-if="total > 0" class="app-pagination">
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
.my-app-page {
  min-height: calc(100vh - 120px);
  padding: 0 20px 48px;
  max-width: 1280px;
  margin: 0 auto;
  background-color: #f3ebe8;
  background-image:
    radial-gradient(ellipse 90% 50% at 50% -20%, rgba(197, 61, 46, 0.1) 0%, transparent 55%),
    linear-gradient(180deg, #fdf9f7 0%, #f5ebe8 40%, #efe5e1 100%);
}

.app-hero {
  padding: 28px 0 12px;
}

.app-hero-inner {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px 24px;
  padding: 22px 26px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 251, 248, 0.98) 100%);
  border: 1px solid rgba(197, 61, 46, 0.12);
  border-radius: 20px;
  box-shadow:
    0 14px 40px rgba(62, 18, 18, 0.08),
    0 1px 0 rgba(255, 255, 255, 0.9) inset;
}

.app-hero-icon {
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

.app-hero-text {
  flex: 1;
  min-width: 200px;
}

.app-title {
  margin: 0 0 6px;
  font-size: clamp(22px, 3vw, 28px);
  font-weight: 800;
  letter-spacing: 0.04em;
  background: linear-gradient(135deg, #c53d2e 0%, #4a1818 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
}

.app-sub {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #6b5c55;
}

.app-count {
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

.app-filter {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  padding: 16px 18px;
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(197, 61, 46, 0.1);
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(62, 18, 18, 0.06);
}

.filter-input {
  flex: 1;
  min-width: 200px;
  max-width: 360px;

  :deep(.el-input__wrapper) {
    border-radius: 999px;
    box-shadow: 0 0 0 1px rgba(197, 61, 46, 0.12) inset;
  }
}

.filter-ic {
  color: #a89892;
}

.app-main {
  margin-top: 16px;
  min-height: 200px;
}

.app-empty {
  padding: 48px 24px;
  background: rgba(255, 255, 255, 0.75);
  border-radius: 20px;
  border: 1px dashed rgba(197, 61, 46, 0.2);
}

.empty-hint {
  margin: 0 0 16px;
  font-size: 14px;
  color: #8b7d78;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 22px;
}

.app-card {
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

    .app-card-img :deep(img) {
      transform: scale(1.04);
    }
  }
}

.app-card-media {
  position: relative;
  height: 160px;
  cursor: pointer;
  background: #f0e8e4;
}

.app-card-img {
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

.app-img-fallback {
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

.app-card-body {
  padding: 16px 18px 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.app-card-title {
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

.app-meta {
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

.meta-label {
  color: #8b7d78;
  min-width: 2em;
}

.meta-val {
  font-weight: 600;
  color: #3d2f2a;
}

.app-remark {
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

.app-times {
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

.app-card-foot {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid rgba(197, 61, 46, 0.08);
}

.foot-link {
  font-weight: 600;
}

.btn-ic {
  margin-right: 4px;
  vertical-align: middle;
}

.app-pagination {
  margin-top: 32px;
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
  .my-app-page {
    padding: 0 12px 32px;
  }

  .app-filter {
    flex-direction: column;
    align-items: stretch;

    .filter-input {
      max-width: none;
    }
  }
}
</style>
