<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { getGoodsList, getCategories, getSellerFirstImages } from "@/api/modules/goods";
import { useUserStore } from "@/stores/userStore";
import GoodsCard from "@/components/common/GoodsCard.vue";
import type { Goods, Category } from "@/api/types/goods";

const router = useRouter();
const userStore = useUserStore();

const goToPublish = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning("请先登录");
    router.push("/login");
    return;
  }
  if (!userStore.isSeller && !userStore.isAdmin) {
    ElMessage.warning("请先申请成为卖家");
    router.push("/user/profile");
    return;
  }
  router.push("/goods/publish");
};

const banners = ref([
  { image: "https://picsum.photos/1200/400?random=1", link: "/goods/list" },
  { image: "https://picsum.photos/1200/400?random=2", link: "/goods/list" },
  { image: "https://picsum.photos/1200/400?random=3", link: "/goods/list" },
]);

const categories = ref<Category[]>([]);
const hotGoods = ref<Goods[]>([]);
const newGoods = ref<Goods[]>([]);
const loading = ref(false);
const firstImageMap = ref<Record<number, string>>({});

const displayCategories = computed(() => {
  // 如果 categories.value 不是数组，返回空数组
  if (!categories.value || !Array.isArray(categories.value)) {
    return [];
  }
  return categories.value.slice(0, 8);
});

const hasHotGoods = computed(() => {
  return (
    hotGoods.value && Array.isArray(hotGoods.value) && hotGoods.value.length > 0
  );
});

const hasNewGoods = computed(() => {
  return (
    newGoods.value && Array.isArray(newGoods.value) && newGoods.value.length > 0
  );
});

const fetchData = async () => {
  loading.value = true;
  try {
    const [catRes, hotRes, newRes] = await Promise.all([
      getCategories(),
      getGoodsList({ page: 1, size: 8, sortBy: "hits_desc", status: 1 }),
      getGoodsList({ page: 1, size: 8, sortBy: "time_desc", status: 1 }),
    ]);

    console.log("catRes:", catRes);
    console.log("hotRes:", hotRes);
    console.log("newRes:", newRes);

    categories.value = Array.isArray(catRes?.data) ? catRes.data : [];
    hotGoods.value = Array.isArray(hotRes?.data?.list) ? hotRes.data.list : [];
    newGoods.value = Array.isArray(newRes?.data?.list) ? newRes.data.list : [];

    // 收集去重的卖家ID，批量获取各自首张发布图，用于缺封面商品兜底
    const sellerIds = Array.from(
      new Set(
        [...hotGoods.value, ...newGoods.value]
          .map((g) => g.sellerId)
          .filter((id): id is number => typeof id === 'number'),
      ),
    );
    if (sellerIds.length > 0) {
      try {
        const res = await getSellerFirstImages(sellerIds);
        firstImageMap.value = res.data || {};
      } catch {
        firstImageMap.value = {};
      }
    } else {
      firstImageMap.value = {};
    }

    console.log("categories.value:", categories.value);
    console.log("hotGoods.value:", hotGoods.value);
    console.log("newGoods.value:", newGoods.value);
  } catch (error) {
    console.error("数据加载失败:", error);
    ElMessage.error("数据加载失败");
    // ✅ 错误时确保是空数组
    categories.value = [];
    hotGoods.value = [];
    newGoods.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchData();
  if (userStore.token && !userStore.userInfo) {
    userStore.fetchUserInfo();
  }
});
</script>

<template>
  <div class="home-page">
    <!-- 轮播图 -->
    <el-carousel
      :interval="4000"
      indicator-position="outside"
      arrow="hover"
      class="banner-carousel"
    >
      <el-carousel-item v-for="(banner, index) in banners" :key="index">
        <a :href="banner.link" target="_blank">
          <el-image :src="banner.image" fit="cover" />
        </a>
      </el-carousel-item>
    </el-carousel>

    <div class="home-content">
      <!-- 分类导航 -->
      <el-card class="category-section" shadow="never">
        <template #header>
          <div class="section-header">
            <h2>热门分类</h2>
            <el-button
              type="primary"
              :disabled="!userStore.isLoggedIn"
              @click="goToPublish"
            >
              <el-icon><Plus /></el-icon> 发布商品
            </el-button>
          </div>
        </template>
        <div v-if="displayCategories.length > 0" class="category-grid">
          <div
            v-for="cat in displayCategories"
            :key="cat.categoryId"
            class="category-item"
            @click="
              () =>
                $router.push({
                  path: '/goods/list',
                  query: { categoryId: cat.categoryId },
                })
            "
          >
            <el-icon class="category-icon"><Goods /></el-icon>
            <span>{{ cat.categoryName }}</span>
          </div>
        </div>
        <el-empty v-else-if="!loading" description="暂无分类" />
      </el-card>

      <!-- 热门商品 -->
      <el-card class="goods-section" shadow="never" v-loading="loading">
        <template #header>
          <div class="section-header">
            <h2>热门推荐</h2>
            <router-link to="/goods/list" class="more-link"
              >查看更多 <i class="el-icon-arrow-right"></i
            ></router-link>
          </div>
        </template>
        <!-- ✅ 修改：使用 hasHotGoods 判断 -->
        <div v-if="hasHotGoods" class="goods-grid">
          <GoodsCard
            v-for="goods in hotGoods"
            :key="goods.secondHandMallId"
            :goods="goods"
            :first-image-map="firstImageMap"
          />
        </div>
        <el-empty v-else-if="!loading" description="暂无热门商品" />
      </el-card>

      <!-- 最新上架 -->
      <el-card class="goods-section" shadow="never" v-loading="loading">
        <template #header>
          <div class="section-header">
            <h2>最新上架</h2>
            <router-link to="/goods/list" class="more-link"
              >查看更多 <i class="el-icon-arrow-right"></i
            ></router-link>
          </div>
        </template>
        <div v-if="hasNewGoods" class="goods-grid">
          <GoodsCard
            v-for="goods in newGoods"
            :key="goods.secondHandMallId"
            :goods="goods"
            :first-image-map="firstImageMap"
          />
        </div>
        <el-empty v-else-if="!loading" description="暂无最新商品" />
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  min-height: 100vh;
}
.banner-carousel {
  height: 320px;
  border-radius: 0 0 12px 12px;
  overflow: hidden;
  margin-bottom: 24px;
}
.banner-carousel :deep(.el-carousel__item) {
  height: 100%;
}
.banner-carousel :deep(.el-carousel__item img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.home-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.section-header h2 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}
.more-link {
  color: #409eff;
  font-size: 14px;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 4px;
}
.more-link:hover {
  text-decoration: underline;
}
.category-section {
  margin-bottom: 24px;
}
.category-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 16px;
}
.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  background: #fafafa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.category-item:hover {
  background: #f0f7ff;
  transform: translateY(-2px);
}
.category-icon {
  font-size: 24px;
  color: #409eff;
}
.category-item span {
  font-size: 12px;
  color: #303133;
}
.goods-section {
  margin-bottom: 24px;
}
.goods-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-top: 8px;
}

@media (max-width: 992px) {
  .goods-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .category-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .goods-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  .category-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  .banner-carousel {
    height: 200px;
  }
}
</style>
