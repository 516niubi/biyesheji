<script setup lang="ts">
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock, UserFilled } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import useUserStore from "../stores/userStore";
import http from "../utils/http";
import { roles } from "../utils/menu.js";
import config from "../config/index.js";

const userStore = useUserStore();
const router = useRouter();
const loginFormRef = ref<FormInstance>();
const loading = ref(false);

const loginForm = reactive({
  username: "caiya",
  password: "123",
  role: "admin", // 默认选择管理员角色
});

const loginRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  role: [{ required: true, message: "请选择角色", trigger: "change" }],
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;

  try {
    await loginFormRef.value.validate();
    loading.value = true;

    if (loginForm.role == "admin") {
       const res = await http.post("/admin/login", loginForm);
      if (res.code == 200) {
         userStore.setToken(res.data.token),
          userStore.setUserInfo(res.data),
          userStore.setRoleFlag(loginForm.role),
        ElMessage.success("登录成功");
        await router.push("/");
      } else if (res.code === 520) {
        ElMessage.error("无权限");
      }
    } else {
     const res = await http.post("/login", loginForm);
      if (res.code == 200) {
        userStore.setToken(res.data.token),
        userStore.setUserInfo(res.data),
        userStore.setRoleFlag(loginForm.role),
        ElMessage.success("登录成功");
        await router.push("/front/home");
      } else if (res.code === 520) {
        ElMessage.error("无权限");
      }
    }

    
  } catch (error) {
    console.error("登录失败:", error);
    ElMessage.error("登录失败，请检查用户名和密码");
  } finally {
    loading.value = false;
  }
};

// 跳转到注册页面
const goToRegister = () => {
  router.push("/register");
};
</script>

<template>
  <div class="login-container">
    <!-- 背景装饰元素 -->
    <div class="bg-decorations">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
      <div class="bg-grid"></div>
    </div>

    <!-- 登录框 -->
    <div class="login-box">
      <!-- 左侧背景图片区域 -->
      <div class="login-left">
        <div class="left-overlay"></div>
        <div class="left-content">
          <img src="@/assets/logo.png" alt="系统Logo" class="logo" />
          <h2 class="system-name">{{ config.frontName }}</h2>
          <p class="welcome-text">{{ config.welcome }}</p>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-right">
        <div class="form-container">
          <h2 class="form-title">用户登录</h2>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            label-width="0"
            size="large"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item prop="role">
              <el-select
                v-model="loginForm.role"
                placeholder="请选择角色"
                :prefix-icon="UserFilled"
              >
                <el-option
                  v-for="role in roles"
                  :key="role.value"
                  :label="role.label"
                  :value="role.value"
                ></el-option>
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                class="login-button"
                @click="handleLogin"
              >
                {{ loading ? "登录中..." : "登 录" }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <div class="register-link">
              <span>还没有账号？</span>
              <el-button type="text" @click="goToRegister">立即注册</el-button>
            </div>
            <p class="copyright">© 2023 {{ config.frontName }} 版权所有</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 背景样式 */
.login-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ed 100%);
  overflow: hidden;
}

.bg-decorations {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.6;
}

.bg-circle-1 {
  width: 600px;
  height: 600px;
  top: -200px;
  left: -200px;
  background: linear-gradient(135deg, #1976d2 0%, #64b5f6 100%);
}

.bg-circle-2 {
  width: 400px;
  height: 400px;
  bottom: -100px;
  right: -100px;
  background: linear-gradient(135deg, #4fc3f7 0%, #b3e5fc 100%);
}

.bg-circle-3 {
  width: 300px;
  height: 300px;
  top: 50%;
  right: 100px;
  background: linear-gradient(135deg, #bbdefb 0%, #e3f2fd 100%);
}

.bg-grid {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: linear-gradient(
      rgba(255, 255, 255, 0.3) 1px,
      transparent 1px
    ),
    linear-gradient(90deg, rgba(255, 255, 255, 0.3) 1px, transparent 1px);
  background-size: 40px 40px;
  opacity: 0.3;
}

/* 登录框样式 */
.login-box {
  position: relative;
  display: flex;
  width: 900px;
  height: 550px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  z-index: 2001;
  animation: modalFadeIn 0.3s ease-out;
}

/* 左侧背景图片区域 */
.login-left {
  width: 45%;
  background-size: cover;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.left-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  /* background: rgba(25, 118, 210, 0.6); */
  background-color: var(--primary-color);
}

.left-content {
  position: relative;
  z-index: 1;
  color: white;
  text-align: center;
  padding: 0 40px;
}

.left-content .logo {
  width: 120px;
  height: 120px;
  border-radius: 6px;
  margin-bottom: 20px;
}

.left-content .system-name {
  font-size: 28px;
  margin-bottom: 15px;
  font-weight: 600;
}

.left-content .welcome-text {
  font-size: 16px;
  opacity: 0.9;
}

/* 右侧登录表单 */
.login-right {
  width: 55%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.form-container {
  width: 100%;
  max-width: 350px;
}

.form-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 30px;
  text-align: center;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
  margin-top: 10px;
}

.remember-me {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.forgot-password {
  font-size: 14px;
}

.form-footer {
  margin-top: 30px;
  text-align: center;
}

.register-link {
  margin-bottom: 20px;
  font-size: 14px;
  color: #666;
}

.register-link span {
  margin-right: 8px;
}

.copyright {
  margin: 0;
  font-size: 12px;
  color: #999;
}

/* 输入框样式优化 */
:deep(.el-input__wrapper) {
  height: 48px;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

:deep(.el-input__inner) {
  height: 100%;
}

/* 下拉框样式优化 */
:deep(.el-select) {
  width: 100%;
}

:deep(.el-select .el-input__wrapper) {
  height: 48px;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

:deep(.el-select .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

:deep(.el-select .el-input__inner) {
  height: 100%;
}

/* 动画 */
@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 960px) {
  .login-box {
    width: 90%;
    height: auto;
    flex-direction: column;
  }

  .login-left,
  .login-right {
    width: 100%;
  }

  .login-left {
    padding: 40px;
    height: 200px;
  }

  .login-right {
    padding: 40px 20px;
  }
}
</style>
