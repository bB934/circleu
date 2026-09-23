<template>
  <div class="user-manage">
    <el-card>
      <template #header>
        <div class="header">
          <h2>用户管理</h2>
          <div class="search">
            <el-input
              v-model="keyword"
              placeholder="搜索用户名/手机号"
              clearable
              style="width: 200px"
              @keyup.enter="fetchData"
            />
            <el-select v-model="filterRole" placeholder="角色" clearable @change="fetchData">
              <el-option label="管理员" value="admin" />
              <el-option label="卖家" value="seller" />
              <el-option label="买家" value="buyer" />
            </el-select>
            <el-select v-model="filterState" placeholder="审核状态" clearable @change="fetchData">
              <el-option label="待审核" value="待审核" />
              <el-option label="已通过" value="已通过" />
              <el-option label="已拒绝" value="已拒绝" />
            </el-select>
            <el-button type="primary" @click="fetchData">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column label="角色" width="80">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)" size="small">
              {{ getRoleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStateType(row.examineState)" size="small">
              {{ row.examineState || '已通过' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <template v-if="row.examineState === '待审核'">
              <el-button type="success" size="small" @click="handleAudit(row, true)">
                通过
              </el-button>
              <el-button type="danger" size="small" @click="handleAudit(row, false)">
                拒绝
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
import { getAdminUserList, auditUser, deleteUser } from '@/api/modules/admin';
import type { User } from '@/api/types/user';

const list = ref<User[]>([]);
const loading = ref(false);
const total = ref(0);
const page = ref(1);
const size = ref(10);
const keyword = ref('');
const filterRole = ref('');
const filterState = ref('');

const getRoleType = (role: string) => {
  const map: Record<string, string> = { admin: 'danger', seller: 'success', buyer: 'info' };
  return map[role] || 'info';
};

const getRoleLabel = (role: string) => {
  const map: Record<string, string> = { admin: '管理员', seller: '卖家', buyer: '买家' };
  return map[role] || role;
};

const getStateType = (state: string) => {
  const map: Record<string, string> = { '待审核': 'warning', '已通过': 'success', '已拒绝': 'danger' };
  return map[state] || 'info';
};

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getAdminUserList({
      keyword: keyword.value || undefined,
      userRole: filterRole.value || undefined,
      examineState: filterState.value || undefined,
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
  filterRole.value = '';
  filterState.value = '';
  page.value = 1;
  fetchData();
};

const handleAudit = async (row: User, passed: boolean) => {
  const action = passed ? '通过' : '拒绝';
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 "${row.username}" 的申请吗？`,
      '确认审核',
      { confirmButtonText: '确定', cancelButtonText: '取消' }
    );
    await auditUser(row.userId, { passed });
    ElMessage.success(`审核${action}成功`);
    fetchData();
  } catch (error) {
    // 用户取消
  }
};

const handleDelete = async (row: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.username}" 吗？`,
      '确认删除',
      { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }
    );
    await deleteUser(row.userId);
    ElMessage.success('删除成功');
    fetchData();
  } catch (error) {
    // 用户取消
  }
};

onMounted(fetchData);
</script>

<style scoped>
.user-manage {
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
