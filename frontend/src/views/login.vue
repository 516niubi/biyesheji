<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock, UserFilled, Key } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import useUserStore from "../stores/userStore";
import http from "../utils/http";
import { roles } from "../utils/menu.js";
import config from "../config/index.js";

const userStore = useUserStore();
const router = useRouter();
const route = useRoute();

/** 登录成功后的回跳地址（仅允许站内相对路径，防开放重定向） */
const resolvePostLoginPath = (fallback) => {
  const r = route.query.redirect;
  const s = Array.isArray(r) ? r[0] : r;
  if (typeof s === "string" && s.startsWith("/") && !s.startsWith("//")) {
    return s;
  }
  return fallback;
};
const loginFormRef = ref<FormInstance>();
const loading = ref(false);
const captchaDataUrl = ref("");
const loginForm = reactive({
  username: "caiya",
  password: "123",
  role: "admin",
  captchaKey: "",
  captcha: "",
});

const loginRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  role: [{ required: true, message: "请选择角色", trigger: "change" }],
  captcha: [{ required: true, message: "请输入验证码", trigger: "blur" }],
};

const refreshCaptcha = async () => {
  loginForm.captcha = "";
  try {
    const res = await http.get("/captcha");
    if (res.code === 200 && res.data) {
      loginForm.captchaKey = res.data.key;
      captchaDataUrl.value = `data:image/png;base64,${res.data.imageBase64}`;
    }
  } catch {
    ElMessage.error("验证码加载失败");
  }
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;

  try {
    await loginFormRef.value.validate();
    loading.value = true;

    if (loginForm.role == "admin") {
      const res = await http.post("/admin/login", loginForm);
      if (res.code == 200) {
        userStore.setToken(res.data.token);
        userStore.setUserInfo(res.data);
        userStore.setRoleFlag(loginForm.role);
        ElMessage.success("登录成功");
        await router.push(resolvePostLoginPath("/home"));
      } else if (res.code === 520) {
        ElMessage.error("无权限");
        await refreshCaptcha();
      } else {
        await refreshCaptcha();
      }
    } else if (loginForm.role == "inheritor") {
      const res = await http.post("/inheritor/login", loginForm);
      if (res.code == 200) {
        userStore.setToken(res.data.token);
        userStore.setUserInfo(res.data);
        userStore.setRoleFlag("inheritor");
        ElMessage.success("登录成功");
        await router.push(resolvePostLoginPath("/home"));
      } else if (res.code === 520) {
        ElMessage.error("无权限");
        await refreshCaptcha();
      } else {
        await refreshCaptcha();
      }
    } else {
      const res = await http.post("/login", loginForm);
      if (res.code == 200) {
        userStore.setToken(res.data.token);
        userStore.setUserInfo(res.data);
        userStore.setRoleFlag(loginForm.role);
        ElMessage.success("登录成功");
        await router.push(resolvePostLoginPath("/front/home"));
      } else if (res.code === 520) {
        ElMessage.error("无权限");
        await refreshCaptcha();
      } else {
        await refreshCaptcha();
      }
    }
  } catch (error) {
    console.error("登录失败:", error);
    ElMessage.error("登录失败，请检查用户名和密码");
    await refreshCaptcha();
  } finally {
    loading.value = false;
  }
};

const goToRegister = () => {
  router.push("/register");
};

const goToInheritorRegister = () => {
  router.push("/inheritorRegister");
};

onMounted(() => {
  refreshCaptcha();
});
</script>

