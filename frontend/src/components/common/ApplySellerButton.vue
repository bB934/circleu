<template>
  <el-button
    v-if="showButton"
    type="primary"
    :size="size"
    @click="handleClick"
  >
    <el-icon><User /></el-icon>
    <span>申请成为卖家</span>
  </el-button>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/userStore';
import { ElMessage } from 'element-plus';
import { User } from '@element-plus/icons-vue';

const props = withDefaults(defineProps<{
  size?: 'large' | 'default' | 'small';
}>(), {
  size: 'default',
});

const router = useRouter();
const userStore = useUserStore();

const showButton = computed(() => {
  return userStore.isLoggedIn && !userStore.isSeller && !userStore.isAdmin;
});

const handleClick = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  router.push('/apply-seller');
};
</script>
