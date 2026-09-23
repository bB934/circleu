<template>
  <div class="profile-address">
    <div class="address-header">
      <h2>收货地址</h2>
      <el-button type="primary" size="small" @click="showAddDialog = true">
        添加地址
      </el-button>
    </div>

    <!-- 地址列表 -->
    <div v-loading="loading">
      <div v-if="list.length === 0" class="empty-address">
        <el-empty description="暂无收货地址" />
      </div>
      <div v-else class="address-list">
        <div
          v-for="item in list"
          :key="item.addressId"
          class="address-item"
          :class="{ default: item.isDefault }"
        >
          <div class="address-info">
            <span class="name">{{ item.name }}</span>
            <span class="phone">{{ item.phone }}</span>
            <el-tag v-if="item.isDefault" type="success" size="small">默认</el-tag>
            <p class="address">{{ item.address }}</p>
          </div>
          <div class="address-actions">
            <el-button type="primary" link size="small" @click="editAddress(item)">
              编辑
            </el-button>
            <el-button
              v-if="!item.isDefault"
              type="success"
              link
              size="small"
              @click="setDefault(item)"
            >
              设为默认
            </el-button>
            <el-button type="danger" link size="small" @click="handleDeleteAddress(item)">
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingId ? '编辑地址' : '添加地址'"
      width="500px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="收货人" prop="name">
          <el-input v-model="form.name" placeholder="请输入收货人姓名" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="邮政编码" prop="postcode">
          <el-input v-model="form.postcode" placeholder="请输入邮政编码" />
        </el-form-item>

        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入详细地址" />
        </el-form-item>

        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAddress">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import {
  getAddressList,
  addAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress,
} from '@/api/modules/user';
import type { Address } from '@/api/types/user';

const list = ref<Address[]>([]);
const loading = ref(false);
const submitting = ref(false);
const showAddDialog = ref(false);
const editingId = ref<number | null>(null);

const formRef = ref<FormInstance>();

const form = reactive({
  name: '',
  phone: '',
  postcode: '',
  address: '',
  isDefault: false,
});

const rules: FormRules = {
  name: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
};

const fetchAddresses = async () => {
  loading.value = true;
  try {
    const res = await getAddressList();
    list.value = res.data || [];
  } catch (error) {
    ElMessage.error('加载地址失败');
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  editingId.value = null;
  form.name = '';
  form.phone = '';
  form.postcode = '';
  form.address = '';
  form.isDefault = false;
};

const submitAddress = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch {
    return;
  }

  submitting.value = true;
  try {
    if (editingId.value) {
      await updateAddress(editingId.value, { ...form });
      ElMessage.success('地址更新成功');
    } else {
      await addAddress({ ...form });
      ElMessage.success('地址添加成功');
    }
    showAddDialog.value = false;
    await fetchAddresses();
  } catch (error) {
    ElMessage.error('操作失败');
  } finally {
    submitting.value = false;
  }
};

const editAddress = (item: Address) => {
  editingId.value = item.addressId;
  form.name = item.name;
  form.phone = item.phone;
  form.postcode = item.postcode || '';
  form.address = item.address;
  form.isDefault = item.isDefault;
  showAddDialog.value = true;
};

const setDefault = async (item: Address) => {
  try {
    await setDefaultAddress(item.addressId);
    ElMessage.success('设置默认地址成功');
    await fetchAddresses();
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

const handleDeleteAddress = async (item: Address) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await deleteAddress(item.addressId);
    ElMessage.success('删除成功');
    await fetchAddresses();
  } catch (error) {
    // 用户取消
  }
};

onMounted(fetchAddresses);
</script>

<style scoped>
.profile-address h2 {
  margin: 0;
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
  margin-bottom: 16px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #eee;
  border-radius: 8px;
  transition: border-color 0.3s;
}

.address-item.default {
  border-color: #67c23a;
  background: #f0f9f0;
}

.address-info .name {
  font-weight: 600;
  margin-right: 12px;
}

.address-info .phone {
  color: #666;
  margin-right: 12px;
}

.address-info .address {
  margin: 8px 0 0;
  color: #666;
}

.address-actions {
  display: flex;
  gap: 8px;
}
</style>
