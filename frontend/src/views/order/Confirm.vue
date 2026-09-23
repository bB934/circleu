<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useCartStore } from '@/stores/cartStore';
import { getAddressList } from '@/api/modules/user';
import { getGoodsDetail } from '@/api/modules/goods';
import { addToCart } from '@/api/modules/cart';
import { createOrder } from '@/api/modules/order';
import type { Address } from '@/api/types/user';
import type { CartItem } from '@/api/types/cart';
import type { Goods } from '@/api/types/goods';
import { formatPrice } from '@/utils/format';

const DEFAULT_COVER =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300">' +
      '<rect width="100%" height="100%" fill="#f5f7fa"/>' +
      '<text x="50%" y="50%" font-size="20" fill="#c0c4cc" text-anchor="middle" dominant-baseline="middle">暂无图片</text>' +
      '</svg>'
  );
const router = useRouter();
const route = useRoute();
const cartStore = useCartStore();

const loading = ref(false);
const addresses = ref<Address[]>([]);
const selectedAddressId = ref<number | undefined>(undefined);
const selectedAddress = computed(() => addresses.value.find(a => a.addressId === selectedAddressId.value) || null);
const remark = ref('');
const goodsId = route.query.goodsId ? Number(route.query.goodsId) : null;
const buyNum = route.query.num ? Number(route.query.num) : 1;
const buyNowItem = ref<CartItem | null>(null);

const isBuyNow = computed(() => goodsId !== null);

const selectedItems = computed<CartItem[]>(() => {
  if (isBuyNow.value && buyNowItem.value) {
    return [buyNowItem.value];
  }
  return cartStore.selectedItems;
});

const totalPrice = computed(() => {
  return selectedItems.value.reduce((sum, item) => sum + item.price * item.num, 0);
});

const totalCount = computed(() => {
  return selectedItems.value.reduce((sum, item) => sum + item.num, 0);
});

const fetchAddresses = async () => {
  const res = await getAddressList();
  addresses.value = res.data;
  const defaultAddr = res.data.find(a => a.isDefault);
  if (defaultAddr) {
    selectedAddressId.value = defaultAddr.addressId;
  } else if (res.data.length > 0) {
    selectedAddressId.value = res.data[0].addressId;
  }
};

const fetchBuyNowGoods = async () => {
  try {
    const res = await getGoodsDetail(goodsId as number);
    const g: Goods = res.data;
    buyNowItem.value = {
      cartId: 0,
      goodsId: g.secondHandMallId,
      userId: 0,
      title: g.title,
      img: g.coverImg,
      price: g.price,
      priceAgo: g.priceAgo,
      num: buyNum,
      priceCount: g.price * buyNum,
      type: (g as any).type || '',
      description: g.description || '',
      selected: true,
    };
  } catch (error) {
    ElMessage.error('商品不存在或已下架');
    router.push('/goods/list');
  }
};

const selectAddress = (addr: Address) => {
  selectedAddressId.value = addr.addressId;
};

const handleSubmit = async () => {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址');
    return;
  }
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择商品');
    return;
  }

  loading.value = true;
  try {
    let cartIds: number[];
    if (isBuyNow.value && buyNowItem.value) {
      // 立即购买：先加入购物车取得 cartId，再下单
      await addToCart({ goodsId: buyNowItem.value.goodsId, num: buyNowItem.value.num });
      await cartStore.fetchCartList();
      const added = cartStore.items.find(i => i.goodsId === buyNowItem.value!.goodsId);
      if (!added) {
        throw new Error('加入购物车失败');
      }
      cartIds = [added.cartId];
    } else {
      cartIds = selectedItems.value.map(item => item.cartId);
    }

    await createOrder({
      cartIds,
      addressId: selectedAddress.value.addressId,
      remark: remark.value,
    });
    ElMessage.success('订单创建成功');
    await cartStore.fetchCartList();
    router.push('/order/list');
  } catch (error) {
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchAddresses();
  if (isBuyNow.value) {
    fetchBuyNowGoods();
  } else {
    cartStore.fetchCartList();
  }
});
</script>

<template>
  <div class="order-confirm-page" v-loading="loading">
    <div class="confirm-container">
      <div class="confirm-main">
        <el-card class="section-card">
          <template #header>
            <h2>收货地址</h2>
          </template>
          <div class="address-list">
