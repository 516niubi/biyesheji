<script setup lang="ts">
import http from "../../utils/http";
import { Clock, Delete, StarFilled, View } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { onMounted, ref, computed } from "vue";
import { useRouter } from "vue-router";
import useUserStore from "../../stores/userStore";
import { getImageUrl } from "../../utils/system";

const router = useRouter();
const userStore = useUserStore();

const initParams = {
  pageNum: 1,
  pageSize: 12,
};

const params = ref({ ...initParams });
const tableData = ref<any[]>([]);
const total = ref(0);
/** 首屏 true，避免未请求前误显示「空状态」闪烁 */
const loading = ref(true);

const currentUser = computed(() => {
  try {
    return typeof userStore.userInfo === "string"
      ? JSON.parse(userStore.userInfo)
      : userStore.userInfo;
  } catch {
    return {};
  };
});

const isEmpty = computed(
  () => !loading.value && total.value === 0
);

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
    const res = await http.get(
      `/collect/pageByUserId?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}&userId=${currentUser.value.id}`
    );
    if (res.code === 200) {
      tableData.value = res.data.records || [];
      total.value = res.data.total ?? 0;
    } else {
      ElMessage.error("获取收藏列表失败");
    }
  } catch {
    ElMessage.error("获取收藏列表失败");
  } finally {
    loading.value = false;
  }
};

const cancelCollect = async (id: number) => {
  try {
    const res = await http.get(`/collect/del?id=${id}`);
    if (res.code === 200) {
      ElMessage.success("已取消收藏");
      await getTableData();
    } else {
      ElMessage.error("取消收藏失败");
    }
  } catch {
    ElMessage.error("取消收藏失败");
  }
};

const goToHeritageDetail = (heritageId: number) => {
  router.push({
    path: "/front/heritageDetail",
    query: { id: heritageId },
  });
};

const coverSrc = (row: any) => getImageUrl(row.heritageCoverImage);

const handleSizeChange = (val: number) => {
  params.value.pageSize = val;
  getTableData();
};

const handleCurrentChange = (val: number) => {
  params.value.pageNum = val;
  getTableData();
};

onMounted(async () => {
  await getTableData();
});
</script>

<template>
  <div class="my-collect-page">
    <header class="collect-hero">
      <div class="collect-hero-inner">
        <div class="collect-hero-icon" aria-hidden="true">
          <el-icon :size="28"><StarFilled /></el-icon>
        </div>
        <div class="collect-hero-text">
          <h1 class="collect-title">我的收藏</h1>
          <p class="collect-sub">
            您收藏的非遗文物档案，随时回看与整理
          </p>
        </div>
        <div v-if="total > 0" class="collect-count">
          共 <strong>{{ total }}</strong> 件
        </div>
      </div>
    </header>

    <div class="collect-main" v-loading="loading">
      <el-empty
        v-if="isEmpty"
        class="collect-empty"
        description="还没有收藏任何文物"
        :image-size="120"
      >
        <p class="empty-hint">在文物详情页点击收藏，即可出现在这里</p>
        <el-button type="primary" round @click="router.push('/front/heritage')">
          去逛逛非遗文物
        </el-button>
      </el-empty>

      <template v-else>
        <div class="collect-grid">
          <article
            v-for="row in tableData"
            :key="row.id"
            class="collect-card"
          >
            <div
              class="collect-card-media"
              role="button"
              tabindex="0"
              @click="goToHeritageDetail(row.heritageId)"
              @keydown.enter="goToHeritageDetail(row.heritageId)"
            >
              <el-image
                :src="coverSrc(row)"
                fit="cover"
                class="collect-card-img"
                :alt="row.heritageName"
              >
                <template #error>
                  <div class="collect-img-fallback">暂无封面</div>
                </template>
              </el-image>
              <span
                v-if="row.heritageCategoryName"
                class="collect-category"
              >{{ row.heritageCategoryName }}</span>
            </div>
            <div class="collect-card-body">
              <h2
                class="collect-card-title"
                role="button"
                tabindex="0"
                @click="goToHeritageDetail(row.heritageId)"
                @keydown.enter="goToHeritageDetail(row.heritageId)"
              >
                {{ row.heritageName || "未命名文物" }}
              </h2>
              <p v-if="row.heritageIntro" class="collect-card-intro">
                {{ row.heritageIntro }}
              </p>
              <p v-else class="collect-card-intro muted">暂无简介</p>
              <div class="collect-card-meta">
                <el-icon class="meta-icon"><Clock /></el-icon>
                <span>收藏于 {{ row.createTime }}</span>
              </div>
              <div class="collect-card-actions">
                <el-button
                  type="primary"
                  link
                  class="action-link"
                  @click.stop="goToHeritageDetail(row.heritageId)"
                >
                  <el-icon class="btn-ic"><View /></el-icon>
                  查看详情
                </el-button>
                <el-popconfirm
                  title="确定取消收藏该文物？"
                  confirm-button-text="取消收藏"
                  cancel-button-text="保留"
                  width="220"
                  @confirm="cancelCollect(row.id)"
                >
                  <template #reference>
                    <el-button
                      type="danger"
                      link
                      class="action-unfav"
                      @click.stop
                    >
                      <el-icon class="btn-ic"><Delete /></el-icon>
                      取消收藏
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </article>
        </div>

        <div v-if="total > 0" class="collect-pagination">
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
.my-collect-page {
  min-height: calc(100vh - 120px);
  padding: 0 20px 48px;
  max-width: 1280px;
  margin: 0 auto;
  background-color: #f3ebe8;
  background-image:
    radial-gradient(ellipse 90% 50% at 50% -20%, rgba(197, 61, 46, 0.1) 0%, transparent 55%),
    linear-gradient(180deg, #fdf9f7 0%, #f5ebe8 40%, #efe5e1 100%);
}

.collect-hero {
  padding: 28px 0 8px;
}

.collect-hero-inner {
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

.collect-hero-icon {
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

.collect-hero-text {
  flex: 1;
  min-width: 200px;
}

.collect-title {
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

.collect-sub {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #6b5c55;
}

.collect-count {
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

.collect-main {
  margin-top: 20px;
  min-height: 200px;
}

.collect-empty {
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

.collect-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 22px;
}

.collect-card {
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

    .collect-card-img :deep(img) {
      transform: scale(1.04);
    }
  }
}

.collect-card-media {
  position: relative;
  height: 176px;
  cursor: pointer;
  background: #f0e8e4;
}

.collect-card-img {
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

.collect-img-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #9a8a84;
  background: linear-gradient(145deg, #efe5e1 0%, #e5d9d4 100%);
}

.collect-category {
  position: absolute;
  top: 10px;
  left: 10px;
  max-width: calc(100% - 20px);
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 600;
  color: #3d2f2a;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.collect-card-body {
  padding: 16px 18px 18px;
  display: flex;
  flex-direction: column;
  flex: 1;
  gap: 8px;
}

.collect-card-title {
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

.collect-card-intro {
  margin: 0;
  font-size: 13px;
  line-height: 1.55;
  color: #6b5c55;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;

  &.muted {
    color: #a89892;
    font-style: italic;
  }
}

.collect-card-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #8b7d78;
  margin-top: 4px;

  .meta-icon {
    font-size: 14px;
    opacity: 0.85;
  }
}

.collect-card-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding-top: 10px;
  margin-top: 4px;
  border-top: 1px solid rgba(197, 61, 46, 0.08);
}

.btn-ic {
  margin-right: 4px;
  vertical-align: middle;
}

.action-link {
  font-weight: 600;
}

.action-unfav {
  font-weight: 600;
}

.collect-pagination {
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
</style>
