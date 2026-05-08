<script setup lang="ts">
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import http from "../utils/http";
import config from "../config/index.js";

const router = useRouter();
const registerFormRef = ref<FormInstance>();
const loading = ref(false);

const registerForm = reactive({
  username: "",
  password: "",
  confirmPassword: ""
});

// 确认密码验证
const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'));
  } else {
    callback();
  }
};

const registerRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" }
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: "blur" }
  ]
};

const handleRegister = async () => {
  if (!registerFormRef.value) return;

  try {
    await registerFormRef.value.validate();
    loading.value = true;

    const res = await http.post("/register", {
      username: registerForm.username,
      password: registerForm.password
    });
    
    if (res.code === 200) {
      ElMessage.success("注册成功，请登录");
      await router.push("/login");
    } else {
      ElMessage.error(res.message || "注册失败");
    }
  } catch (error) {
    console.error("注册失败:", error);
    ElMessage.error("注册失败，请稍后重试");
  } finally {
    loading.value = false;
  }
};

// 跳转到登录页面
const goToLogin = () => {
  router.push("/login");
};
</script>

<template>
  <div class="register-container">
    <!-- 背景装饰元素 -->
    <div class="bg-decorations">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
      <div class="bg-grid"></div>
    </div>

    <!-- 注册框 -->
    <div class="register-box">
      <!-- 左侧背景图片区域 -->
      <div class="register-left">
        <div class="left-overlay"></div>
        <div class="left-content">
          <h2 class="system-name">{{ config.frontName }}</h2>
          <p class="welcome-text">{{ config.welcome }}</p>
        </div>
      </div>

      <!-- 右侧注册表单 -->
      <div class="register-right">
        <div class="form-container">
          <h2 class="form-title">用户注册</h2>

          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            label-width="0"
            size="large"
          >
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请确认密码"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleRegister"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                class="register-button"
                @click="handleRegister"
              >
                {{ loading ? "注册中..." : "注 册" }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <div class="login-link">
              <span>已有账号？</span>
              <el-button type="text" @click="goToLogin">立即登录</el-button>
            </div>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 背景样式 */
.register-container {
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

/* 注册框样式 */
.register-box {
  position: relative;
  display: flex;
  width: 900px;
  height: 600px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  z-index: 2001;
  animation: modalFadeIn 0.3s ease-out;
}

/* 左侧背景图片区域 */
.register-left {
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
  background-color: var(--primary-color);
}

.left-content {
  position: relative;
  z-index: 1;
  color: white;
  text-align: center;
  padding: 0 40px;
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

/* 右侧注册表单 */
.register-right {
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

.register-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
  margin-top: 10px;
}

.form-footer {
  margin-top: 30px;
  text-align: center;
}

.login-link {
  margin-bottom: 20px;
  font-size: 14px;
  color: #666;
}

.login-link span {
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
  .register-box {
    width: 90%;
    height: auto;
    flex-direction: column;
  }

  .register-left,
  .register-right {
    width: 100%;
  }

  .register-left {
    padding: 40px;
    height: 200px;
  }

  .register-right {
    padding: 40px 20px;
  }
}
</style>