<template>
  <div class="profile-info">
    <h2>基本信息</h2>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="用户名">
        <el-input :value="form.username" disabled />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" />
      </el-form-item>

      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号" />
      </el-form-item>

      <el-form-item label="性别" prop="gender">
        <el-radio-group v-model="form.gender">
          <el-radio value="男">男</el-radio>
          <el-radio value="女">女</el-radio>
          <el-radio value="保密">保密</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="角色">
        <el-tag :type="roleTagType">{{ roleLabel }}</el-tag>
        <span v-if="isSellerPending" class="pending-tip">（审核中）</span>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          保存修改
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { updateUserInfo } from '@/api/modules/user';
import { useUserStore } from '@/stores/userStore';
import type { User } from '@/api/types/user';

const props = defineProps<{
  user: User | null;
}>();

const emit = defineEmits<{
  (e: 'update'): void;
}>();

const userStore = useUserStore();
const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive({
  username: '',
  email: '',
  phone: '',
  gender: '',
});

const rules: FormRules = {
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
};

const roleLabel = computed(() => {
  const map: Record<string, string> = {
    admin: '管理员',
    seller: '卖家',
    buyer: '买家',
  };
  return map[props.user?.role || ''] || '用户';
});

const roleTagType = computed(() => {
  const map: Record<string, 'danger' | 'success' | 'info'> = {
    admin: 'danger',
    seller: 'success',
    buyer: 'info',
  };
  return map[props.user?.role || ''] || 'info';
});

const isSellerPending = computed(() => {
  return props.user?.role === 'seller' && props.user?.examineState === '待审核';
});

// 同步 props 到 form
watch(
  () => props.user,
  (newVal) => {
    if (newVal) {
      form.username = newVal.username || '';
      form.email = newVal.email || '';
      form.phone = newVal.phone || '';
      form.gender = newVal.gender || '';
    }
  },
  { immediate: true }
);

const handleSubmit = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch {
    return;
  }

  loading.value = true;
  try {
    await updateUserInfo({
      email: form.email,
      phone: form.phone,
      gender: form.gender,
    });
    ElMessage.success('信息更新成功');
    await userStore.fetchUserInfo();
    emit('update');
  } catch (error) {
    ElMessage.error('更新失败');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.profile-info h2 {
  margin-top: 0;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.pending-tip {
  color: #e6a23c;
  font-size: 12px;
  margin-left: 8px;
}
</style>
