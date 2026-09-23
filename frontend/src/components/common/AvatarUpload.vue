<template>
  <div class="avatar-upload-container">
    <el-upload
      class="avatar-uploader"
      :action="uploadUrl"
      :headers="uploadHeaders"
      :show-file-list="false"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
    >
      <div class="avatar-wrapper">
        <img v-if="avatar" :src="avatar" class="avatar" />
        <el-icon v-else class="avatar-placeholder"><Plus /></el-icon>
      </div>
    </el-upload>
    <p class="upload-hint">点击上传头像 (支持 JPG/PNG/GIF，最大 5MB)</p>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from "@/stores/userStore";
import { ElMessage } from "element-plus";
import { Plus } from "@element-plus/icons-vue";

const userStore = useUserStore();

const props = defineProps<{
  avatar?: string | null;
}>();

const emit = defineEmits<{
  (e: "update", url: string): void;
}>();

// 上传地址（对应后端 POST /user/avatar，经 VITE_API_BASE_URL 前缀）
const uploadUrl = "/api/user/avatar";

// 上传请求头（携带 Token）
const uploadHeaders = {
  Authorization: `Bearer ${userStore.token}`,
};

const handleSuccess = (response: any) => {
  // ✅ 添加调试日志
  console.log("=== 头像上传响应 ===");
  console.log("完整响应:", response);
  console.log("code:", response.code);
  console.log("data:", response.data);

  if (response.code === 200) {
    const avatarUrl = response.data;
    console.log("头像 URL:", avatarUrl);
    emit("update", avatarUrl);
    ElMessage.success("头像上传成功");
  } else {
    ElMessage.error(response.message || "上传失败");
  }
};
const handleError = (error: any) => {
  ElMessage.error(error?.message || "上传失败，请重试");
};

const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith("image/");
  const isLt5M = file.size / 1024 / 1024 < 5;

  if (!isImage) {
    ElMessage.error("只能上传图片文件！");
    return false;
  }
  if (!isLt5M) {
    ElMessage.error("图片大小不能超过 5MB！");
    return false;
  }
  return true;
};
</script>

<style scoped>
.avatar-upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar-uploader {
  cursor: pointer;
}

.avatar-wrapper {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px dashed #ddd;
  transition: border-color 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.avatar-wrapper:hover {
  border-color: #409eff;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: 32px;
  color: #ccc;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
  margin: 0;
}
</style>
