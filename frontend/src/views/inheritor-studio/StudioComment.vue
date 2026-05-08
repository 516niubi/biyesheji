<script setup lang="ts">
import http from "@/utils/http";
import { Search, Refresh, Delete, Link } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { hasPermission } from "@/utils/system";
import CommentThread from "@/components/CommentThread.vue";

const router = useRouter();

const initParams = { pageNum: 1, pageSize: 12, userName: "" };
const params = ref({ ...initParams });
const tableData = ref<any[]>([]);
const total = ref(0);
const loading = ref(true);
const multipleSelection = ref<any[]>([]);
const selectedIds = computed(() => multipleSelection.value.map((i) => i.id));
const hasSelected = computed(() => multipleSelection.value.length > 0);

const getTableData = async () => {
  loading.value = true;
  try {
    let url = `/comment/manage/selectRootTreePage?pageNum=${params.value.pageNum}&pageSize=${params.value.pageSize}`;
    if (params.value.userName) url += `&userName=${encodeURIComponent(params.value.userName)}`;
    const res = await http.get(url);
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
  getTableData();
};

const submitStudioReply = async (
  heritageId: number,
  payload: { parentId: number; content: string; images: string[] }
) => {
  if (heritageId == null) {
    ElMessage.error("缺少文物信息");
    return;
  }
  const text = payload.content?.trim() || "";
  if (!text && !payload.images?.length) {
    ElMessage.warning("请输入回复内容或上传图片");
    return;
  }
  try {
    const res = await http.post("/comment/add", {
      heritageId,
      parentId: payload.parentId,
      content: text,
      images: payload.images || [],
    });
    if (res.code === 200) {
      ElMessage.success("回复成功");
      await getTableData();
    } else {
      ElMessage.error(res.msg || "回复失败");
    }
  } catch {
    ElMessage.error("回复失败");
  }
};

const delRow = async (id: number) => {
  const res = await http.delete(`/comment/delete/${id}`);
  if (res.code === 200) {
    ElMessage.success("已删除（含其下回复）");
    await getTableData();
  }
};

/** 删除任意一层单条（含其下子回复）；与「删除整楼」共用后端接口 */
const deleteOneComment = async (id: number) => {
  try {
    const res = await http.delete(`/comment/delete/${id}`);
    if (res.code === 200) {
      ElMessage.success("已删除该条及其下回复");
      await getTableData();
    } else {
      ElMessage.error(res.msg || "删除失败");
    }
  } catch {
    ElMessage.error("删除失败");
  }
};

const batchDel = async () => {
  if (!multipleSelection.value.length) return ElMessage.warning("请先选择主楼评论");
  const res = await http.delete("/comment/delete/batch", { data: selectedIds.value });
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

const handleSizeChange = (v: number) => {
  params.value.pageSize = v;
  getTableData();
};

const handleCurrentChange = (v: number) => {
  params.value.pageNum = v;
  getTableData();
};

const openHeritage = (id: number) => {
  if (id == null) return;
  router.push({ path: "/front/heritageDetail", query: { id: String(id) } });
};

onMounted(() => getTableData());
</script>

<template>
  <div class="studio-page">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="params.userName"
          placeholder="按访客昵称筛选主楼"
          clearable
          class="w280"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" round :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button round :icon="Refresh" @click="handleReset">重置</el-button>
      </div>
      <el-button
        v-if="hasPermission('comment', '删除')"
        round
        type="danger"
        plain
        :disabled="!hasSelected"
        :icon="Delete"
        @click="batchDel"
      >
        批量删除主楼
      </el-button>
    </div>

    <p class="hint">
      共 <strong>{{ total }}</strong> 条主楼评论 · 仅展示您发布文物下的访客留言；每条评论（含回复）均可单独删除或回复；删除任一条会同时删除其下的所有子回复
    </p>

    <div v-loading="loading" class="main-area">
      <div v-if="!loading && !tableData.length" class="empty">
        <el-empty description="暂无评论" />
      </div>
      <div v-else class="thread-list">
        <section v-for="row in tableData" :key="row.id" class="thread-card">
          <header class="thread-top">
            <el-checkbox
              v-if="hasPermission('comment', '删除')"
              :model-value="isSelected(row)"
              @change="(v: boolean) => toggleSelect(row, v)"
            />
            <div class="heritage-block">
              <span class="heritage-label">所属文物</span>
              <button type="button" class="heritage-name" @click="openHeritage(row.heritageId)">
                {{ row.heritageName || "未命名文物" }}
              </button>
              <el-button type="primary" link :icon="Link" @click="openHeritage(row.heritageId)">
                打开详情页
              </el-button>
            </div>
            <div class="thread-actions">
              <el-popconfirm title="删除该主楼及全部回复？" @confirm="delRow(row.id)">
                <template #reference>
                  <el-button
                    v-if="hasPermission('comment', '删除')"
                    type="danger"
                    link
                    :icon="Delete"
                  >
                    删除整楼
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </header>

          <div class="thread-body">
            <CommentThread
              :comment="row"
              :depth="0"
              :can-reply="hasPermission('comment', '编辑')"
              :can-delete="hasPermission('comment', '删除')"
              @submit-reply="(p) => submitStudioReply(row.heritageId, p)"
              @delete-comment="deleteOneComment"
            />
          </div>
        </section>
      </div>
    </div>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="params.pageNum"
        v-model:page-size="params.pageSize"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
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
    align-items: center;
  }
  .toolbar-left {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    align-items: center;
  }
  .w280 {
    width: min(300px, 100%);
  }
  .hint {
    font-size: 13px;
    color: #7a6a62;
    margin: 0 0 14px;
    line-height: 1.55;

    strong {
      color: #a82c28;
      font-weight: 700;
    }
  }
  .main-area {
    min-height: 200px;
  }
  .empty {
    padding: 36px 0;
  }
  .thread-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
  .thread-card {
    border-radius: 16px;
    overflow: hidden;
    background: linear-gradient(145deg, #fff 0%, #fffbf8 100%);
    border: 1px solid rgba(92, 38, 36, 0.12);
    box-shadow: 0 10px 28px rgba(45, 28, 18, 0.06);
  }
  .thread-top {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 10px 16px;
    padding: 12px 16px;
    background: rgba(197, 61, 46, 0.06);
    border-bottom: 1px solid rgba(92, 38, 36, 0.08);
  }
  .heritage-block {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px 12px;
    flex: 1;
    min-width: 0;
  }
  .heritage-label {
    font-size: 12px;
    font-weight: 600;
    color: #8b7d78;
    letter-spacing: 0.04em;
  }
  .heritage-name {
    border: none;
    background: none;
    padding: 0;
    font-size: 15px;
    font-weight: 700;
    color: #c53d2e;
    cursor: pointer;
    text-align: left;
    text-decoration: underline;
    text-underline-offset: 3px;

    &:hover {
      color: #8b2924;
    }
  }
  .thread-actions {
    margin-left: auto;
  }
  .thread-body {
    padding: 14px 16px 18px;
  }
  .pagination-container {
    margin-top: 24px;
    display: flex;
    justify-content: center;
  }
}
</style>
