<template>
  <div class="profile-container">
    <el-row :gutter="24">
      <!-- 侧边栏 -->
      <el-col :xs="24" :sm="8" :md="6">
        <el-card class="profile-sidebar">
          <div class="avatar-section">
            <AvatarUpload :avatar="userInfo?.avatar" @update="onAvatarUpdate" />
            <h3>{{ userInfo?.username || "用户" }}</h3>
            <el-tag :type="roleTagType">{{ roleLabel }}</el-tag>
          </div>

          <el-menu :default-active="activeTab" @select="handleTabChange">
            <el-menu-item index="info">
              <el-icon><User /></el-icon>
              <span>基本信息</span>
            </el-menu-item>
            <el-menu-item index="password">
              <el-icon><Lock /></el-icon>
              <span>修改密码</span>
            </el-menu-item>
            <el-menu-item index="address">
              <el-icon><Location /></el-icon>
              <span>收货地址</span>
            </el-menu-item>
            <el-menu-item index="orders" v-if="!isSeller">
              <el-icon><List /></el-icon>
              <span>我的订单</span>
            </el-menu-item>
            <el-menu-item index="goods" v-if="isSeller || isAdmin">
              <el-icon><Goods /></el-icon>
              <span>我的商品</span>
            </el-menu-item>
            <el-menu-item index="seller" v-if="isSeller || isAdmin">
              <el-icon><Shop /></el-icon>
              <span>卖家信息</span>
            </el-menu-item>
            <el-menu-item index="seller-orders" v-if="isSeller || isAdmin">
              <el-icon><List /></el-icon>
              <span>卖家订单</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 主内容 -->
      <el-col :xs="24" :sm="16" :md="18">
        <el-card class="profile-content">
          <!-- 基本信息 -->
          <div v-if="activeTab === 'info'">
            <ProfileInfo :user="userInfo" @update="fetchUserInfo" />
          </div>

          <!-- 修改密码 -->
          <div v-if="activeTab === 'password'">
            <ProfilePassword />
          </div>

          <!-- 收货地址 -->
          <div v-if="activeTab === 'address'">
            <ProfileAddress />
          </div>

          <!-- 我的订单 -->
          <div v-if="activeTab === 'orders'">
            <OrderList />
          </div>

          <!-- 我的商品 -->
          <div v-if="activeTab === 'goods'">
            <MyGoods />
          </div>

          <!-- 卖家信息 -->
          <div v-if="activeTab === 'seller'">
            <SellerInfo />
          </div>

          <!-- 卖家订单 -->
          <div v-if="activeTab === 'seller-orders'">
            <SellerOrderManage />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/userStore";
import { ElMessage } from "element-plus";
import { User, Lock, Location, List, Goods, Shop } from "@element-plus/icons-vue";
import ProfileInfo from "./components/ProfileInfo.vue";
import ProfilePassword from "./components/ProfilePassword.vue";
import ProfileAddress from "./components/ProfileAddress.vue";
import OrderList from "@/views/order/List.vue";
import MyGoods from "@/views/goods/MyGoods.vue";
import SellerInfo from "@/views/seller/Info.vue";
import SellerOrderManage from "@/views/seller/OrderManage.vue";
import AvatarUpload from "@/components/common/AvatarUpload.vue";

const router = useRouter();
const userStore = useUserStore();
const activeTab = ref("info");

const userInfo = computed(() => userStore.userInfo);
const isSeller = computed(() => userStore.isSeller);
const isAdmin = computed(() => userStore.isAdmin);
const isLoggedIn = computed(() => userStore.isLoggedIn);

const roleLabel = computed(() => {
  const map: Record<string, string> = {
    admin: "管理员",
    seller: "卖家",
    buyer: "买家",
  };
  return map[userInfo.value?.role || ""] || "用户";
});

const roleTagType = computed(() => {
  const map: Record<string, "danger" | "success" | "info"> = {
    admin: "danger",
    seller: "success",
    buyer: "info",
  };
  return map[userInfo.value?.role || ""] || "info";
});

const handleTabChange = (index: string) => {
  activeTab.value = index;
};

const onAvatarUpdate = (url: string) => {
  console.log("=== 头像更新回调 ===");
  console.log("新头像 URL:", url);

  if (userStore.userInfo) {
    // ✅ 更新本地状态
    userStore.userInfo.avatar = url;
    console.log("更新后的 userInfo:", userStore.userInfo);
  }
  // ✅ 重新获取用户信息（确保数据库同步）
  userStore.fetchUserInfo();
};

const fetchUserInfo = async () => {
  await userStore.fetchUserInfo();
};

onMounted(() => {
  if (!isLoggedIn.value) {
    ElMessage.warning("请先登录");
    router.push("/login");
  }
});
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: 80vh;
}

.profile-sidebar {
  position: sticky;
  top: 20px;
}

.avatar-section {
  text-align: center;
  padding: 20px 0 16px;
  border-bottom: 1px solid #eee;
  margin-bottom: 16px;
}

.avatar-uploader {
  display: block;
  width: 120px;
  height: 120px;
  margin: 0 auto 16px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 3px dashed #ddd;
  transition: border-color 0.3s;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  font-size: 48px;
  color: #ccc;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.avatar-section h3 {
  margin: 8px 0;
}

.profile-content {
  min-height: 400px;
}
</style>