<template>
  <div class="login-container">
    <!-- 非遗意象：纸纹、祥云、窗棂格 -->
    <div class="heritage-bg" aria-hidden="true">
      <div class="paper-texture" />
      <svg class="pattern-clouds" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <pattern
            id="lattice"
            width="48"
            height="48"
            patternUnits="userSpaceOnUse"
          >
            <path
              d="M0 24h48M24 0v48"
              fill="none"
              stroke="currentColor"
              stroke-width="0.35"
              opacity="0.12"
            />
          </pattern>
        </defs>
        <rect width="100%" height="100%" fill="url(#lattice)" />
      </svg>
      <div class="cloud cloud-a" />
      <div class="cloud cloud-b" />
      <div class="cloud cloud-c" />
      <div class="seal-decoration" />
    </div>

    <div class="login-box">
      <div class="login-left">
        <div class="left-pattern" />
        <div class="left-overlay" />
        <div class="left-content">
          <div class="ornament-line" />
          <h2 class="system-name">{{ config.frontName }}</h2>
          <p class="tagline">传承 · 保护 · 共享</p>
          <p class="welcome-text">{{ config.welcome }}</p>
          <ul class="heritage-bullets">
            <li>传统技艺与民俗</li>
            <li>数字化记录与传播</li>
            <li>连接传承人与公众</li>
          </ul>
          <div class="ornament-line" />
        </div>
      </div>

      <div class="login-right">
        <div class="form-container">
          <h2 class="form-title">用户登录</h2>
          <p class="form-sub">欢迎回到非遗文化数字化保护平台</p>

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
                />
              </el-select>
            </el-form-item>

            <el-form-item prop="captcha">
              <div class="captcha-row">
                <el-input
                  v-model="loginForm.captcha"
                  placeholder="验证码"
                  :prefix-icon="Key"
                  maxlength="6"
                  clearable
                  class="captcha-input"
                  @keyup.enter="handleLogin"
                />
                <button
                  type="button"
                  class="captcha-thumb"
                  title="点击刷新验证码"
                  @click="refreshCaptcha"
                >
                  <img
                    v-if="captchaDataUrl"
                    :src="captchaDataUrl"
                    alt="验证码"
                  />
                  <span v-else class="captcha-placeholder">加载中</span>
                </button>
              </div>
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
              <span>前台用户注册</span>
              <el-button type="text" @click="goToRegister">立即注册</el-button>
            </div>
            <div class="register-link">
              <span>非遗传承人入驻</span>
              <el-button type="text" @click="goToInheritorRegister">提交资料审核</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  position: fixed;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  overflow: hidden;
  background: linear-gradient(
    145deg,
    #2c1810 0%,
    #5c2a1e 28%,
    #8b3a2e 55%,
    #4a3428 100%
  );
}

.heritage-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.paper-texture {
  position: absolute;
  inset: 0;
  opacity: 0.14;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='120'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.45'/%3E%3C/svg%3E");
}

.pattern-clouds {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  color: #f5e6d3;
}

.cloud {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(
    circle,
    rgba(245, 230, 211, 0.22) 0%,
    transparent 70%
  );
  filter: blur(2px);
}

.cloud-a {
  width: 420px;
  height: 180px;
  top: 8%;
  left: -8%;
  transform: rotate(-8deg);
}

.cloud-b {
  width: 360px;
  height: 160px;
  bottom: 12%;
  right: -5%;
  transform: rotate(6deg);
}

.cloud-c {
  width: 280px;
  height: 120px;
  top: 42%;
  right: 18%;
  opacity: 0.7;
}

.seal-decoration {
  position: absolute;
  top: 50%;
  left: 50%;
  width: min(88vw, 720px);
  height: min(88vw, 720px);
  transform: translate(-50%, -50%);
  border: 1px solid rgba(212, 175, 55, 0.12);
  border-radius: 12px;
  box-shadow: inset 0 0 0 1px rgba(212, 175, 55, 0.06);
}

.login-box {
  position: relative;
  display: flex;
  width: min(920px, 94vw);
  min-height: 560px;
  background: #fffdf8;
  border-radius: 4px;
  box-shadow:
    0 24px 80px rgba(0, 0, 0, 0.35),
    0 0 0 1px rgba(212, 175, 55, 0.35),
    inset 0 0 0 1px rgba(139, 58, 46, 0.08);
  overflow: hidden;
  z-index: 2001;
  animation: modalFadeIn 0.45s ease-out;
}

.login-left {
  width: 44%;
  min-height: 560px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(
    165deg,
    #6b2c22 0%,
    #8b3a2e 40%,
    #5a241c 100%
  );
}

