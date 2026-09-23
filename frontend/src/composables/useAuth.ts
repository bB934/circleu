import { computed } from 'vue';
import { useUserStore } from '@/stores/userStore';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

export function useAuth() {
  const userStore = useUserStore();
  const router = useRouter();

  const isLoggedIn = computed(() => userStore.isLoggedIn);
  const isAdmin = computed(() => userStore.isAdmin);
  const isSeller = computed(() => userStore.isSeller);
  const userInfo = computed(() => userStore.userInfo);

  const requireAuth = (redirectPath?: string) => {
    if (!isLoggedIn.value) {
      ElMessage.warning('请先登录');
      router.push({ path: '/login', query: { redirect: redirectPath || router.currentRoute.value.fullPath } });
      return false;
    }
    return true;
  };

  const requireRole = (role: 'admin' | 'seller') => {
    if (!isLoggedIn.value) {
      ElMessage.warning('请先登录');
      router.push('/login');
      return false;
    }
    if (role === 'admin' && !isAdmin.value) {
      ElMessage.warning('需要管理员权限');
      router.push('/');
      return false;
    }
    if (role === 'seller' && !isSeller.value && !isAdmin.value) {
      ElMessage.warning('需要卖家权限');
      router.push('/');
      return false;
    }
    return true;
  };

  return {
    isLoggedIn,
    isAdmin,
    isSeller,
    userInfo,
    requireAuth,
    requireRole,
  };
}