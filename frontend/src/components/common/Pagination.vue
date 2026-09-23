<script setup lang="ts">
import { computed } from 'vue';
import type { PaginationProps } from 'element-plus';

interface Props {
  pageSize?: number;
  total: number;
  currentPage: number;
  pageSizes?: number[];
}

const props = withDefaults(defineProps<Props>(), {
  pageSize: 10,
  pageSizes: () => [10, 20, 50, 100],
});

const emit = defineEmits<{
  (e: 'update:currentPage', page: number): void;
  (e: 'update:pageSize', size: number): void;
}>();

const handleSizeChange = (size: number) => {
  emit('update:pageSize', size);
};

const handleCurrentChange = (page: number) => {
  emit('update:currentPage', page);
};
</script>

<template>
  <el-pagination
    v-bind="{
      ...$attrs,
      pageSize: props.pageSize,
      total: props.total,
      pageSizes: props.pageSizes,
      layout: 'total, sizes, prev, pager, next, jumper',
    }"
    :current-page="props.currentPage"
    @size-change="handleSizeChange"
    @current-change="handleCurrentChange"
    background
    small
  />
</template>