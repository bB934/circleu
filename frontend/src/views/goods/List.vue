<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getGoodsList, getCategories, getSellerFirstImages } from '@/api/modules/goods';
import GoodsCard from '@/components/common/GoodsCard.vue';
import Pagination from '@/components/common/Pagination.vue';
import SearchBar from '@/components/common/SearchBar.vue';
import type { Goods, GoodsQueryParams, Category } from '@/api/types/goods';

const router = useRouter();
const route = useRoute();

const categories = ref<Category[]>([]);
const loading = ref(false);
const goodsList = ref<Goods[]>([]);
const total = ref(0);
const firstImageMap = ref<Record<number, string>>({});

const query = ref<GoodsQueryParams>({
  page: 1,
  size: 12,
  keyword: '',
  categoryId: undefined,
  minPrice: undefined,
  maxPrice: undefined,
  sortBy: 'time_desc',
  status: 1,
});

const fetchCategories = async () => {
  const res = await getCategories();
  categories.value = res.data;
};

const fetchGoodsList = async (resetPage = true) => {
  if (resetPage) {
    query.value.page = 1;
  }
  loading.value = true;
  try {
    const res = await getGoodsList(query.value);
    goodsList.value = res.data.list || [];
    total.value = res.data.total || 0;
    await fetchFirstImageMap();
  } catch (error) {
    ElMessage.error('商品加载失败');
  } finally {
    loading.value = false;
  }
};

// 收集卖家ID，批量获取各自首张发布图，用于缺封面商品兜底
const fetchFirstImageMap = async () => {
  const sellerIds = Array.from(
    new Set(
      goodsList.value
        .map((g) => g.sellerId)
        .filter((id): id is number => typeof id === 'number'),
    ),
  );
  if (sellerIds.length === 0) {
    firstImageMap.value = {};
    return;
  }
  try {
    const res = await getSellerFirstImages(sellerIds);
    firstImageMap.value = res.data || {};
  } catch {
    firstImageMap.value = {};
  }
};

const handleSearch = () => {
  fetchGoodsList(true);
};

const handleReset = () => {
  query.value = {
    page: 1,
    size: 12,
    keyword: '',
    categoryId: undefined,
    minPrice: undefined,
    maxPrice: undefined,
    sortBy: 'time_desc',
    status: 1,
  };
  fetchGoodsList(true);
};

const onPageChange = (page: number) => {
  query.value.page = page;
  fetchGoodsList(false);
};

const onSizeChange = (size: number) => {
  query.value.size = size;
  fetchGoodsList(true);
};

// 从路由 query 同步分类筛选（如从首页分类点击进入 /goods/list?categoryId=4）
const syncCategoryFromRoute = () => {
  const catId = route.query.categoryId;
  query.value.categoryId =
    catId !== undefined && catId !== '' ? Number(catId) : undefined;
};

onMounted(() => {
  syncCategoryFromRoute();
  fetchCategories();
  fetchGoodsList(true);
});

// 在列表页内切换分类（query 变化）时重新筛选
watch(
  () => route.query.categoryId,
  () => {
    syncCategoryFromRoute();
    fetchGoodsList(true);
  }
);
</script>

<template>
  <div class="goods-list-page">
    <div class="page-header">
      <h1>商品广场</h1>
      <router-link to="/goods/publish" class="publish-btn" v-if="false">
        <el-button type="primary"><el-icon><Plus /></el-icon> 发布商品</el-button>
      </router-link>
    </div>

    <SearchBar
      v-model="query"
      :categories="categories"
      @search="handleSearch"
      @reset="handleReset"
    />

    <div v-loading="loading" class="goods-grid">
      <GoodsCard v-for="goods in goodsList" :key="goods.secondHandMallId" :goods="goods" :first-image-map="firstImageMap" />
    </div>

    <el-empty v-if="!loading && goodsList.length === 0" description="暂无商品" />

    <Pagination
      v-if="total > 0"
      :total="total"
      v-model:current-page="query.page"
      v-model:page-size="query.size"
      :page-sizes="[12, 24, 36]"
      @current-change="onPageChange"
      @size-change="onSizeChange"
    />
  </div>
</template>

<style scoped>
.goods-list-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}
.publish-btn {
  margin-left: 16px;
}
.goods-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

@media (max-width: 992px) {
  .goods-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .goods-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>