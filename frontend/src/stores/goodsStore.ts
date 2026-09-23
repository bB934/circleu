import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getGoodsList, getCategories } from '@/api/modules/goods';
import type { Goods, Category, GoodsQueryParams } from '@/api/types';

export const useGoodsStore = defineStore('goods', () => {
  const categories = ref<Category[]>([]);
  const loading = ref(false);
  const currentGoods = ref<Goods | null>(null);

  const fetchCategories = async () => {
    const res = await getCategories();
    categories.value = res.data;
  };

  const fetchGoodsList = async (params: GoodsQueryParams) => {
    loading.value = true;
    try {
      const res = await getGoodsList(params);
      return res.data;
    } finally {
      loading.value = false;
    }
  };

  const fetchGoodsDetail = async (_id: number) => {
    loading.value = true;
    try {
      // 这里应该调用单独的 getGoodsDetail API，暂时用 list 代替
      const res = await getGoodsList({ page: 1, size: 1 });
      return res.data.list[0] || null;
    } finally {
      loading.value = false;
    }
  };

  const setCurrentGoods = (goods: Goods | null) => {
    currentGoods.value = goods;
  };

  return {
    categories,
    loading,
    currentGoods,
    fetchCategories,
    fetchGoodsList,
    fetchGoodsDetail,
    setCurrentGoods,
  };
});