<template>
  <div class="seller-order-manage">
    <el-card>
      <template #header>
        <div class="header">
          <h2>卖家订单管理</h2>
          <div class="search">
            <el-select v-model="filterStatus" placeholder="订单状态" clearable @change="fetchData">
              <el-option label="待发货" value="待发货" />
              <el-option label="待收货" value="待收货" />
              <el-option label="已完成" value="已完成" />
              <el-option label="已评价" value="已评价" />
              <el-option label="已取消" value="已取消" />
            </el-select>
            <el-button type="primary" @click="fetchData">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="orderNumber" label="订单号" width="160" />
        <el-table-column prop="title" label="商品" min-width="120" show-overflow-tooltip />
        <el-table-column label="单价" width="100">
          <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="num" label="数量" width="70" />
        <el-table-column label="总价" width="100">
          <template #default="{ row }">¥{{ row.priceCount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="contactName" label="收货人" width="90" />
        <el-table-column prop="contactPhone" label="手机号" width="120" />
        <el-table-column prop="contactAddress" label="收货地址" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === '待发货'"
              type="success"
              size="small"
              @click="handleShip(row)"
            >
              发货
            </el-button>
            <span v-else class="no-action">—</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchData"
        @size-change="fetchData"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getSellerOrderList, shipOrder } from '@/api/modules/order';
import type { Order } from '@/api/types/order';

const list = ref<Order[]>([]);
const loading = ref(false);
const total = ref(0);
const page = ref(1);
const size = ref(10);
const filterStatus = ref('');

const getStatusType = (status: string) => {
  const map: Record<string, 'warning' | 'info' | 'primary' | 'success' | 'danger'> = {
    待付款: 'warning',
    待发货: 'info',
    待收货: 'primary',
    已完成: 'success',
    已评价: 'success',
    已取消: 'danger',
  };
  return map[status as keyof typeof map] || 'info';
};

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    待付款: '待付款',
    待发货: '待发货',
    待收货: '待收货',
    已完成: '已完成',
    已评价: '已评价',
    已取消: '已取消',
  };
  return map[status] || status;
};

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getSellerOrderList({
      status: filterStatus.value || undefined,
      page: page.value,
      size: size.value,
    });
    list.value = res.data?.list || [];
    total.value = res.data?.total || 0;
  } catch (error) {
    ElMessage.error('加载订单失败');
  } finally {
    loading.value = false;
  }
};

const resetSearch = () => {
  filterStatus.value = '';
  page.value = 1;
  fetchData();
};

const handleShip = async (row: Order) => {
  try {
    await ElMessageBox.confirm(
      `确认对订单 "${row.orderNumber}" 发货吗？`,
      '确认发货',
      { type: 'warning', confirmButtonText: '确认发货', cancelButtonText: '取消' }
    );
    await shipOrder(row.orderId);
    ElMessage.success('发货成功，等待买家收货');
    fetchData();
  } catch (error) {
    // 用户取消
  }
};

onMounted(fetchData);
</script>

<style scoped>
.seller-order-manage {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.header h2 {
  margin: 0;
  font-size: 18px;
}

.search {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.no-action {
  color: #c0c4cc;
}
</style>
