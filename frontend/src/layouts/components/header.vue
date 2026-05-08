<script setup>
import { Fold, Expand, User } from "@element-plus/icons-vue";
import { useRouter } from "vue-router";
import { getImageUrl } from "../../utils/system";
import http from "../../utils/http";
import useUserStore from "../../stores/userStore";
import { nextTick, ref } from "vue";
import { ElMessage } from "element-plus";

const props = defineProps({
  isCollapse: {
    type: Boolean,
    required: true,
  },
});

const router = useRouter();
const userStore = useUserStore();

// 修改密码相关
const passwordVisible = ref(false);
const passwordForm = ref({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});
const passwordRules = {
  oldPassword: [{ required: true, message: "请输入原密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码长度不能小于6位", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请确认新密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
};

const passwordFormRef = ref(null);

const emit = defineEmits(["update:isCollapse"]);

const toggleCollapse = () => {
  emit("update:isCollapse", !props.isCollapse);
};

// 个人信息
const goPerson = () => {
  router.push("/person");
};

// 打开修改密码弹窗（nextTick 避免与下拉菜单同帧竞争；弹窗挂到 body 避免被侧栏/内容区裁切或叠层错误）
const showPasswordDialog = () => {
  passwordForm.value = {
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
  };
  nextTick(() => {
    passwordVisible.value = true;
  });
};

// 修改密码
const updatePassword = async () => {
  if (!passwordFormRef.value) return;

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const url =
          userStore.roleFlag === "inheritor"
            ? "/inheritor/updatePass"
            : "/admin/updatePass";
        const res = await http.post(url, {
          userId: userStore.userInfo.id,
          oldPass: passwordForm.value.oldPassword,
          password: passwordForm.value.newPassword,
        });

        if (res.code === 200) {
          ElMessage.success("密码修改成功，请重新登录");
          passwordVisible.value = false;
          // 退出登录
          localStorage.clear();
          await router.push("/login");
        }
      } catch (error) {
        console.error("修改密码失败:", error);
      }
    }
  });
};

// 退出登录
const logout = async () => {
  const res = await http.post("/logout");
  // 清除本地信息
  if (res.code === 200) {
    localStorage.clear();
    await router.push("/login");
  }
};
</script>

<template>
  <div class="header-container">
    <div class="left">
      <el-icon class="collapse-btn" @click="toggleCollapse">
        <Fold v-if="!isCollapse" />
        <Expand v-else />
      </el-icon>
    </div>
    <div class="right">
      <el-dropdown>
        <div class="avatar-container">
          <el-avatar :size="30" :src="getImageUrl(userStore.userInfo.avatar)" />
          <span class="username">{{ userStore.userInfo.nickName }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="goPerson">个人信息</el-dropdown-item>
            <el-dropdown-item @click="showPasswordDialog"
              >修改密码</el-dropdown-item
            >
            <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 修改密码弹窗：必须挂到 body，并提高 z-index，避免被内容区 backdrop-filter / 叠层压住 -->
    <el-dialog
      v-model="passwordVisible"
      title="修改密码"
      width="420px"
      align-center
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
      class="ich-password-dialog"
      :z-index="5000"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="passwordVisible = false">取消</el-button>
          <el-button type="primary" @click="updatePassword">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.header-container {
  height: 65px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  overflow: visible;
  position: relative;
  z-index: 10;

  .left {
    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      color: var(--el-text-color-primary);

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }

  .right {
    .avatar-container {
      display: flex;
      align-items: center;
      cursor: pointer;

      .role-tag {
        margin-right: 12px;
        font-size: 13px;
        height: 24px;
        padding: 0 12px;
        line-height: 22px;
        font-weight: bold;
        border: none;
        box-shadow: 0 2px 4px rgba(var(--el-color-danger-rgb), 0.2);
        text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
      }

      .username {
        margin-left: 8px;
        font-size: 14px;
        color: var(--el-text-color-primary);
      }
    }
  }
}

:deep(.el-form-item__content) {
  width: 100%;

  .el-input {
    width: 100%;
  }
}
</style>
