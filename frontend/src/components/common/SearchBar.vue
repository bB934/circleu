<script setup lang="ts">
import type { GoodsQueryParams } from '@/api/types/goods';

interface Props {
  modelValue: GoodsQueryParams;
  categories?: { categoryId: number; categoryName: string }[];
  size?: '' | 'small' | 'default' | 'large';
}

const props = withDefaults(defineProps<Props>(), {
  categories: () => [],
});

const emit = defineEmits<{
  (e: 'update:modelValue', value: GoodsQueryParams): void;
  (e: 'search'): void;
  (e: 'reset'): void;
}>();

const handleSearch = () => {
  emit('search');
};

const handleReset = () => {
  emit('update:modelValue', {
    page: 1,
    size: 10,
    keyword: '',
    categoryId: undefined,
    minPrice: undefined,
    maxPrice: undefined,
    sortBy: undefined,
    status: 1,
  });
  emit('reset');
};
</script>

<template>
  <el-card class="search-bar">
    <el-form :model="props.modelValue" inline :size="size">
      <el-form-item label="关键词">
        <el-input
          v-model="props.modelValue.keyword"
          placeholder="搜索商品名称/描述"
          clearable
          style="width: 200px"
        />
      </el-form-item>
      <el-form-item label="分类" v-if="props.categories.length > 0">
        <el-select
          v-model="props.modelValue.categoryId"
          placeholder="全部分类"
          clearable
          style="width: 160px"
          @change="emit('search')"
        >
          <el-option
            v-for="cat in props.categories"
            :key="cat.categoryId"
            :label="cat.categoryName"
            :value="cat.categoryId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="价格区间">
        <el-input-number
          v-model="props.modelValue.minPrice"
          :min="0"
          :step="1"
          placeholder="最低价"
          style="width: 100px"
          :controls="false"
        />
        <span class="price-separator">-</span>
        <el-input-number
          v-model="props.modelValue.maxPrice"
          :min="0"
          :step="1"
          placeholder="最高价"
          style="width: 100px"
          :controls="false"
        />
      </el-form-item>
      <el-form-item label="排序">
        <el-select
          v-model="props.modelValue.sortBy"
          placeholder="默认排序"
          clearable
          style="width: 140px"
          @change="emit('search')"
        >
          <el-option label="最新发布" value="time_desc" />
          <el-option label="价格升序" value="price_asc" />
          <el-option label="价格降序" value="price_desc" />
          <el-option label="浏览最多" value="hits_desc" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped>
.search-bar {
  margin-bottom: 16px;
}
.price-separator {
  display: inline-flex;
  align-items: center;
  padding: 0 8px;
  color: #909399;
}
</style>