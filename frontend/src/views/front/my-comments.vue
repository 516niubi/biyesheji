<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import http from "@/utils/http";
import { getImageUrl } from "@/utils/system";
import { getStoredToken } from "@/utils/authToken";
import { ElMessage, ElMessageBox } from "element-plus";
import { User as UserIcon } from "@element-plus/icons-vue";

const router = useRouter();

const normalizeReply = (row: any) => {
  if (!row) return row;
  const inhId = row.inheritorId ?? row.inheritor_id;
  return {
    ...row,
    inheritorId: inhId != null && Number(inhId) > 0 ? Number(inhId) : null,
    avatar: row.avatar ?? row.avatarUrl,
    userName: row.userName ?? row.user_name,
    heritageId: row.heritageId ?? row.heritage_id,
    heritageName: row.heritageName ?? row.heritage_name,
    fromInheritor: row.fromInheritor ?? row.from_inheritor,
    imageUrls: row.imageUrls ?? row.image_urls ?? [],
    createTime: row.createTime ?? row.create_time,
    content: row.content,
  };
};

const replyAvatarSrc = (row: any) => {
  const a = row?.avatar;
  if (!a) return "";
  return getImageUrl(a);
};

/** 兼容后端/序列化字段名；有效 parentId 才视为回复 */
const normalizePost = (row: any) => {
  if (!row) return row;
  const rawParent = row.parentId ?? row.parent_id;
  const parentNum = rawParent != null && rawParent !== "" ? Number(rawParent) : NaN;
  const parentId = Number.isFinite(parentNum) && parentNum > 0 ? parentNum : null;
  const inhId = row.inheritorId ?? row.inheritor_id;
  return {
    ...row,
    parentId,
    inheritorId: inhId != null && Number(inhId) > 0 ? Number(inhId) : null,
    avatar: row.avatar ?? row.avatarUrl,
    userName: row.userName ?? row.user_name,
    replyToName: row.replyToName ?? row.reply_to_name,
    heritageId: row.heritageId ?? row.heritage_id,
    heritageName: row.heritageName ?? row.heritage_name,
    fromInheritor: row.fromInheritor ?? row.from_inheritor,
    imageUrls: row.imageUrls ?? row.image_urls ?? [],
    createTime: row.createTime ?? row.create_time,
  };
};

const isReplyPost = (row: any) => row?.parentId != null;

