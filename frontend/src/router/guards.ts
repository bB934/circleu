import router from './index';
import { useUserStore } from '@/stores/userStore';
import { ElMessage } from 'element-plus';

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore();

  if (to.meta.auth) {
    if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录');
      next({ path: '/login', query: { redirect: to.fullPath } });
      return;
    }

    // 已登录；角色以服务端为准：进入需要角色鉴权的页面前，
    // 始终重新拉取最新 userInfo（避免审核通过后本地 userInfo 仍是旧角色而误判）
    if (to.meta.role) {
      await userStore.fetchUserInfo();
    }

    if (to.meta.role) {
      const requiredRole = to.meta.role as string;
      const userRole = userStore.userInfo?.role;
      if (requiredRole === 'admin' && userRole !== 'admin') {
        ElMessage.warning('需要管理员权限');
        next('/');
        return;
      }
      if (requiredRole === 'seller' && userRole !== 'seller' && userRole !== 'admin') {
        ElMessage.warning('需要卖家权限');
        next('/');
        return;
      }
    }
  }

  if (to.meta.guest && userStore.isLoggedIn) {
    next('/');
    return;
  }

  next();
});