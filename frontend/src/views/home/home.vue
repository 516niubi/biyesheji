<template>
  <div class="home">
    <h1 class="welcome-title">欢迎来到 {{ Config.systemName }}</h1>

    <!-- 公告展示区域 -->
    <div class="notice-container">
      <div class="timeline">
        <div
          v-for="notice in noticeList"
          :key="notice.id"
          class="timeline-item"
        >
          <div class="timeline-date">
            {{ notice.createTime?.split(" ")[0] }}
          </div>
          <div class="timeline-content">
            <h3>{{ notice.title }}</h3>
            <div class="commit-info">
              <span
                >{{ notice.createBy }} committed
                {{ notice.createTime?.split(" ")[1] }}</span
              >
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import Config from "../../config";
import http from "../../utils/http";
import { onMounted, ref } from "vue";

// 公告列表数据
const noticeList = ref([]);

// 获取公告数据
const getNoticeData = async () => {
  const res = await http.get("/notice/list");
  if (res.code === 200) {
    noticeList.value = res.data;
  }
};

// 页面加载时获取数据
onMounted(() => {
  getNoticeData();
});
</script>

<style scoped>
.home {
  padding: 20px;
}

.welcome-title {
  text-align: center;
  margin-bottom: 40px;
  color: var(--el-color-primary);
}

.notice-container {
  max-width: 800px;
  margin: 0 auto;
}

.timeline {
  position: relative;
  padding-left: 40px;
}

.timeline::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 2px;
  background-color: #e4e7ed;
}

.timeline-item {
  position: relative;
  margin-bottom: 30px;
}

.timeline-item::before {
  content: "";
  position: absolute;
  left: -44px;
  top: 5px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #fff;
  border: 2px solid var(--el-color-primary);
}

.timeline-date {
  color: var(--el-text-color-secondary);
  font-size: 14px;
  margin-bottom: 8px;
}

.timeline-content {
  background-color: #fff;
  padding: 16px 20px;
  border-radius: 6px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.timeline-content:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.timeline-content h3 {
  margin: 0 0 8px 0;
  color: var(--el-color-primary);
  font-size: 16px;
}

.commit-info {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}
</style>
