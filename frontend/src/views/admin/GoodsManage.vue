<template>
  <div class="goods-manage">
    <el-card>
      <template #header>
        <div class="header">
          <h2>商品管理</h2>
          <div class="search">
            <el-input
              v-model="keyword"
              placeholder="搜索商品标题"
              clearable
              style="width: 200px"
              @keyup.enter="fetchData"
            />
            <el-select v-model="filterStatus" placeholder="状态" clearable @change="fetchData">
              <el-option label="上架" :value="1" />
              <el-option label="下架" :value="0" />
              <el-option label="已售" :value="2" />
            </el-select>
            <el-button type="primary" @click="fetchData">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="secondHandMallId" label="ID" width="80" />
        <el-table-column prop="title" label="商品标题" min-width="150" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="sellerName" label="卖家" width="100" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="hits" label="点击" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" size="small" @click="handleAudit(row, true)">
                上架
              </el-button>
            </template>
            <template v-else-if="row.status === 1">
              <el-button type="warning" size="small" @click="handleAudit(row, false)">
                下架
              </el-button>
            </template>
            <el-button
              type="danger"
              size="small"
              plain
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchData"
        @size-change="fetchData"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getAdminGoodsList, auditGoods, deleteGoods } from '@/api/modules/admin';
import type { Goods } from '@/api/types/goods';

const list = ref<Goods[]>([]);
const loading = ref(false);
const total = ref(0);
const page = ref(1);
const size = ref(10);
const keyword = ref('');
const filterStatus = ref<number>();

const getStatusType = (status: number) => {
  const map: Record<number, string> = { 0: 'info', 1: 'success', 2: 'warning' };
  return map[status] || 'info';
};

const getStatusLabel = (status: number) => {
  const map: Record<number, string> = { 0: '下架', 1: '上架', 2: '已售' };
  return map[status] || '未知';
};

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getAdminGoodsList({
      keyword: keyword.value || undefined,
      status: filterStatus.value,
      page: page.value,
      size: size.value,
    });
    list.value = res.data?.list || [];
    total.value = res.data?.total || 0;
  } catch (error) {
    ElMessage.error('加载数据失败');
  } finally {
    loading.value = false;
  }
};

const resetSearch = () => {
  keyword.value = '';
  filterStatus.value = undefined;
  page.value = 1;
  fetchData();
};

const handleAudit = async (row: Goods, passed: boolean) => {
  const action = passed ? '上架' : '下架';
  try {
    await ElMessageBox.confirm(
      `确定要${action}商品 "${row.title}" 吗？`,
      '确认操作',
      { confirmButtonText: '确定', cancelButtonText: '取消' }
    );
    await auditGoods(row.secondHandMallId, { passed });
    ElMessage.success(`${action}成功`);
    fetchData();
  } catch (error) {
    // 用户取消
  }
};

const handleDelete = async (row: Goods) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除商品 "${row.title}" 吗？`,
      '确认删除',
      { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }
    );
    await deleteGoods(row.secondHandMallId);
    ElMessage.success('删除成功');
    fetchData();
  } catch (error) {
    // 用户取消
  }
};

onMounted(fetchData);
</script>

<style scoped>
.goods-manage {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.header h2 {
  margin: 0;
  font-size: 18px;
}

.search {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
