<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getOrderList, cancelOrder, confirmReceipt, rateOrder, payOrder, deleteOrder } from '@/api/modules/order';
import { getSellerFirstImages } from '@/api/modules/goods';
import { deleteReviewByOrder } from '@/api/modules/extra-modules';
import Pagination from '@/components/common/Pagination.vue';
import { formatPrice, formatDateTime } from '@/utils/format';
import type { Order, OrderQueryParams } from '@/api/types/order';

const router = useRouter();

const loading = ref(false);
const orderList = ref<Order[]>([]);
const total = ref(0);
const firstImageMap = ref<Record<number, string>>({});

// 订单商品图兜底：订单快照图 → 卖家首张发布图 → 默认占位
const DEFAULT_COVER =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300">' +
      '<rect width="100%" height="100%" fill="#f5f7fa"/>' +
      '<text x="50%" y="50%" font-size="20" fill="#c0c4cc" text-anchor="middle" dominant-baseline="middle">暂无图片</text>' +
      '</svg>'
  );

const orderCover = (order: Order): string => {
  if (order.img) return order.img;
  if (order.merchantId != null && firstImageMap.value[order.merchantId]) {
    return firstImageMap.value[order.merchantId];
  }
  return DEFAULT_COVER;
};

const query = ref<OrderQueryParams>({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined,
});

const statusOptions = [
  { label: '全部', value: '' },
  { label: '待付款', value: '待付款' },
  { label: '待发货', value: '待发货' },
  { label: '待收货', value: '待收货' },
  { label: '已完成', value: '已完成' },
  { label: '已取消', value: '已取消' },
  { label: '已评价', value: '已评价' },
];

const statusColors: Record<string, string> = {
  待付款: 'warning',
  待发货: 'info',
  待收货: 'primary',
  已完成: 'success',
  已取消: 'danger',
  已评价: 'success',
};

const statusLabels: Record<string, string> = {
  待付款: '待付款',
  待发货: '待发货',
  待收货: '待收货',
  已完成: '已完成',
  已取消: '已取消',
  已评价: '已评价',
};

const fetchOrders = async (resetPage = true) => {
  if (resetPage) {
    query.value.page = 1;
  }
  loading.value = true;
  try {
    const params = { ...query.value };
    if (params.status === '') {
      params.status = undefined;
    }
    const res = await getOrderList(params);
    orderList.value = res.data.list;
    total.value = res.data.total;
    await fetchFirstImageMap();
  } catch (error) {
    ElMessage.error('订单加载失败');
  } finally {
    loading.value = false;
  }
};

// 收集卖家(merchantId)批量获取各自首张发布图，用于订单缺图兜底
const fetchFirstImageMap = async () => {
  const merchantIds = Array.from(
    new Set(
      orderList.value
        .map((o) => o.merchantId)
        .filter((id): id is number => typeof id === 'number'),
    ),
  );
  if (merchantIds.length === 0) {
    firstImageMap.value = {};
    return;
  }
  try {
    const res = await getSellerFirstImages(merchantIds);
    firstImageMap.value = res.data || {};
  } catch {
    firstImageMap.value = {};
  }
};

const handleCancel = async (order: Order) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' });
  } catch {
    return;
  }
  await cancelOrder(order.orderId);
  ElMessage.success('订单已取消');
  fetchOrders(false);
};

const handlePay = async (order: Order) => {
  try {
    await ElMessageBox.confirm(`确认支付订单 "${order.orderNumber}" 吗？`, '确认支付', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'info',
    });
  } catch {
    return;
  }
  await payOrder(order.orderId);
  ElMessage.success('支付成功，等待卖家发货');
  fetchOrders(false);
};

const handleConfirm = async (order: Order) => {
  try {
    await ElMessageBox.confirm('确认已收到货物？确认后订单将变为已完成', '提示', { type: 'warning' });
  } catch {
    return;
  }
  await confirmReceipt(order.orderId, { starRating: 5 });
  ElMessage.success('确认收货成功');
  fetchOrders(false);
};

const rateForm = ref({ starRating: 5, remarks: '' });
const rateDialogVisible = ref(false);
const rateOrderId = ref<number | null>(null);

const openRate = (order: Order) => {
  rateOrderId.value = order.orderId;
  rateForm.value = { starRating: 5, remarks: '' };
  rateDialogVisible.value = true;
};

const submitRate = async () => {
  if (rateOrderId.value == null) return;
  try {
    await rateOrder(rateOrderId.value, { ...rateForm.value });
    ElMessage.success('评价成功');
    rateDialogVisible.value = false;
    fetchOrders(false);
  } catch (error) {
    // 错误处理由拦截器统一提示
  }
};

const handleDelete = async (order: Order) => {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？删除后不可恢复', '提示', { type: 'warning' });
  } catch {
    return;
  }
  await deleteOrder(order.orderId);
  ElMessage.success('删除成功');
  fetchOrders(false);
};

