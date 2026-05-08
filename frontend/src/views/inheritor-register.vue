<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, type FormInstance, type UploadRawFile } from "element-plus";
import { User, Lock, Phone, Document, Key } from "@element-plus/icons-vue";
import http from "../utils/http";
import Config from "../config/index.js";

const router = useRouter();
const formRef = ref<FormInstance>();
const loading = ref(false);
const captchaDataUrl = ref("");
const certificateUrls = ref<string[]>([]);

const form = reactive({
  username: "",
  password: "",
  confirmPassword: "",
  nickName: "",
  phone: "",
  gender: 2,
  age: 18,
  avatar: "",
  profile: "",
  captchaKey: "",
  captcha: "",
});

const uploadUrl = `${Config.baseUrl}/file/upload`;
const uploadHeaders = {
  authorization: `${localStorage.getItem("token") || ""}`,
};

const validateConfirm = (_rule: unknown, value: string, callback: (e?: Error) => void) => {
  if (!value) callback(new Error("请再次输入密码"));
  else if (value !== form.password) callback(new Error("两次密码不一致"));
  else callback();
};

const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  confirmPassword: [{ required: true, validator: validateConfirm, trigger: "blur" }],
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    { pattern: /^1\d{10}$/, message: "请输入11位手机号", trigger: "blur" },
  ],
  profile: [{ required: true, message: "请填写个人简介", trigger: "blur" }],
  captcha: [{ required: true, message: "请输入验证码", trigger: "blur" }],
};

const refreshCaptcha = async () => {
  form.captcha = "";
  try {
    const res = await http.get("/captcha");
    if (res.code === 200 && res.data) {
      form.captchaKey = res.data.key;
      captchaDataUrl.value = `data:image/png;base64,${res.data.imageBase64}`;
    }
  } catch {
    ElMessage.error("验证码加载失败");
  }
};

const onCertSuccess = (response: { code?: number; data?: string }) => {
  if (response?.code === 200 && response.data) {
    certificateUrls.value.push(response.data);
    ElMessage.success("材料已上传");
  }
};

const beforeCertUpload = (raw: UploadRawFile) => {
  const ok =
    raw.type.includes("image") ||
    raw.type === "application/pdf" ||
    raw.name?.toLowerCase().endsWith(".pdf");
  if (!ok) {
    ElMessage.error("仅支持图片或 PDF");
    return false;
  }
  if (raw.size / 1024 / 1024 > 15) {
    ElMessage.error("单文件不超过 15MB");
    return false;
  }
  return true;
};

const removeCert = (url: string) => {
  certificateUrls.value = certificateUrls.value.filter((u) => u !== url);
};

const submit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate();
  if (certificateUrls.value.length === 0) {
    ElMessage.warning("请至少上传一份认证证书或证明材料");
    return;
  }
  loading.value = true;
  try {
    const res = await http.post("/inheritor/register", {
      username: form.username,
      password: form.password,
      nickName: form.nickName || form.username,
      phone: form.phone,
      gender: form.gender,
      age: form.age,
      avatar: form.avatar || undefined,
      profile: form.profile,
      certificateUrls: certificateUrls.value,
      captchaKey: form.captchaKey,
      captcha: form.captcha,
    });
    if (res.code === 200) {
      ElMessage.success("提交成功，请等待管理员审核通过后再登录");
      await router.push("/login");
    } else {
      await refreshCaptcha();
    }
  } catch {
    await refreshCaptcha();
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  refreshCaptcha();
});
</script>

<template>
  <div class="page">
    <div class="panel">
      <h1 class="title">非遗传承人注册</h1>
      <p class="sub">填写资料并上传认证材料，由管理员审核通过后即可登录工作台</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="large">
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="密码" :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickName" placeholder="选填，默认与账号相同" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="用于后续短信通知等" :prefix-icon="Phone" maxlength="11" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="0">男</el-radio>
            <el-radio :value="1">女</el-radio>
            <el-radio :value="2">保密</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="form.age" :min="1" :max="120" />
        </el-form-item>
        <el-form-item label="个人简介" prop="profile">
          <el-input v-model="form.profile" type="textarea" :rows="5" placeholder="请介绍您的传承领域、从艺经历等" />
        </el-form-item>
        <el-form-item label="认证材料" required>
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="onCertSuccess"
            :before-upload="beforeCertUpload"
          >
            <el-button type="primary" :icon="Document">上传证书/证明（图片或PDF）</el-button>
          </el-upload>
          <div v-if="certificateUrls.length" class="cert-list">
            <el-tag
              v-for="u in certificateUrls"
              :key="u"
              closable
              class="cert-tag"
              @close="removeCert(u)"
            >
              {{ u.slice(-20) }}
            </el-tag>
          </div>
        </el-form-item>
        <el-form-item label="验证码" prop="captcha">
          <div class="captcha-row">
            <el-input v-model="form.captcha" placeholder="验证码" :prefix-icon="Key" maxlength="6" />
            <button type="button" class="cap-btn" @click="refreshCaptcha">
              <img v-if="captchaDataUrl" :src="captchaDataUrl" alt="验证码" />
              <span v-else>加载中</span>
            </button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="submit-btn" @click="submit">提交审核</el-button>
          <el-button @click="router.push('/login')">返回登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 40px 16px;
  background: linear-gradient(145deg, #2c1810 0%, #5c2a1e 40%, #4a3428 100%);
}
.panel {
  max-width: 560px;
  margin: 0 auto;
  padding: 32px 28px 40px;
  background: #fffdf8;
  border-radius: 8px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.35);
}
.title {
  margin: 0 0 8px;
  font-size: 22px;
  color: #3d2914;
  text-align: center;
}
.sub {
  margin: 0 0 24px;
  font-size: 13px;
  color: #7a6a58;
  text-align: center;
  line-height: 1.5;
}
.captcha-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.cap-btn {
  width: 120px;
  height: 40px;
  border: 1px solid #dcd3c4;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  padding: 2px;
}
.cap-btn img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}
.submit-btn {
  min-width: 120px;
}
.cert-list {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.cert-tag {
  max-width: 100%;
}
</style>
