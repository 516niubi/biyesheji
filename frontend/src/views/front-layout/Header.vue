<script setup lang="ts">
import { ref, computed, watch, nextTick } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import { User, ArrowDown } from "@element-plus/icons-vue";
import { getImageUrl } from "../../utils/system";
import config from "../../config";
import useUserStore from "../../stores/userStore";
import http from "../../utils/http";

const router = useRouter();
const route = useRoute();

const userStore = useUserStore();
const userInfo = computed(() => userStore.userInfo || {});
const isLoggedIn = computed(() => {
  const hasToken = !!userStore.token;
  const hasUserInfo = !!userInfo.value && Object.keys(userInfo.value).length > 0;
  return hasToken && hasUserInfo;
});

/** 前台「用户」角色可修改登录密码（与后台传承人/管理员入口分离） */
const isFrontRegisteredUser = computed(() => userStore.roleFlag === "user");

const pwdVisible = ref(false);
const pwdFormRef = ref<FormInstance>();
const pwdForm = ref({ oldPassword: "", newPassword: "", confirmPassword: "" });
const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: "请输入原密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码至少 6 位", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请再次输入新密码", trigger: "blur" },
    {
      validator: (_r, v, cb) => {
        if (v !== pwdForm.value.newPassword) cb(new Error("两次输入的密码不一致"));
        else cb();
      },
      trigger: "blur",
    },
  ],
};

const openChangePassword = () => {
  pwdForm.value = { oldPassword: "", newPassword: "", confirmPassword: "" };
  nextTick(() => {
    pwdVisible.value = true;
  });
};

const submitChangePassword = async () => {
  if (!pwdFormRef.value) return;
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return;
    const uid = (userInfo.value as any)?.id;
    if (uid == null) {
      ElMessage.error("无法获取用户信息");
      return;
    }
    try {
      const res = await http.post("/user/updatePass", {
        userId: uid,
        oldPass: pwdForm.value.oldPassword,
        password: pwdForm.value.newPassword,
      });
      if (res.code === 200) {
        ElMessage.success("密码已修改，请重新登录");
        pwdVisible.value = false;
        userStore.logout();
      }
    } catch {
      ElMessage.error("修改失败，请检查原密码是否正确");
    }
  });
};

// 菜单数据
const menuList = ref([
  { name: "首页", path: "/front/home" },
  { name: "非遗文物", path: "/front/heritage" },
  { name: "传承人", path: "/front/inheritors" },
  { name: "活动中心", path: "/front/activity" },
  { name: "新闻资讯", path: "/front/news" },
  { name: "非遗宣传", path: "/front/videos" },
  { name: "意见反馈", path: "/front/feedback" },
]);

// 当前激活的菜单
const activeMenu = ref("/front/home");

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
  userStore.logout();
  ElMessage.success("退出登录成功");
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

// 我的评论（发表与收到的回复）
const handleMyComments = () => {
  router.push("/front/myComments");
};

const handleInbox = () => {
  router.push("/front/inbox");
};

const handleCommand = (command: string) => {
  if (command === "profile") handleProfile();
  if (command === "applications") handleMyApplications();
  if (command === "collect") handleMyCollect();
  if (command === "comments") handleMyComments();
  if (command === "inbox") handleInbox();
  if (command === "changePassword") openChangePassword();
  if (command === "logout") handleLogout();
};

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
          <el-button type="primary" @click="handleLogin">请登录</el-button>
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
                <el-dropdown-item command="comments" @click="handleMyComments">我的评论</el-dropdown-item>
                <el-dropdown-item command="inbox" @click="handleInbox">私信</el-dropdown-item>
                <el-dropdown-item v-if="isFrontRegisteredUser" command="changePassword">
                  修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout" @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="pwdVisible"
      title="修改密码"
      width="420px"
      align-center
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
      class="front-pwd-dialog"
      :z-index="5000"
    >
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="88px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitChangePassword">确定</el-button>
      </template>
    </el-dialog>
  </header>
</template>

<style lang="scss" scoped>
.front-header {
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
  box-shadow: 0 6px 22px rgba(15, 23, 42, 0.08);
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  position: sticky;
  top: 0;
  z-index: 1000;

  .header-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 16px;
    height: 74px;
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

      .system-name {
        font-size: 23px;
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
      gap: 12px;

      .nav-item {
        padding: 8px 16px;
        font-size: 15px;
        font-weight: 500;
        color: #606266;
        cursor: pointer;
        border-radius: 999px;
        transition: all 0.3s;
        position: relative;

        &:hover {
          color: #2563eb;
          background: #eff6ff;
        }

        &.active {
          color: #fff;
          background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
          box-shadow: 0 8px 18px rgba(37, 99, 235, 0.28);

          &::after {
            display: none;
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
        padding: 8px 18px;
        font-weight: 500;
        border-radius: 999px;
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
          background-color: #eef3ff;
        }

        .username {
          font-size: 14px;
          font-weight: 500;
          color: #1f2937;
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
      height: 64px;
    }

    .header-left {
      .logo-section {
        .system-name {
          font-size: 18px;
        }
      }
    }

    .header-center {
      .nav-menu {
        gap: 8px;

        .nav-item {
          padding: 7px 12px;
          font-size: 13px;
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