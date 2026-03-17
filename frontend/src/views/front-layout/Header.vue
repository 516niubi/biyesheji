<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, ArrowDown } from "@element-plus/icons-vue";
import { getImageUrl } from "../../utils/system";
import config from "../../config";
import useUserStore from "../../stores/userStore";

const router = useRouter();
const route = useRoute();

// 用户信息
const userInfo = ref(null);
const isLoggedIn = computed(() => !!userInfo.value);
const userStore = useUserStore();

// 菜单数据
const menuList = ref([
  { name: "首页", path: "/front/home" },
  { name: "非遗文物", path: "/front/heritage" },
  { name: "活动中心", path: "/front/activity" },
  { name: "新闻资讯", path: "/front/news" },
  { name: "非遗宣传", path: "/front/videos" },
  { name: "意见反馈", path: "/front/feedback" },
]);

// 当前激活的菜单
const activeMenu = ref("/front/home");

// 获取用户信息
const getUserInfo = () => {
  const token = userStore.token;
  const user = userStore.userInfo;
  console.log("user", user);
  console.log("token", token);
  if (token && user) {
    try {
      userInfo.value = {...user};
    } catch (error) {
      console.error("解析用户信息失败:", error);
    }
  }
};

// 菜单点击
const handleMenuClick = (path: string) => {
  activeMenu.value = path;
  router.push(path);
};

// 登录
const handleLogin = () => {
  router.push("/login");
};

// 注册
const handleRegister = () => {
  router.push("/register");
};

// 退出登录
const handleLogout = () => {
  localStorage.removeItem("token");
  localStorage.removeItem("userInfo");
  userInfo.value = null;
  ElMessage.success("退出登录成功");
  router.push("/login");
};

// 个人中心
const handleProfile = () => {
  router.push("/front/profile");
};

// 我的报名
const handleMyApplications = () => {
  router.push("/front/myApplications");
};

// 我的收藏
const handleMyCollect = () => {
  router.push("/front/myCollect");
};

onMounted(() => {
  getUserInfo();
});

watch(() => route.path, newPath => {
  activeMenu.value = newPath;
}, {immediate: true})
</script>

<template>
  <header class="front-header">
    <div class="header-container">
      <!-- 左侧：Logo + 系统名称 -->
      <div class="header-left">
        <div class="logo-section" @click="handleMenuClick('/front')">
          <img src="../../assets/logo.png" alt="Logo" class="logo" />
          <h1 class="system-name">{{ config.frontName }}</h1>
        </div>
      </div>

      <!-- 中间：菜单栏 -->
      <nav class="header-center">
        <ul class="nav-menu">
          <li
            v-for="menu in menuList"
            :key="menu.path"
            class="nav-item"
            :class="{ active: activeMenu === menu.path }"
            @click="handleMenuClick(menu.path)"
          >
            {{ menu.name }}
          </li>
        </ul>
      </nav>

      <!-- 右侧：登录操作 -->
      <div class="header-right">
        <!-- 未登录状态 -->
        <div v-if="!isLoggedIn" class="auth-buttons">
          <el-button type="primary" @click="handleLogin">登录</el-button>
          <el-button @click="handleRegister">注册</el-button>
        </div>

        <!-- 已登录状态 -->
        <div v-else class="user-info">
          <el-dropdown @command="handleCommand">
            <div class="user-profile">
              <el-avatar
                :src="userInfo.avatar ? getImageUrl(userInfo.avatar) : ''"
                :icon="User"
                :size="32"
              />
              <span class="username">{{ userInfo.nickName || userInfo.username }}</span>
              <el-icon class="dropdown-icon">
                <ArrowDown />
              </el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile" @click="handleProfile">个人中心</el-dropdown-item>
                <el-dropdown-item command="applications" @click="handleMyApplications">我的报名</el-dropdown-item>
                <el-dropdown-item command="collect" @click="handleMyCollect">我的收藏</el-dropdown-item>
                <el-dropdown-item divided command="logout" @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
  </header>
</template>

<style lang="scss" scoped>
.front-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;

  .header-container {
    max-width: 80%;
    margin: 0 auto;
    padding: 0 20px;
    height: 70px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .header-left {
    .logo-section {
      display: flex;
      align-items: center;
      cursor: pointer;
      transition: opacity 0.3s;

      &:hover {
        opacity: 0.8;
      }

      .logo {
        width: 40px;
        height: 40px;
        margin-right: 12px;
      }

      .system-name {
        font-size: 24px;
        font-weight: 600;
        color: #2c3e50;
        margin: 0;
        white-space: nowrap;
      }
    }
  }

  .header-center {
    flex: 1;
    display: flex;
    justify-content: center;

    .nav-menu {
      display: flex;
      list-style: none;
      margin: 0;
      padding: 0;
      gap: 40px;

      .nav-item {
        padding: 10px 16px;
        font-size: 16px;
        font-weight: 500;
        color: #606266;
        cursor: pointer;
        border-radius: 6px;
        transition: all 0.3s;
        position: relative;

        &:hover {
          color: #409eff;
          background-color: #f0f9ff;
        }

        &.active {
          color: #409eff;
          background-color: #e6f7ff;

          &::after {
            content: "";
            position: absolute;
            bottom: -2px;
            left: 50%;
            transform: translateX(-50%);
            width: 20px;
            height: 3px;
            background-color: #409eff;
            border-radius: 2px;
          }
        }
      }
    }
  }

  .header-right {
    .auth-buttons {
      display: flex;
      gap: 12px;

      .el-button {
        padding: 8px 20px;
        font-weight: 500;
      }
    }

    .user-info {
      .user-profile {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 6px 12px;
        border-radius: 20px;
        cursor: pointer;
        transition: background-color 0.3s;

        &:hover {
          background-color: #f5f7fa;
        }

        .username {
          font-size: 14px;
          font-weight: 500;
          color: #303133;
          max-width: 100px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .dropdown-icon {
          font-size: 12px;
          color: #909399;
          transition: transform 0.3s;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .front-header {
    .header-container {
      padding: 0 15px;
      height: 60px;
    }

    .header-left {
      .logo-section {
        .logo {
          width: 32px;
          height: 32px;
          margin-right: 8px;
        }

        .system-name {
          font-size: 18px;
        }
      }
    }

    .header-center {
      .nav-menu {
        gap: 20px;

        .nav-item {
          padding: 8px 12px;
          font-size: 14px;
        }
      }
    }
  }
}

@media (max-width: 480px) {
  .front-header {
    .header-container {
      flex-wrap: wrap;
      height: auto;
      padding: 10px 15px;
    }

    .header-center {
      order: 3;
      width: 100%;
      margin-top: 10px;

      .nav-menu {
        justify-content: center;
        flex-wrap: wrap;
        gap: 15px;
      }
    }
  }
}
</style>