/** 按文物聚合当前页数据，避免同一文物重复大段标题 */
const groupedPosts = computed(() => {
  const map = new Map<number, { heritageId: number; heritageName: string; items: any[] }>();
  for (const raw of posts.value) {
    const row = normalizePost(raw);
    const hid = row.heritageId;
    if (hid == null) continue;
    if (!map.has(hid)) {
      map.set(hid, { heritageId: hid, heritageName: row.heritageName || "", items: [] });
    }
    map.get(hid)!.items.push(row);
  }
  const groups = Array.from(map.values());
  for (const g of groups) {
    g.items.sort((a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime());
  }
  groups.sort((a, b) => {
    const ta = Math.max(...a.items.map((i) => new Date(i.createTime).getTime()), 0);
    const tb = Math.max(...b.items.map((i) => new Date(i.createTime).getTime()), 0);
    return tb - ta;
  });
  return groups;
});
const activeTab = ref<"posts" | "replies">("posts");

const loadingPosts = ref(false);
const posts = ref<any[]>([]);
const totalPosts = ref(0);
const pagePosts = ref(1);
const sizePosts = ref(10);

const loadingReplies = ref(false);
const replies = ref<any[]>([]);
const totalReplies = ref(0);
const pageReplies = ref(1);
const sizeReplies = ref(10);

const requireAuth = () => {
  if (!getStoredToken()) {
    ElMessage.warning("请先登录");
    router.push("/login");
    return false;
  }
  return true;
};

const loadPosts = async () => {
  if (!requireAuth()) return;
  loadingPosts.value = true;
  try {
    const res = await http.get(
      `/comment/my/posts?pageNum=${pagePosts.value}&pageSize=${sizePosts.value}`
    );
    if (res.code === 200) {
      const page = res.data || {};
      const list = page.records || page.list || [];
      posts.value = list.map(normalizePost);
      totalPosts.value = page.total ?? 0;
    }
  } finally {
    loadingPosts.value = false;
  }
};

const loadReplies = async () => {
  if (!requireAuth()) return;
  loadingReplies.value = true;
  try {
    const res = await http.get(
      `/comment/my/replies?pageNum=${pageReplies.value}&pageSize=${sizeReplies.value}`
    );
    if (res.code === 200) {
      const page = res.data || {};
      const list = page.records || page.list || [];
      replies.value = list.map(normalizeReply);
      totalReplies.value = page.total ?? 0;
    }
  } finally {
    loadingReplies.value = false;
  }
};

const goHeritage = (hid: number) => {
  router.push({ path: "/front/heritageDetail", query: { id: String(hid) } });
};

const formatTime = (t: string) => {
  if (!t) return "";
  return new Date(t).toLocaleString("zh-CN");
};

const deleteMine = async (row: any) => {
  try {
    await ElMessageBox.confirm("删除后，该条下的所有回复也会一并删除，确定吗？", "删除评论", {
      type: "warning",
      confirmButtonText: "删除",
      cancelButtonText: "取消",
    });
  } catch {
    return;
  }
  const res = await http.delete(`/comment/my/${row.id}`);
  if (res.code === 200) {
    ElMessage.success("已删除");
    await loadPosts();
  }
};

const onTabChange = (name: string) => {
  if (name === "posts" && !posts.value.length && totalPosts.value === 0) loadPosts();
  if (name === "replies" && !replies.value.length && totalReplies.value === 0) loadReplies();
};

onMounted(() => {
  loadPosts();
  loadReplies();
});
</script>

<template>
  <div class="my-comments-page">
    <div class="page-head">
      <h1>我的评论</h1>
      <p class="sub">查看您发表的留言、收到的回复；可删除自己发布的内容（含其下回复）。</p>
    </div>

    <el-tabs v-model="activeTab" class="tabs" @tab-change="onTabChange">
      <el-tab-pane label="我发表的" name="posts">
        <div v-loading="loadingPosts" class="pane">
          <el-empty v-if="!loadingPosts && !posts.length" description="暂无评论" />
          <section v-for="group in groupedPosts" :key="group.heritageId" class="heritage-group">
            <header class="group-head">
              <div class="group-head-main">
                <h2 class="group-title">{{ group.heritageName || "文物" }}</h2>
                <span class="group-count">我在本文物下共 {{ group.items.length }} 条</span>
              </div>
              <el-button type="primary" link @click="goHeritage(group.heritageId)">进入文物页</el-button>
            </header>
            <article v-for="row in group.items" :key="row.id" class="card reply-card">
              <div class="reply-card-inner">
                <div class="reply-avatar-wrap">
                  <router-link
                    v-if="row.fromInheritor && row.inheritorId"
                    :to="{ path: `/front/inheritor/${row.inheritorId}` }"
                    class="reply-avatar-link"
                    title="查看传承人主页"
                  >
                    <el-avatar
                      class="reply-avatar"
                      :size="52"
                      :src="replyAvatarSrc(row)"
                      :icon="UserIcon"
                    />
                  </router-link>
                  <el-avatar
                    v-else
                    class="reply-avatar"
                    :size="52"
                    :src="replyAvatarSrc(row)"
                    :icon="UserIcon"
                  />
                </div>
                <div class="reply-main">
                  <div class="reply-head">
                    <div class="reply-identity">
                      <span class="who">{{ row.userName || "我" }}</span>
                      <el-tag v-if="isReplyPost(row)" type="info" size="small" effect="plain" round>
                        回复 {{ row.replyToName || "用户" }}
                      </el-tag>
                      <el-tag v-else type="success" size="small" effect="plain" round>主贴留言</el-tag>
                      <el-tag v-if="row.fromInheritor" type="danger" size="small" effect="plain" round>
                        传承人身份
                      </el-tag>
                    </div>
                    <span class="when">{{ formatTime(row.createTime) }}</span>
                  </div>
                  <div class="heritage-line reply-heritage">
                    <span class="lbl">文物</span>
                    <el-button link type="primary" @click="goHeritage(row.heritageId)">
                      {{ group.heritageName || row.heritageName || "查看" }}
                    </el-button>
                  </div>
                  <p v-if="isReplyPost(row)" class="hint-reply">回复了该文物下的讨论</p>
                  <p v-else class="hint-reply">发表在该文物下</p>
                  <p v-if="row.content" class="body reply-body">{{ row.content }}</p>
                  <div v-if="row.imageUrls?.length" class="imgs">
                    <el-image
                      v-for="(u, i) in row.imageUrls"
                      :key="i"
                      :src="getImageUrl(u)"
                      :preview-src-list="row.imageUrls.map((x: string) => getImageUrl(x))"
                      :initial-index="i"
                      fit="cover"
                      class="img"
                    />
                  </div>
                  <div class="card-actions reply-actions">
                    <el-button type="primary" link size="small" @click="goHeritage(row.heritageId)">
                      在文物页查看上下文
                    </el-button>
                    <el-button type="danger" link size="small" @click="deleteMine(row)">删除</el-button>
                  </div>
                </div>
              </div>
            </article>
          </section>
          <el-pagination
            v-if="totalPosts > sizePosts"
            v-model:current-page="pagePosts"
            v-model:page-size="sizePosts"
            class="pager"
            layout="total, prev, pager, next"
            :total="totalPosts"
            @current-change="loadPosts"
            @size-change="loadPosts"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="收到的回复" name="replies">
        <div v-loading="loadingReplies" class="pane">
          <el-empty v-if="!loadingReplies && !replies.length" description="暂无回复" />
          <article v-for="row in replies" :key="row.id" class="card reply-card">
            <div class="reply-card-inner">
              <div class="reply-avatar-wrap">
                <router-link
                  v-if="row.fromInheritor && row.inheritorId"
                  :to="{ path: `/front/inheritor/${row.inheritorId}` }"
                  class="reply-avatar-link"
                  title="查看传承人主页"
                >
                  <el-avatar
                    class="reply-avatar"
                    :size="52"
                    :src="replyAvatarSrc(row)"
                    :icon="UserIcon"
                  />
                </router-link>
                <el-avatar
                  v-else
                  class="reply-avatar"
                  :size="52"
                  :src="replyAvatarSrc(row)"
                  :icon="UserIcon"
                />
              </div>
              <div class="reply-main">
                <div class="reply-head">
                  <div class="reply-identity">
                    <span class="who">{{ row.userName || "用户" }}</span>
                    <el-tag v-if="row.fromInheritor" type="danger" size="small" effect="plain" round>
                      传承人
                    </el-tag>
                  </div>
                  <span class="when">{{ formatTime(row.createTime) }}</span>
                </div>
                <div class="heritage-line reply-heritage">
                  <span class="lbl">文物</span>
                  <el-button link type="primary" @click="goHeritage(row.heritageId)">
                    {{ row.heritageName || "查看" }}
                  </el-button>
                </div>
                <p class="hint-reply">回复了您的评论</p>
                <p v-if="row.content" class="body reply-body">{{ row.content }}</p>
                <div v-if="row.imageUrls?.length" class="imgs">
                  <el-image
                    v-for="(u, i) in row.imageUrls"
                    :key="i"
                    :src="getImageUrl(u)"
                    :preview-src-list="row.imageUrls.map((x: string) => getImageUrl(x))"
                    :initial-index="i"
                    fit="cover"
                    class="img"
                  />
                </div>
                <div class="card-actions reply-actions">
                  <el-button type="primary" link size="small" @click="goHeritage(row.heritageId)">
                    去评论区查看
                  </el-button>
                </div>
              </div>
            </div>
          </article>
          <el-pagination
            v-if="totalReplies > sizeReplies"
            v-model:current-page="pageReplies"
            v-model:page-size="sizeReplies"
            class="pager"
            layout="total, prev, pager, next"
            :total="totalReplies"
            @current-change="loadReplies"
            @size-change="loadReplies"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped lang="scss">
.my-comments-page {
  max-width: 860px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}

.heritage-group {
  margin-bottom: 28px;
  &:last-child {
    margin-bottom: 0;
  }
}

.group-head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(168, 44, 40, 0.2);
}

