<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { useUserStore } from '@/stores/userStore';
import type { LoginParams } from '@/api/types/user';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const formRef = ref<FormInstance>();
const loading = ref(false);
const loginForm = reactive<LoginParams>({
  username: '',
  password: '',
});

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch {
    return;
  }

  loading.value = true;
  try {
    await userStore.login(loginForm);
    ElMessage.success('登录成功');
    // 管理员登录后跳转后台；其余情况跳转原 redirect（无则首页）
    const redirect = route.query.redirect as string;
    if (userStore.isAdmin) {
      router.push(redirect && redirect !== '/' ? redirect : '/admin');
    } else {
      router.push(redirect || '/');
    }
  } catch (error) {
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>校园二手交易平台</h2>
      </template>
      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit" block>
            登录
          </el-button>
        </el-form-item>
        <el-form-item>
          <div class="register-link">
            <span>还没有账号？</span>
            <router-link to="/register">立即注册</router-link>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}
.login-card {
  width: 100%;
  max-width: 420px;
}
.login-card :deep(.el-card__header) {
  padding-bottom: 16px;
  border-bottom: 1px solid #e8eaec;
}
.login-card h2 {
  margin: 0;
  text-align: center;
  color: #303133;
  font-weight: 600;
}
.register-link {
  text-align: center;
  color: #909399;
  font-size: 14px;
}
.register-link a {
  color: #409eff;
  margin-left: 8px;
  text-decoration: none;
}
.register-link a:hover {
  text-decoration: underline;
}
</style>