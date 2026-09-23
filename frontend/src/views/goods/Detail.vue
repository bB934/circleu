<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getGoodsDetail, favoriteGoods, unfavoriteGoods } from '@/api/modules/goods';
import { getGoodsReviews, deleteGoodsReview } from '@/api/modules/extra-modules';
import { addToCart } from '@/api/modules/cart';
import { useUserStore } from '@/stores/userStore';
import { useCartStore } from '@/stores/cartStore';
import { formatPrice, formatDateTime } from '@/utils/format';
import type { Goods } from '@/api/types/goods';
import type { GoodsReview } from '@/api/modules/extra-modules';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const cartStore = useCartStore();

const goodsId = Number(route.params.id);
const loading = ref(true);
const goods = ref<Goods | null>(null);
const currentImageIndex = ref(0);
const favLoading = ref(false);
const cartLoading = ref(false);

const images = computed(() => {
  if (!goods.value) return [];
  const imgs = [goods.value.coverImg];
  if (goods.value.img1) imgs.push(goods.value.img1);
  if (goods.value.img2) imgs.push(goods.value.img2);
  if (goods.value.img3) imgs.push(goods.value.img3);
  if (goods.value.img4) imgs.push(goods.value.img4);
  if (goods.value.img5) imgs.push(goods.value.img5);
  return imgs.filter(Boolean);
});

const isFavorited = ref(false);

// ---------- 商品买家评价 ----------
const reviews = ref<GoodsReview[]>([]);
const reviewsLoading = ref(false);
const reviewTotal = ref(0);
const avgRating = ref(0);

const fetchReviews = async () => {
  reviewsLoading.value = true;
  try {
    const res = await getGoodsReviews(goodsId);
    reviews.value = res.data?.list || [];
    reviewTotal.value = res.data?.total || 0;
    avgRating.value = res.data?.avgRating || 0;
  } catch (error) {
  } finally {
    reviewsLoading.value = false;
  }
};

const deletingReviewId = ref<number | null>(null);

const handleDeleteReview = async (review: GoodsReview) => {
  try {
    await ElMessageBox.confirm('确定删除这条评价吗？删除后不可恢复', '删除评价', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    });
  } catch (error) {
    return;
  }
  deletingReviewId.value = review.reviewId;
  try {
    await deleteGoodsReview(review.reviewId);
    ElMessage.success('评价已删除');
    reviews.value = reviews.value.filter((r) => r.reviewId !== review.reviewId);
    reviewTotal.value = Math.max(0, reviewTotal.value - 1);
    const sum = reviews.value.reduce((acc, r) => acc + r.rating, 0);
    avgRating.value = reviews.value.length ? Number((sum / reviews.value.length).toFixed(1)) : 0;
  } catch (error) {
  } finally {
    deletingReviewId.value = null;
  }
};

const fetchDetail = async () => {
  loading.value = true;
  try {
    const res = await getGoodsDetail(goodsId);
    goods.value = res.data;
  } catch (error) {
    ElMessage.error('商品不存在或已下架');
    router.push('/goods/list');
  } finally {
    loading.value = false;
  }
};

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  favLoading.value = true;
  try {
    if (isFavorited.value) {
      await unfavoriteGoods(goodsId);
      isFavorited.value = false;
      ElMessage.success('已取消收藏');
    } else {
      await favoriteGoods(goodsId);
      isFavorited.value = true;
      ElMessage.success('收藏成功');
    }
  } catch (error) {
  } finally {
    favLoading.value = false;
  }
};

const handleAddToCart = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  if (!goods.value) return;
  if (goods.value.inventory <= 0) {
    ElMessage.warning('商品库存不足');
    return;
  }
  cartLoading.value = true;
  try {
    await addToCart({ goodsId: goods.value.secondHandMallId, num: 1 });
    await cartStore.fetchCartList();
    ElMessage.success('已加入购物车');
  } catch (error) {
  } finally {
    cartLoading.value = false;
  }
};

const handleBuyNow = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  if (!goods.value) return;
  if (goods.value.inventory <= 0) {
    ElMessage.warning('商品库存不足');
    return;
  }
  router.push({ path: '/order/confirm', query: { goodsId: goods.value.secondHandMallId, num: 1 } });
};

onMounted(() => {
  fetchDetail();
  fetchReviews();
});
</script>