.group-head-main {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 10px 14px;
}

.group-title {
  margin: 0;
  font-size: 17px;
  font-weight: 800;
  color: #1f1a17;
}

.group-count {
  font-size: 13px;
  color: #7a6a62;
}

.page-head {
  margin-bottom: 8px;
  h1 {
    margin: 0 0 8px;
    font-size: 22px;
    font-weight: 800;
    color: #1f1a17;
  }
  .sub {
    margin: 0 0 20px;
    font-size: 14px;
    color: #6b645c;
    line-height: 1.5;
  }
}

.tabs {
  :deep(.el-tabs__item.is-active) {
    font-weight: 700;
  }
}

.pane {
  min-height: 200px;
  padding-top: 8px;
}

.card {
  padding: 16px 18px;
  margin-bottom: 12px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid rgba(31, 26, 23, 0.08);
  box-shadow: 0 6px 20px rgba(45, 28, 18, 0.05);
}

.reply-card {
  padding: 0;
  overflow: hidden;
  background: linear-gradient(135deg, #fffefb 0%, #fff 48%, #fffbf9 100%);
  border: 1px solid rgba(168, 44, 40, 0.12);
  box-shadow: 0 8px 28px rgba(110, 26, 26, 0.07);

  &::before {
    content: "";
    display: block;
    height: 3px;
    background: linear-gradient(90deg, #8b2e2a, #d0453c, #e8a598);
  }
}

.reply-card-inner {
  display: flex;
  gap: 16px;
  padding: 18px 18px 16px;
  align-items: flex-start;
}

.reply-avatar-wrap {
  flex-shrink: 0;
}

.reply-avatar-link {
  display: inline-flex;
  border-radius: 50%;
  outline: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  &:hover {
    transform: scale(1.04);
    box-shadow: 0 6px 18px rgba(168, 44, 40, 0.25);
  }
  &:focus-visible {
    box-shadow: 0 0 0 3px rgba(208, 69, 60, 0.35);
  }
}

.reply-avatar {
  border: 2px solid rgba(168, 44, 40, 0.2);
  box-shadow: 0 4px 14px rgba(45, 28, 18, 0.08);
}

.reply-main {
  flex: 1;
  min-width: 0;
}

.reply-head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
}

.reply-identity {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  .who {
    font-weight: 700;
    color: #2c2622;
    font-size: 15px;
  }
}

.reply-head .when {
  font-size: 12px;
  color: #9a8f88;
  flex-shrink: 0;
}

.reply-heritage {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  .lbl {
    font-size: 12px;
    color: #9a8f88;
    font-weight: 600;
  }
}

.reply-body {
  margin-top: 4px;
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(31, 26, 23, 0.06);
}

.reply-actions {
  border-top: none;
  padding-top: 8px;
  margin-top: 4px;
}

.heritage-line {
  font-size: 13px;
  color: #7a6a62;
  margin-bottom: 8px;
}

.hint-reply {
  margin: 0 0 6px;
  font-size: 12px;
  color: #a82c28;
  font-weight: 600;
}

.body {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #3d3833;
  white-space: pre-wrap;
  word-break: break-word;
}

.imgs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}
.img {
  width: 88px;
  height: 88px;
  border-radius: 10px;
  cursor: pointer;
}

.card-actions {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid rgba(31, 26, 23, 0.06);
  display: flex;
  gap: 12px;
}

.pager {
  justify-content: center;
  margin-top: 20px;
}
</style>
