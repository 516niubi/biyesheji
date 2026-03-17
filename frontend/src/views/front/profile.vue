<script setup>
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { Picture as IconPicture } from "@element-plus/icons-vue";
import Config from "../../config";
import { genderDict, } from "../../config/dict";
import http from "../../utils/http";
import useUserStore from "../../stores/userStore";
import { getImageUrl } from "../../utils/system";

const userStore = useUserStore();
const form = ref({
  username: "",
  nickName: "",
  age: 18,
  gender: 0,
  avatar: "",
  profile: "",
  roleFlag: "",
});

// 图片上传地址
const uploadUrl = ref(`${Config.baseUrl}/file/upload`);
// 文件上传携带的数据
const uploadData = {
  authorization: `${localStorage.getItem("token")}`,
};
// 图片上传后的回调地址
const imageUrl = ref("");

// 获取用户信息
const getUserInfo = async () => {
  try {
    const res = await http.get("/user/current");
    if (res.code === 200) {
      form.value = res.data;
      imageUrl.value = form.value.avatar ? getImageUrl(form.value.avatar) : "";
      userStore.userInfo = res.data;
    }
  } catch (error) {
    console.error("获取用户信息失败:", error);
  }
};

// 保存用户信息
const saveUserInfo = async () => {
  try {
    const res = await http.post("/user/edit", form.value);
    if (res.code === 200) {
      ElMessage.success("保存成功");
      await getUserInfo();
    } else {
      ElMessage.error("保存失败");
    }
  } catch (error) {
    console.error("保存用户信息失败:", error);
    ElMessage.error("保存失败");
  }
};

/** 文件上传前的钩子函数 */
const beforeAvatarUpload = (rawFile) => {
  if (!rawFile.type.includes("image")) {
    ElMessage.error("文件类型错误，只允许上传图片文件");
    return false;
  } else if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error("图片超过2MB限制");
    return false;
  }
  return true;
};

/** 文件上传成功回调 */
const handleAvatarSuccess = (response) => {
  if (response?.code === 200) {
    form.value.avatar = response.data;
    imageUrl.value = getImageUrl(response.data);
  }
};

onMounted(() => {
  getUserInfo();
});
</script>

<template>
  <div class="person-container">
    <!-- 头像上传区域 -->
    <div class="avatar-section">
      <el-upload
        class="avatar-uploader"
        :action="uploadUrl"
        :headers="uploadData"
        :show-file-list="false"
        :on-success="handleAvatarSuccess"
        :before-upload="beforeAvatarUpload"
      >
        <el-image v-if="imageUrl" :src="imageUrl" fit="cover" class="avatar">
          <template #error>
            <div class="image-slot">
              <el-icon><icon-picture /></el-icon>
            </div>
          </template>
        </el-image>
        <el-icon v-else class="avatar-uploader-icon">
          <icon-picture />
        </el-icon>
      </el-upload>
      <div class="upload-tip">
        点击上传头像，只能上传jpg/png文件，且不超过2MB
      </div>
    </div>

    <!-- 表单区域 -->
    <el-form :model="form" label-width="120px" class="form-section">
      <el-form-item label="账号">
        <el-input v-model="form.username" disabled />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="form.nickName" placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="年龄">
        <el-input-number v-model="form.age" :min="1" :max="120" />
      </el-form-item>
      <el-form-item label="性别">
        <el-radio-group v-model="form.gender">
          <el-radio
            v-for="(label, value) in genderDict"
            :key="value"
            :label="Number(value)"
            >{{ label }}</el-radio
          >
        </el-radio-group>
      </el-form-item>
      <el-form-item label="个人介绍">
        <el-input
          type="textarea"
          v-model="form.profile"
          :autosize="{ minRows: 3, maxRows: 6 }"
          placeholder="请输入个人介绍"
        />
      </el-form-item>
    </el-form>

    <!-- 保存按钮 -->
    <div class="button-section">
      <el-button type="primary" @click="saveUserInfo">保存修改</el-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.person-container {
  padding: 20px;
  width: 100%;
  max-width: 600px;
  margin: 0 auto;

  .avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 40px;
  }

  .avatar-uploader {
    width: 178px;
    height: 178px;
    border: 1px dashed var(--el-border-color);
    border-radius: 50%;
    cursor: pointer;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);

    &:hover {
      border-color: var(--el-color-primary);
    }

    .avatar {
      width: 178px;
      height: 178px;
      display: block;
    }
  }

  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    text-align: center;
    line-height: 178px;
  }

  .upload-tip {
    font-size: 12px;
    color: #999;
    margin-top: 8px;
    line-height: 1.4;
    text-align: center;
  }

  .image-slot {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    background: #f5f7fa;
    color: #909399;
  }

  .form-section {
    margin-bottom: 30px;

    :deep(.el-form-item__content) {
      width: 100%;
      max-width: 400px;
    }

    :deep(.el-input),
    :deep(.el-textarea) {
      width: 100%;
    }
  }

  .button-section {
    text-align: center;
  }
}
</style>
