<script setup lang="ts">
import { computed } from 'vue';
import { formatPrice, formatRelativeTime } from '@/utils/format';

interface Props {
  goods: {
    secondHandMallId: number;
    title: string;
    description: string;
    price: number;
    priceAgo: number;
    coverImg: string;
    hits: number;
    createTime: string;
    sellerId?: number;
    sellerName?: string;
    categoryName?: string;
  };
  firstImageMap?: Record<number, string>;
}

const props = defineProps<Props>();

// 封面图为空时回退：优先用「卖家首张发布图」，再回退到默认占位图，避免出现空白
const DEFAULT_COVER =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300">' +
      '<rect width="100%" height="100%" fill="#f5f7fa"/>' +
      '<text x="50%" y="50%" font-size="20" fill="#c0c4cc" text-anchor="middle" dominant-baseline="middle">暂无图片</text>' +
      '</svg>'
  );

const cover = computed(() => {
  if (props.goods.coverImg) return props.goods.coverImg;
  if (props.goods.sellerId != null && props.firstImageMap?.[props.goods.sellerId]) {
    return props.firstImageMap[props.goods.sellerId];
  }
  return DEFAULT_COVER;
});
</script>

<template>
  <el-card :class="['goods-card', { 'goods-card-hover': true }]" shadow="hover">
    <router-link :to="`/goods/detail/${props.goods.secondHandMallId}`" class="goods-card-link">
      <el-image
        :src="cover"
        fit="cover"
        class="goods-cover"
        :preview-src-list="[cover]"
      />
      <div class="goods-info">
        <h3 class="goods-title">{{ props.goods.title }}</h3>
        <p class="goods-desc">{{ props.goods.description }}</p>
        <div class="goods-meta">
          <span class="goods-price">{{ formatPrice(props.goods.price) }}</span>
          <span v-if="props.goods.priceAgo > props.goods.price" class="goods-old-price">
            {{ formatPrice(props.goods.priceAgo) }}
          </span>
        </div>
        <div class="goods-footer">
          <span class="goods-seller" v-if="props.goods.sellerName">
            <i class="el-icon-user"></i> {{ props.goods.sellerName }}
          </span>
          <span class="goods-time">
            <i class="el-icon-time"></i> {{ formatRelativeTime(props.goods.createTime) }}
          </span>
          <span class="goods-hits">
            <i class="el-icon-view"></i> {{ props.goods.hits }}
          </span>
        </div>
      </div>
    </router-link>
  </el-card>
</template>

<style scoped>
.goods-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
}
.goods-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.12) !important;
}
.goods-card-link {
  text-decoration: none;
  color: inherit;
  display: flex;
  flex-direction: column;
  height: 100%;
}
.goods-cover {
  width: 100%;
  height: 180px;
  border-radius: 8px 8px 0 0;
  margin: -20px -20px 12px -20px;
}
.goods-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0 4px 12px;
}
.goods-title {
  font-size: 14px;
  font-weight: 600;
  line-height: 1.4;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.goods-desc {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin: 0 0 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  flex: 1;
}
.goods-meta {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 12px;
}
.goods-price {
  font-size: 18px;
  font-weight: 700;
  color: #f56c6c;
}
.goods-old-price {
  font-size: 12px;
  color: #c0c4cc;
  text-decoration: line-through;
}
.goods-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
  color: #c0c4cc;
}
.goods-seller,
.goods-time,
.goods-hits {
  display: flex;
  align-items: center;
  gap: 4px;
}
.goods-seller i,
.goods-time i,
.goods-hits i {
  font-size: 12px;
}
</style>