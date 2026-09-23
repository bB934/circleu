<template>
  <div class="seller-audit">
    <el-card>
      <template #header>
        <div class="header">
          <h2>卖家申请审核</h2>
          <div class="search">
            <el-input
              v-model="keyword"
              placeholder="搜索姓名/手机号/学校"
              clearable
              style="width: 200px"
              @keyup.enter="fetchData"
            />
            <el-select v-model="filterStatus" placeholder="状态" clearable @change="fetchData">
              <el-option label="待审核" value="pending" />
              <el-option label="已通过" value="approved" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
            <el-button type="primary" @click="fetchData">搜索</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="applicationId" label="ID" width="80" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="gender" label="性别" width="60" />
        <el-table-column prop="age" label="年龄" width="60" />
        <el-table-column prop="school" label="学校" min-width="120" />
        <el-table-column prop="introduction" label="简介" min-width="120" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'pending'">
              <el-button type="success" size="small" @click="handleAudit(row, true)">
                通过
              </el-button>
              <el-button type="danger" size="small" @click="handleAudit(row, false)">
                拒绝
              </el-button>
            </template>
            <span v-else>已处理</span>
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
import { ref, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getApplicationList, auditApplication } from '@/api/modules/user';
import type { SellerApplication } from '@/api/types/user';

const list = ref<SellerApplication[]>([]);
const loading = ref(false);
const total = ref(0);
const page = ref(1);
const size = ref(10);
const keyword = ref('');
const filterStatus = ref('pending');

const getStatusType = (status: string) => {
  const map: Record<string, 'warning' | 'success' | 'danger' | 'info'> = {
    pending: 'warning', approved: 'success', rejected: 'danger',
  };
  return map[status] || 'info';
};

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    pending: '待审核', approved: '已通过', rejected: '已拒绝',
  };
  return map[status] || status;
};

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getApplicationList({
      status: filterStatus.value || undefined,
      keyword: keyword.value || undefined,
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

const handleAudit = async (row: SellerApplication, passed: boolean) => {
  const action = passed ? '通过' : '拒绝';
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 "${row.realName}" 的卖家申请吗？`,
      '确认审核',
      { confirmButtonText: '确定', cancelButtonText: '取消' }
    );
    await auditApplication(row.applicationId, { passed });
    ElMessage.success(`审核${action}成功`);
    fetchData();
  } catch (error) {
    // 用户取消
  }
};

watch(filterStatus, () => {
  page.value = 1;
  fetchData();
});

onMounted(fetchData);
</script>

<style scoped>
.seller-audit {
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