<template>
  <div class="goods-detail-page" v-loading="loading">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/goods/list' }">商品广场</el-breadcrumb-item>
      <el-breadcrumb-item>{{ goods?.title }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="detail-content">
      <div class="detail-main">
        <!-- 图片轮播 -->
        <div class="gallery">
          <el-carousel v-model="currentImageIndex" :interval="0" arrow="hover" class="main-carousel">
            <el-carousel-item v-for="(img, index) in images" :key="index">
              <el-image :src="img" fit="contain" :preview-src-list="images" />
            </el-carousel-item>
          </el-carousel>
          <div class="thumbnails" v-if="images.length > 1">
            <div
              v-for="(img, index) in images"
              :key="index"
              :class="['thumb', { active: currentImageIndex === index }]"
              @click="currentImageIndex = index"
            >
              <el-image :src="img" fit="cover" />
            </div>
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="goods-info">
          <h1 class="goods-title">{{ goods?.title }}</h1>
          <div class="goods-price-row">
            <span class="current-price">{{ formatPrice(goods?.price || 0) }}</span>
            <span v-if="goods && goods.priceAgo > goods.price" class="original-price">{{ formatPrice(goods.priceAgo) }}</span>
            <el-tag v-if="goods" :type="goods.status === 1 ? 'success' : goods.status === 2 ? 'danger' : 'info'" class="status-tag">
              {{ goods.status === 1 ? '在售中' : goods.status === 2 ? '已售出' : '已下架' }}
            </el-tag>
          </div>

          <div class="goods-meta">
            <span><el-icon><User /></el-icon> 卖家：{{ goods?.sellerName || '未知' }}</span>
            <span><el-icon><Location /></el-icon> 分类：{{ goods?.categoryName || '未知' }}</span>
            <span><el-icon><View /></el-icon> 浏览：{{ goods?.hits || 0 }}</span>
            <span><el-icon><Time /></el-icon> 发布时间：{{ goods?.createTime ? formatDateTime(goods.createTime) : '' }}</span>
          </div>

          <el-divider />

          <div class="goods-description">
            <h3>商品描述</h3>
            <p>{{ goods?.description }}</p>
          </div>

          <div class="goods-content" v-if="goods?.content">
            <h3>详细信息</h3>
            <div v-html="goods.content" />
          </div>
        </div>

        <!-- 买家评价 -->
        <div class="goods-reviews">
          <div class="reviews-header">
            <h3 class="reviews-title">买家评价（{{ reviewTotal }}）</h3>
            <div class="reviews-summary" v-if="reviewTotal > 0">
              <el-rate :model-value="avgRating" disabled allow-half />
              <span class="reviews-avg">{{ avgRating }} 分</span>
            </div>
          </div>

          <div class="review-list" v-loading="reviewsLoading">
            <el-empty v-if="!reviewsLoading && reviews.length === 0" description="暂无评价，快来抢沙发吧~" />
            <div class="review-item" v-for="item in reviews" :key="item.reviewId">
              <el-avatar :size="40" :src="item.avatar">{{ item.nickname?.charAt(0) }}</el-avatar>
              <div class="review-body">
                <div class="review-head">
                  <span class="review-name">{{ item.nickname }}</span>
                  <el-rate :model-value="item.rating" disabled />
                  <span class="review-time">{{ item.createTime ? formatDateTime(item.createTime) : '' }}</span>
                  <el-button
                    v-if="userStore.userInfo?.userId === item.userId"
                    class="review-delete"
                    type="danger"
                    link
                    size="small"
                    :loading="deletingReviewId === item.reviewId"
                    @click="handleDeleteReview(item)"
                  >
                    <el-icon><Delete /></el-icon> 删除
                  </el-button>
                </div>
                <p class="review-content">{{ item.content }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="detail-sidebar">
        <el-card class="action-card">
          <div class="price-summary">
            <span class="label">商品价格</span>
            <span class="value">{{ formatPrice(goods?.price || 0) }}</span>
          </div>
          <div class="stock-info" v-if="goods">
            <el-progress :percentage="Math.min((goods?.inventory / 10) * 100, 100)" :stroke-width="8" :format="() => `库存：${goods?.inventory} 件`" />
          </div>
          <div class="actions">
            <el-button type="primary" block :loading="cartLoading" :disabled="!goods || goods.inventory <= 0 || goods.status !== 1" @click="handleAddToCart">
              <el-icon><ShoppingCart /></el-icon> 加入购物车
            </el-button>
            <el-button type="success" block :disabled="!goods || goods.inventory <= 0 || goods.status !== 1" @click="handleBuyNow">
              <el-icon><ShoppingBag /></el-icon> 立即购买
            </el-button>
            <el-button block :loading="favLoading" @click="toggleFavorite">
              <el-icon><StarFilled v-if="isFavorited" /><Star v-else /></el-icon>
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </el-card>

        <el-card class="seller-card" v-if="goods?.sellerId">
          <template #header>
            <div class="seller-header">
              <el-avatar :size="50" :src="goods?.coverImg || ''" />
              <div>
<h4>{{ goods?.sellerName }}</h4>
<el-tag size="small" type="success">卖家</el-tag>
              </div>
            </div>
          </template>
          <div class="seller-stats">
            <div class="stat">
              <span class="stat-value">0</span>
              <span class="stat-label">在售商品</span>
            </div>
            <div class="stat">
              <span class="stat-value">0</span>
              <span class="stat-label">已售商品</span>
            </div>
            <div class="stat">
              <span class="stat-value">0</span>
              <span class="stat-label">好评率</span>
            </div>
          </div>
          <el-button block @click="router.push({ path: '/user/profile', query: { userId: goods?.sellerId } })">查看店铺</el-button>
        </el-card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.goods-detail-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.breadcrumb {
  margin-bottom: 20px;
  font-size: 14px;
}
.detail-content {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
}
.detail-main {
  min-width: 0;
}
.gallery {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
}
.main-carousel {
  height: 500px;
  background: #fafafa;
}
.main-carousel :deep(.el-carousel__item) {
  display: flex;
  align-items: center;
  justify-content: center;
}
.main-carousel img {
  max-width: 100%;
  max-height: 500px;
}
.thumbnails {
  display: flex;
  gap: 8px;
  padding: 12px;
  overflow-x: auto;
}
.thumb {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  flex-shrink: 0;
}
.thumb.active {
  border-color: #409eff;
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.goods-info {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}
.goods-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 16px;
  color: #303133;
}
.goods-price-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.current-price {
  font-size: 28px;
  font-weight: 700;
  color: #f56c6c;
}
.original-price {
  font-size: 16px;
  color: #c0c4cc;
  text-decoration: line-through;
}
.status-tag {
  font-size: 12px;
}
.goods-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 16px;
  color: #909399;
  font-size: 14px;
}
.goods-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}
.goods-meta i {
  font-size: 16px;
}
.goods-description,
.goods-content {
  margin-top: 24px;
}
.goods-description h3,
.goods-content h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8eaec;
}
.goods-description p {
  line-height: 1.8;
  color: #606266;
}
.goods-content :deep(img) {
  max-width: 100%;
  height: auto;
}