<div
  v-for="addr in addresses"
  :key="addr.addressId"
  class="address-item"
  :class="{ selected: selectedAddressId === addr.addressId }"
  @click="selectAddress(addr)"
>
<div class="address-radio">
  <el-radio :label="addr.addressId" v-model="selectedAddressId" />
</div>
              <div class="address-info">
                <div class="address-header">
                  <span class="addr-name">{{ addr.name }}</span>
                  <span class="addr-phone">{{ addr.phone }}</span>
                  <el-tag v-if="addr.isDefault" size="small" type="success">默认</el-tag>
                </div>
                <div class="addr-detail">{{ addr.address }}</div>
                <div v-if="addr.postcode" class="addr-postcode">邮编：{{ addr.postcode }}</div>
              </div>
            </div>
            <div class="address-item add-address" @click="router.push('/user/profile')">
              <el-icon><Plus /></el-icon>
              <span>新增地址</span>
            </div>
          </div>
        </el-card>

        <el-card class="section-card">
          <template #header>
            <h2>商品清单</h2>
          </template>
          <div class="goods-list">
            <div v-for="item in selectedItems" :key="item.cartId" class="order-goods-item">
              <el-image :src="item.img || DEFAULT_COVER" fit="cover" class="goods-img" />
              <div class="goods-detail">
                <p class="goods-title">{{ item.title }}</p>
                <p class="goods-specs">{{ item.type }}</p>
                <p class="goods-price">{{ formatPrice(item.price) }} × {{ item.num }}</p>
              </div>
            </div>
          </div>
        </el-card>

        <el-card class="section-card">
          <template #header>
            <h2>备注留言</h2>
          </template>
          <el-input
            v-model="remark"
            type="textarea"
            :rows="2"
            placeholder="选填：给卖家留言（如：请发顺丰、易碎物品请小心）"
            maxlength="200"
            show-word-limit
          />
        </el-card>
      </div>

      <el-card class="confirm-sidebar">
        <div class="price-detail">
          <div class="price-row">
            <span>商品金额</span>
            <span>{{ formatPrice(totalPrice) }}</span>
          </div>
          <div class="price-row">
            <span>运费</span>
            <span>¥0.00 (面交/同城免运费)</span>
          </div>
          <el-divider />
          <div class="price-row total">
            <span>实付款</span>
            <span>{{ formatPrice(totalPrice) }}</span>
          </div>
        </div>
        <el-button type="primary" block size="large" :loading="loading" @click="handleSubmit" :disabled="!selectedAddress || selectedItems.length === 0">
          提交订单 ({{ totalCount }})
        </el-button>
        <p class="secure-tip">
          <el-icon><Lock /></el-icon>
          交易安全有保障
        </p>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.order-confirm-page {
  padding: 20px;
  max-width: 1100px;
  margin: 0 auto;
}
.confirm-container {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
}
.section-card :deep(.el-card__header) {
  padding-bottom: 12px;
  border-bottom: 1px solid #e8eaec;
}
.section-card h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}
.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.address-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  border: 1px solid #e8eaec;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.address-item:hover {
  border-color: #409eff;
}
.address-item.selected {
  border-color: #409eff;
  background: #f0f7ff;
}
.address-radio {
  margin-top: 4px;
}
.address-info {
  flex: 1;
}
.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}
.addr-name {
  font-weight: 600;
  font-size: 14px;
}
.addr-phone {
  color: #909399;
  font-size: 14px;
}
.addr-detail {
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
}
.addr-postcode {
  color: #c0c4cc;
  font-size: 12px;
  margin-top: 4px;
}
.add-address {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  color: #909399;
  cursor: pointer;
}
.add-address:hover {
  border-color: #409eff;
  color: #409eff;
}
.goods-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
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
  font-weight: 500;
}
.confirm-sidebar {
  position: sticky;
  top: 100px;
  height: fit-content;
}
.price-detail {
  margin-bottom: 16px;
}
.price-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}
.price-row.total {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}
.secure-tip {
  text-align: center;
  margin-top: 16px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

@media (max-width: 992px) {
  .confirm-container {
    grid-template-columns: 1fr;
  }
  .confirm-sidebar {
    position: static;
  }
}
</style>