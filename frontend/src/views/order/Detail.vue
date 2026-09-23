<template>
  <div class="order-detail-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="header">
          <h2>订单详情</h2>
          <el-button @click="router.back()">返回</el-button>
        </div>
      </template>

      <div v-if="order">
        <!-- 订单状态 -->
        <div class="status-section">
          <el-steps :active="stepIndex" finish-status="success" align-center>
            <el-step title="下单" />
            <el-step title="付款" />
            <el-step title="发货" />
            <el-step title="收货" />
          </el-steps>
          <div class="status-info">
            <el-tag :type="statusColors[order.status] || 'info'" size="large">
              当前状态：{{ statusLabels[order.status] || order.status }}
            </el-tag>
            <span v-if="order.status === 'cancelled'" class="cancelled-tip">
              该订单已取消
            </span>
          </div>
        </div>

        <!-- 订单信息 -->
        <el-descriptions :column="2" border class="block">
          <el-descriptions-item label="订单编号">{{ order.orderNumber }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatDateTime(order.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ statusLabels[order.status] || order.status }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">在线支付</el-descriptions-item>
        </el-descriptions>

        <!-- 商品信息 -->
        <el-divider>商品信息</el-divider>
        <div class="goods-section">
          <div class="goods-item">
            <el-image v-if="order.img" :src="order.img" fit="cover" class="goods-image" />
            <div v-else class="goods-image placeholder">无图</div>
            <div class="goods-info">
              <div class="goods-title">{{ order.title }}</div>
              <div class="goods-specs">{{ order.norms || order.type || '无规格' }}</div>
              <div class="goods-price">单价：{{ formatPrice(order.price) }}</div>
              <div class="goods-quantity">数量：{{ order.num }}</div>
            </div>
            <div class="goods-total">
              <span class="total-label">实付：</span>
              <span class="total-price">{{ formatPrice(order.priceCount) }}</span>
            </div>
          </div>
        </div>

        <!-- 收货信息 -->
        <el-divider>收货信息</el-divider>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="收货人">{{ order.contactName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ order.contactPhone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="收货地址">{{ order.contactAddress || '—' }}</el-descriptions-item>
          <el-descriptions-item label="邮政编码">{{ order.postalCode || '无' }}</el-descriptions-item>
          <el-descriptions-item label="订单备注">{{ order.description || '无' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 操作按钮 -->
        <div class="actions">
          <template v-if="order.status === '待付款'">
            <el-button type="primary" size="large" @click="handlePay">
              立即支付
            </el-button>
            <el-button size="large" @click="handleCancel">
              取消订单
            </el-button>
          </template>

          <template v-if="order.status === '待收货'">
            <el-button type="success" size="large" @click="handleConfirm">
              确认收货
            </el-button>
          </template>

          <template v-if="order.status === '已完成'">
            <el-button type="warning" size="large" @click="openRate">
              评价订单
            </el-button>
          </template>

          <template v-if="order.status === '已取消' || order.status === '已完成' || order.status === '已评价'">
            <el-button type="danger" plain @click="handleDelete">
              删除订单
            </el-button>
          </template>
        </div>
      </div>

      <el-empty v-else-if="!loading" description="订单不存在" />
    </el-card>

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

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  getOrderDetail,
  payOrder,
  cancelOrder,
  confirmReceipt,
  rateOrder,
  deleteOrder,
} from '@/api/modules/order';
import { formatPrice, formatDateTime } from '@/utils/format';
import type { Order } from '@/api/types/order';

const router = useRouter();
const route = useRoute();
const orderId = Number(route.params.id || route.query.id);

const order = ref<Order | null>(null);
const loading = ref(false);

const statusLabels: Record<string, string> = {
  待付款: '待付款',
  待发货: '待发货',
  待收货: '待收货',
  已完成: '已完成',
  已评价: '已评价',
  已取消: '已取消',
};

const statusColors: Record<string, 'warning' | 'info' | 'primary' | 'success' | 'danger'> = {
  待付款: 'warning',
  待发货: 'info',
  待收货: 'primary',
  已完成: 'success',
  已评价: 'success',
  已取消: 'danger',
};

const statusStepIndex: Record<string, number> = {
  待付款: 0,
  待发货: 1,
  待收货: 2,
  已完成: 3,
  已评价: 3,
  已取消: 0,
};

const stepIndex = computed(() => statusStepIndex[order.value?.status || ''] ?? 0);

const fetchDetail = async () => {
  if (!orderId) {
    ElMessage.error('订单参数缺失');
    router.back();
    return;
  }
  loading.value = true;
  try {
    const res = await getOrderDetail(orderId);
    order.value = res.data;
  } catch (error) {
    ElMessage.error('加载订单详情失败');
    router.back();
  } finally {
    loading.value = false;
  }
};

const handlePay = async () => {
  try {
    await ElMessageBox.confirm('确认支付该订单吗？', '确认支付', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'info',
    });
    await payOrder(orderId);
    ElMessage.success('支付成功，等待卖家发货');
    await fetchDetail();
  } catch (error) {
    // 用户取消
  }
};

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确认取消该订单吗？', '确认取消', {
      confirmButtonText: '确认取消',
      cancelButtonText: '返回',
      type: 'warning',
    });
    await cancelOrder(orderId);
    ElMessage.success('订单已取消');
    await fetchDetail();
  } catch (error) {
    // 用户取消
  }
};

const handleConfirm = async () => {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？确认后订单将变为已完成', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'success',
    });
    await confirmReceipt(orderId, { starRating: 5 });
    ElMessage.success('确认收货成功');
    await fetchDetail();
  } catch (error) {
    // 用户取消
  }
};

const rateForm = ref({ starRating: 5, remarks: '' });
const rateDialogVisible = ref(false);

const openRate = () => {
  rateForm.value = { starRating: 5, remarks: '' };
  rateDialogVisible.value = true;
};

const submitRate = async () => {
  try {
    await rateOrder(orderId, { ...rateForm.value });
    ElMessage.success('评价成功');
    rateDialogVisible.value = false;
    await fetchDetail();
  } catch (error) {
    // 错误处理由拦截器统一提示
  }
};

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确认删除该订单吗？删除后不可恢复', '确认删除', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await deleteOrder(orderId);
    ElMessage.success('订单已删除');
    router.push('/order/list');
  } catch (error) {
    // 用户取消
  }
};

onMounted(fetchDetail);
</script>

<style scoped>
.order-detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h2 {
  margin: 0;
}

.status-section {
  margin-bottom: 24px;
}

.status-info {
  margin-top: 16px;
  text-align: center;
}

.cancelled-tip {
  margin-left: 12px;
  color: #f56c6c;
  font-size: 13px;
}

.block {
  margin-top: 8px;
}

.goods-section {
  padding: 12px 0;
}

.goods-item {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.goods-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  flex-shrink: 0;
}

.goods-image.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 12px;
}

.goods-info {
  flex: 1;
}

.goods-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.goods-specs {
  font-size: 13px;
  color: #909399;
  margin: 4px 0;
}

.goods-price {
  font-size: 14px;
  color: #909399;
}

.goods-quantity {
  font-size: 13px;
  color: #909399;
}

.goods-total {
  text-align: right;
  padding-left: 16px;
  border-left: 1px solid #ebeef5;
  min-width: 120px;
}

.total-label {
  font-size: 13px;
  color: #909399;
}

.total-price {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}

.actions {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}
</style>