.goods-reviews {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-top: 20px;
}
.reviews-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8eaec;
}
.reviews-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}
.reviews-summary {
  display: flex;
  align-items: center;
  gap: 8px;
}
.reviews-avg {
  font-size: 13px;
  color: #f56c6c;
  font-weight: 600;
}
.review-list {
  min-height: 60px;
}
.review-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f2f3f5;
}
.review-item:last-child {
  border-bottom: none;
}
.review-body {
  flex: 1;
  min-width: 0;
}
.review-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}
.review-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.review-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-left: auto;
}
.review-delete {
  margin-left: 8px;
}
.review-content {
  margin: 0;
  line-height: 1.7;
  color: #606266;
  font-size: 14px;
  white-space: pre-wrap;
  word-break: break-word;
}

.action-card {
  position: sticky;
  top: 100px;
}
.price-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #e8eaec;
  font-size: 16px;
}
.price-summary .value {
  font-size: 24px;
  font-weight: 700;
  color: #f56c6c;
}
.stock-info {
  padding: 16px 0;
  border-bottom: 1px solid #e8eaec;
}
.actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 0;
}
.actions .el-button {
  height: 44px;
  font-size: 15px;
}

.seller-card {
  margin-top: 20px;
}
.seller-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.seller-header h4 {
  margin: 0;
  font-size: 16px;
}
.seller-stats {
  display: flex;
  justify-content: space-around;
  padding: 16px 0;
  border-top: 1px solid #e8eaec;
  border-bottom: 1px solid #e8eaec;
  margin: 16px 0;
}
.stat {
  text-align: center;
}
.stat-value {
  display: block;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}
.stat-label {
  font-size: 12px;
  color: #909399;
}

@media (max-width: 992px) {
  .detail-content {
    grid-template-columns: 1fr;
  }
  .detail-sidebar {
    order: -1;
  }
  .action-card {
    position: static;
  }
  .main-carousel {
    height: 350px;
  }
}
</style>