const handleDeleteReview = async (order: Order) => {
  try {
    await ElMessageBox.confirm('确定删除该评价吗？删除后不可恢复', '删除评价', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    });
  } catch {
    return;
  }
  try {
    await deleteReviewByOrder(order.orderId);
    ElMessage.success('评价已删除');
    fetchOrders(false);
  } catch (error) {
  }
};

const handleViewDetail = (order: Order) => {
  router.push(`/order/detail/${order.orderId}`);
};

const handleTabChange = () => {
  fetchOrders(true);
};

const onPageChange = (page: number) => {
  query.value.page = page;
  fetchOrders(false);
};

const onSizeChange = (size: number) => {
  query.value.size = size;
  fetchOrders(true);
};

onMounted(() => {
  fetchOrders(true);
});
</script>

<template>
  <div class="order-list-page">
    <div class="page-header">
      <h1>我的订单</h1>
    </div>

    <el-tabs v-model="query.status" class="status-tabs" @tab-change="handleTabChange">
      <el-tab-pane v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :name="opt.value" />
    </el-tabs>

    <div v-loading="loading" class="order-list">
      <el-card v-for="order in orderList" :key="order.orderId" class="order-card">
        <div class="order-header">
          <div class="order-info">
            <span class="order-no">订单号：{{ order.orderNumber }}</span>
            <span class="order-time">{{ formatDateTime(order.createTime) }}</span>
          </div>
          <el-tag :type="(statusColors[order.status] || 'info') as 'info' | 'primary' | 'success' | 'warning' | 'danger'" size="large" effect="dark">
            {{ statusLabels[order.status] || order.status }}
          </el-tag>
        </div>

        <div class="order-goods">
          <div v-for="item in [order]" :key="item.orderId" class="order-goods-item">
            <el-image :src="orderCover(item)" fit="cover" class="goods-img" />
            <div class="goods-detail">
              <p class="goods-title">{{ item.title }}</p>
              <p class="goods-specs">{{ item.norms || item.type }}</p>
              <p class="goods-price">{{ formatPrice(item.price) }} × {{ item.num }}</p>
            </div>
            <div class="goods-total">
              <span>{{ formatPrice(item.priceCount) }}</span>
            </div>
          </div>
        </div>

        <div class="order-footer">
          <div class="order-summary">
            <span>共 {{ order.num }} 件商品</span>
            <span class="total-amount">合计：{{ formatPrice(order.priceCount) }}</span>
          </div>
          <div class="order-actions">
            <el-button size="small" @click="handleViewDetail(order)">查看详情</el-button>
            <el-button v-if="order.status === '待付款'" size="small" type="danger" @click="handleCancel(order)">取消订单</el-button>
            <el-button v-if="order.status === '待付款'" size="small" type="primary" @click="handlePay(order)">去付款</el-button>
            <el-button v-if="order.status === '待收货'" size="small" type="success" @click="handleConfirm(order)">确认收货</el-button>
            <el-button v-if="order.status === '已完成'" size="small" type="warning" @click="openRate(order)">评价</el-button>
            <el-button v-if="order.status === '已评价'" size="small" type="danger" plain @click="handleDeleteReview(order)">删除评价</el-button>
            <el-button v-if="order.status === '已取消' || order.status === '已完成' || order.status === '已评价'" size="small" type="danger" @click="handleDelete(order)">删除订单</el-button>
          </div>
        </div>
      </el-card>

      <el-empty v-if="!loading && orderList.length === 0" description="暂无订单">
        <el-button type="primary" @click="router.push('/goods/list')">去逛逛</el-button>
      </el-empty>

      <Pagination
        v-if="total > 0"
        :total="total"
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :page-sizes="[10, 20, 50]"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </div>

    <el-dialog v-model="rateDialogVisible" title="评价订单" width="400px">
      <el-form label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="rateForm.starRating" />
        </el-form-item>
        <el-form-item label="评价">
          <el-input v-model="rateForm.remarks" type="textarea" :rows="3" placeholder="说点什么吧（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRate">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.order-list-page {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}
.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 20px;
}
.status-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
  border-bottom: 1px solid #e8eaec;
}
.status-tabs :deep(.el-tabs__nav) {
  padding: 0 10px;
}
.status-tabs :deep(.el-tabs__item) {
  padding: 12px 16px;
}
.order-card {
  margin-bottom: 16px;
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f5f7fa;
}
.order-info {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #909399;
}
.order-no {
  font-family: monospace;
}
.order-goods {
  padding: 16px 20px;
}
.order-goods-item {
  display: flex;
  gap: 12px;
}
.goods-img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
}
.goods-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.goods-title {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.goods-specs {
  margin: 0 0 4px;
  font-size: 12px;
  color: #909399;
}
.goods-price {
  margin: 0;
  font-size: 14px;
  color: #f56c6c;
}
.goods-total {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
  min-width: 120px;
  text-align: right;
}
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #fafafa;
  border-top: 1px solid #f5f7fa;
}
.order-summary {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 14px;
}
.total-amount {
  font-weight: 600;
  color: #f56c6c;
}
.order-actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 768px) {
  .order-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  .order-footer {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  .order-actions {
    justify-content: flex-end;
  }
}
</style>