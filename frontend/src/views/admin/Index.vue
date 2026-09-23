<template>
  <div class="admin-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in stats" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ background: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20" class="quick-actions">
      <el-col :span="8" v-for="action in actions" :key="action.path">
        <el-card
          shadow="hover"
          class="action-card"
          @click="router.push(action.path)"
        >
          <el-icon :size="28"><component :is="action.icon" /></el-icon>
          <span>{{ action.label }}</span>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import {
  User,
  Goods,
  ShoppingCart,
  Warning,
  ArrowRight,
  Stamp,
} from "@element-plus/icons-vue";
import { getAdminStats } from "@/api/modules/admin";
import type { AdminStats } from "@/api/modules/admin";

const router = useRouter();

const statsData = ref<AdminStats | null>(null);

const stats = ref([
  { key: "users", label: "总用户", icon: User, color: "#409eff", value: 0 },
  { key: "goods", label: "总商品", icon: Goods, color: "#67c23a", value: 0 },
  {
    key: "orders",
    label: "总订单",
    icon: ShoppingCart,
    color: "#e6a23c",
    value: 0,
  },
  {
    key: "pending",
    label: "待处理",
    icon: Warning,
    color: "#f56c6c",
    value: 0,
  },
]);

const actions = [
  { label: "用户管理", icon: User, path: "/admin/users" },
  { label: "商品管理", icon: Goods, path: "/admin/goods" },
  { label: "订单管理", icon: ShoppingCart, path: "/admin/orders" },
  { label: "卖家审核", icon: Stamp, path: "/admin/seller-applications" },
];

const fetchStats = async () => {
  try {
    const res = await getAdminStats();
    statsData.value = res.data;
    stats.value[0].value = res.data.totalUsers || 0;
    stats.value[1].value = res.data.totalGoods || 0;
    stats.value[2].value = res.data.totalOrders || 0;
    // 后端统计无 pendingUsers，待处理 = 待审核商品 + 待处理订单
    stats.value[3].value =
      (res.data.pendingGoods || 0) + (res.data.pendingOrders || 0);
  } catch (error) {
    console.error("获取统计数据失败:", error);
  }
};

onMounted(fetchStats);
</script>

<style scoped>
.admin-container {
  padding: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-content {
  margin-left: 16px;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.quick-actions {
  margin-top: 20px;
}

.action-card {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.action-card .el-icon:first-child {
  color: #409eff;
  margin-right: 12px;
}

.action-card span {
  flex: 1;
  font-size: 16px;
  font-weight: 500;
}

.action-card .arrow {
  color: #c0c4cc;
}
</style>
