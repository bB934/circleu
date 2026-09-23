<script setup lang="ts">
import { onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useCartStore } from '@/stores/cartStore';
import { formatPrice } from '@/utils/format';
import type { CartItem } from '@/api/types/cart';

const router = useRouter();
const cartStore = useCartStore();

const selectedAll = computed({
  get: () => cartStore.items.length > 0 && cartStore.items.every(item => item.selected),
  set: (val) => cartStore.selectAll(val),
});

const handleUpdateQuantity = async (item: CartItem, num: number | undefined) => {
  if (!num) return;
  if (num < 1) num = 1;
  if (num > (item.stock || 999)) {
    ElMessage.warning(`库存不足，最多可买 ${item.stock || 999} 件`);
    return;
  }
  await cartStore.updateCartItem(item.cartId, { num });
};

const handleRemove = async (cartId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', { type: 'warning' });
  } catch {
    return;
  }
  await cartStore.removeCartItem(cartId);
  ElMessage.success('删除成功');
};

const handleClearSelected = async () => {
  const selected = cartStore.selectedItems;
  if (selected.length === 0) {
    ElMessage.warning('请先选择商品');
    return;
  }
  try {
    await ElMessageBox.confirm('确定要删除选中的商品吗？', '提示', { type: 'warning' });
  } catch {
    return;
  }
  // 逐个删除选中的商品
  for (const item of selected) {
    await cartStore.removeCartItem(item.cartId);
  }
  ElMessage.success('删除成功');
};

const goToCheckout = () => {
  const selected = cartStore.selectedItems;
  if (selected.length === 0) {
    ElMessage.warning('请选择要结算的商品');
    return;
  }
  router.push('/order/confirm');
};

onMounted(() => {
  cartStore.fetchCartList();
});
</script>

<template>
  <div class="cart-page">
    <div class="page-header">
      <h1>购物车</h1>
    </div>

    <div v-loading="cartStore.loading" class="cart-content">
      <el-table v-if="cartStore.items.length > 0" :data="cartStore.items" border style="width: 100%" row-key="cartId">
        <el-table-column type="selection" width="55" :reserve-selection="true" />
        <el-table-column prop="title" label="商品" width="300">
          <template #default="scope">
            <div class="goods-cell">
              <el-image :src="scope.row.img" fit="cover" class="goods-img" />
              <div class="goods-info">
                <p class="goods-title">{{ scope.row.title }}</p>
                <p class="goods-price">{{ formatPrice(scope.row.price) }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="scope">
            <span>{{ formatPrice(scope.row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="180">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.num"
              :min="1"
              :max="scope.row.stock || 999"
              :controls="false"
              @change="handleUpdateQuantity(scope.row as CartItem, $event)"
              style="width: 100px"
            />
            <span v-if="scope.row.stock" class="stock-tip">库存：{{ scope.row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="scope">
            <span class="subtotal">{{ formatPrice(scope.row.price * scope.row.num) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button size="small" type="danger" link @click="handleRemove(scope.row.cartId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-else description="购物车空空如也" image="https://picsum.photos/200/200?random=cart">
        <el-button type="primary" @click="router.push('/goods/list')">去逛逛</el-button>
      </el-empty>

      <div class="cart-footer" v-if="cartStore.items.length > 0">
        <div class="footer-left">
          <el-checkbox v-model="selectedAll" label="全选" />
          <el-button type="danger" @click="handleClearSelected" :disabled="cartStore.selectedItems.length === 0">删除选中</el-button>
        </div>
        <div class="footer-right">
          <div class="price-summary">
            <span>合计：</span>
            <span class="total-price">{{ formatPrice(cartStore.totalPrice) }}</span>
          </div>
          <el-button type="primary" size="large" @click="goToCheckout" :disabled="cartStore.selectedItems.length === 0" :loading="cartStore.loading">
            去结算 ({{ cartStore.selectedItems.length }})
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cart-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 20px;
}
.goods-cell {
  display: flex;
  gap: 12px;
  align-items: center;
}
.goods-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
}
.goods-title {
  margin: 0 0 4px;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}
.goods-price {
  margin: 0;
  font-size: 13px;
  color: #f56c6c;
}
.stock-tip {
  display: block;
  margin-top: 4px;
  font-size: 11px;
  color: #909399;
}
.subtotal {
  font-weight: 600;
  color: #f56c6c;
  font-size: 15px;
}
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  margin-top: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.footer-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.price-summary {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 16px;
}
.total-price {
  font-size: 24px;
  font-weight: 700;
  color: #f56c6c;
}
.footer-right {
  display: flex;
  align-items: center;
  gap: 24px;
}
.footer-right .el-button {
  height: 48px;
  padding: 0 32px;
  font-size: 16px;
}

@media (max-width: 768px) {
  .cart-footer {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  .footer-right {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>