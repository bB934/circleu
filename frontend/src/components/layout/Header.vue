<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/userStore';
import { useCartStore } from '@/stores/cartStore';
import { ElMessage } from 'element-plus';
import { ShoppingCart, ShoppingBag, ArrowDown } from '@element-plus/icons-vue';
import ApplySellerButton from '@/components/common/ApplySellerButton.vue';

const router = useRouter();
const userStore = useUserStore();
const cartStore = useCartStore();

const logout = async () => {
  userStore.logout();
  ElMessage.success('已退出登录');
  router.push('/');
};

const goToPublish = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  // 角色以服务端为准，先刷新最新 userInfo（审核通过后本地可能仍是旧角色）
  if (!userStore.userInfo) {
    await userStore.fetchUserInfo();
  }
  if (!userStore.isSeller && !userStore.isAdmin) {
    ElMessage.warning('请先申请成为卖家');
    router.push('/user/profile');
    return;
  }
  router.push('/goods/publish');
};
</script>

<template>
  <el-header class="app-header">
    <div class="header-content">
      <router-link to="/" class="logo">
        <el-icon><ShoppingBag /></el-icon>
        <span>校园二手</span>
      </router-link>

        <el-menu mode="horizontal" class="nav-menu" router>
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/goods/list">商品广场</el-menu-item>
          <el-menu-item v-if="userStore.isLoggedIn" index="/order/list">我的订单</el-menu-item>
          <el-menu-item index="/goods/publish" :disabled="!userStore.isLoggedIn">发布商品</el-menu-item>
        </el-menu>

      <div class="header-actions">
        <el-dropdown v-if="!userStore.isLoggedIn">
          <span class="dropdown-link">
            <el-button type="text" @click="router.push('/login')">登录</el-button>
            <el-button type="primary" @click="router.push('/register')">注册</el-button>
          </span>
        </el-dropdown>

        <el-dropdown v-else trigger="click">
          <span class="user-avatar-wrapper">
            <el-avatar :size="32" :src="userStore.userInfo?.avatar || ''" />
            <span class="username">{{ userStore.userInfo?.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item icon="User" @click="router.push('/user/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item icon="ShoppingCart" @click="router.push('/cart')" v-if="userStore.isBuyer">购物车</el-dropdown-item>
              <el-dropdown-item icon="List" @click="router.push('/order/list')" v-if="userStore.isBuyer">我的订单</el-dropdown-item>
              <el-dropdown-item icon="Goods" @click="goToPublish" v-if="userStore.isSeller || userStore.isAdmin">发布商品</el-dropdown-item>
              <el-dropdown-item icon="Setting" @click="router.push('/admin')" v-if="userStore.isAdmin">后台管理</el-dropdown-item>
              <el-dropdown-item divided icon="SwitchButton" @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <ApplySellerButton v-if="userStore.isLoggedIn && !userStore.isSeller && !userStore.isAdmin" size="small" />

        <el-dropdown v-if="userStore.isLoggedIn">
          <el-button type="text" @click="router.push('/cart')">
            <el-icon><ShoppingCart /></el-icon>
            <sup v-if="cartStore.totalCount > 0" class="cart-badge">{{ cartStore.totalCount }}</sup>
          </el-button>
        </el-dropdown>
      </div>
    </div>
  </el-header>
</template>

<style scoped>
.app-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: #409eff;
  text-decoration: none;
}
.logo i {
  font-size: 24px;
}
.nav-menu {
  flex: 1;
  margin: 0 40px;
  border: none;
  height: auto;
}
.nav-menu .el-menu-item {
  height: 40px;
  line-height: 40px;
  padding: 0 16px;
  font-size: 14px;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}
.dropdown-link {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background 0.2s;
}
.user-avatar-wrapper:hover {
  background: #f5f7fa;
}
.username {
  font-size: 14px;
  color: #303133;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cart-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background: #f56c6c;
  color: #fff;
  font-size: 10px;
  padding: 0 4px;
  border-radius: 10px;
  min-width: 16px;
  text-align: center;
}
</style>