.left-pattern {
  position: absolute;
  inset: 0;
  opacity: 0.2;
  background-image: repeating-linear-gradient(
      -45deg,
      transparent,
      transparent 6px,
      rgba(212, 175, 55, 0.12) 6px,
      rgba(212, 175, 55, 0.12) 7px
    ),
    repeating-linear-gradient(
      45deg,
      transparent,
      transparent 10px,
      rgba(0, 0, 0, 0.06) 10px,
      rgba(0, 0, 0, 0.06) 11px
    );
}

.left-overlay {
  position: absolute;
  inset: 0;
  background: radial-gradient(
    ellipse at 30% 20%,
    rgba(212, 175, 55, 0.15) 0%,
    transparent 55%
  );
}

.left-content {
  position: relative;
  z-index: 1;
  color: #fdf6e9;
  text-align: center;
  padding: 0 36px;
  max-width: 320px;
}

.ornament-line {
  height: 1px;
  margin: 16px auto;
  max-width: 160px;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(212, 175, 55, 0.85),
    transparent
  );
}

.left-content .system-name {
  font-size: 22px;
  margin-bottom: 8px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.35);
}

.tagline {
  font-size: 13px;
  letter-spacing: 0.35em;
  color: rgba(253, 246, 233, 0.85);
  margin: 0 0 12px;
}

.left-content .welcome-text {
  font-size: 14px;
  opacity: 0.92;
  line-height: 1.6;
  margin-bottom: 16px;
}

.heritage-bullets {
  list-style: none;
  padding: 0;
  margin: 0;
  font-size: 13px;
  text-align: left;
  color: rgba(253, 246, 233, 0.88);
  line-height: 2;
}

.heritage-bullets li::before {
  content: "◆ ";
  color: #d4af37;
  font-size: 8px;
  vertical-align: middle;
  margin-right: 8px;
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 44px 40px;
  background: linear-gradient(180deg, #fffdf8 0%, #faf6f0 100%);
}

.form-container {
  width: 100%;
  max-width: 380px;
}

.form-title {
  font-size: 26px;
  color: #3d2914;
  margin: 0 0 8px;
  text-align: center;
  font-weight: 600;
  letter-spacing: 0.12em;
}

.form-sub {
  margin: 0 0 28px;
  text-align: center;
  font-size: 13px;
  color: #7a6a58;
}

.captcha-row {
  display: flex;
  gap: 10px;
  width: 100%;
  align-items: stretch;
}

.captcha-input {
  flex: 1;
  min-width: 0;
}

.captcha-thumb {
  flex-shrink: 0;
  width: 120px;
  height: 48px;
  padding: 2px 4px;
  box-sizing: border-box;
  border: 1px solid #dcd3c4;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: border-color 0.2s;
}

.captcha-thumb:hover {
  border-color: #b8860b;
}

.captcha-thumb img {
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
  object-fit: contain;
  display: block;
}

.captcha-placeholder {
  font-size: 12px;
  color: #999;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
  margin-top: 6px;
  background: linear-gradient(135deg, #a52a2a 0%, #6b1c1c 100%) !important;
  border: none !important;
}

.login-button:hover {
  filter: brightness(1.06);
}

.form-footer {
  margin-top: 24px;
  text-align: center;
}

.register-link {
  margin-bottom: 16px;
  font-size: 14px;
  color: #666;
}

.register-link span {
  margin-right: 8px;
}

:deep(.el-input__wrapper) {
  height: 48px;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e8dfd0 inset;
  background: #fff;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #d4c4b0 inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #a52a2a inset !important;
}

:deep(.el-input__inner) {
  height: 100%;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-select .el-input__wrapper) {
  height: 48px;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e8dfd0 inset;
}

:deep(.el-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #d4c4b0 inset;
}

:deep(.el-select .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #a52a2a inset !important;
}

:deep(.el-select .el-input__inner) {
  height: 100%;
}

@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: translateY(-16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 960px) {
  .login-box {
    flex-direction: column;
    min-height: unset;
    width: 92vw;
  }

  .login-left {
    width: 100%;
    min-height: 220px;
  }

  .login-right {
    padding: 32px 24px 40px;
  }

  .heritage-bullets {
    display: none;
  }
}
</style>
