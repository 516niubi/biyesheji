<script setup lang="ts">
import { ref, onMounted } from "vue";
import Header from "./Header.vue";
import Footer from "./Footer.vue";

// 页面加载状态
const loading = ref(true);

// 组件挂载后
onMounted(() => {
  // 模拟页面加载完成
  setTimeout(() => {
    loading.value = false;
  }, 300);
});
</script>

<template>
  <div class="front-layout">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>
    </div>

    <!-- 主布局 -->
    <div v-else class="layout-container">
      <!-- 头部 -->
      <Header />

      <!-- 主要内容区域 -->
      <main class="main-content">
        <router-view />
      </main>

      <!-- 底部 -->
      <Footer />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.front-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;

  .loading-container {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: #f8f8f8;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;

    .loading-spinner {
      text-align: center;

      .spinner {
        width: 40px;
        height: 40px;
        border: 4px solid #f3f3f3;
        border-top: 4px solid #409eff;
        border-radius: 50%;
        animation: spin 1s linear infinite;
        margin: 0 auto 16px;
      }

      p {
        color: #606266;
        font-size: 14px;
        margin: 0;
      }
    }
  }

  .layout-container {
    min-height: 100vh;
    display: flex;
    flex-direction: column;

    .main-content {
      flex: 1;
      min-height: calc(100vh - 140px); // 减去头部和底部的大概高度
      background: #f8f9fa;
    }
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

// 全局样式重置
:deep(*) {
  box-sizing: border-box;
}

:deep(body) {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  line-height: 1.6;
  color: #333;
  background: #fff;
}

:deep(a) {
  text-decoration: none;
  color: inherit;
  transition: color 0.3s;
}

:deep(ul, ol) {
  list-style: none;
  margin: 0;
  padding: 0;
}

:deep(h1, h2, h3, h4, h5, h6) {
  margin: 0;
  font-weight: 600;
}

:deep(p) {
  margin: 0;
}

// 响应式设计
@media (max-width: 768px) {
  .front-layout {
    .layout-container {
      .main-content {
        min-height: calc(100vh - 120px);
      }
    }
  }
}

@media (max-width: 480px) {
  .front-layout {
    .layout-container {
      .main-content {
        min-height: calc(100vh - 100px);
      }
    }
  }
}
</style>