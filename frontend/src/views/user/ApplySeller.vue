<template>
  <div class="apply-seller-container">
    <el-card class="apply-card">
      <template #header>
        <div class="header">
          <h2>申请成为卖家</h2>
          <el-tag v-if="applyStatus === 'pending'" type="warning" size="large">
            ⏳ 审核中
          </el-tag>
          <el-tag v-else-if="applyStatus === 'approved'" type="success" size="large">
            ✅ 已通过
          </el-tag>
          <el-tag v-else-if="applyStatus === 'rejected'" type="danger" size="large">
            ❌ 已拒绝
          </el-tag>
        </div>
      </template>

      <!-- 状态展示 -->
      <div v-if="applyStatus !== 'none'" class="status-info">
        <el-alert
          :type="statusAlertType"
          :title="statusMessage"
          :closable="false"
          show-icon
        />
        <div v-if="applyStatus === 'rejected'" class="reapply-section">
          <p v-if="application?.remark" class="reject-reason">
            拒绝原因：{{ application.remark }}
          </p>
          <el-button type="primary" @click="reapply">重新申请</el-button>
        </div>
        <div v-if="applyStatus === 'pending'" class="pending-section">
          <el-button type="danger" @click="handleCancel">取消申请</el-button>
        </div>
        <div v-if="applyStatus === 'approved'" class="approved-section">
          <el-button type="primary" @click="router.push('/seller/info')">
            去完善卖家信息
          </el-button>
        </div>
      </div>

      <!-- 申请表单 -->
      <el-form
        v-else
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="form.age" :min="18" :max="100" placeholder="请输入年龄" />
        </el-form-item>

        <el-form-item label="学校" prop="school">
          <el-input v-model="form.school" placeholder="请输入所在学校" />
        </el-form-item>

        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入详细地址" />
        </el-form-item>

        <el-form-item label="生日" prop="birthday">
          <el-date-picker
            v-model="form.birthday"
            type="date"
            placeholder="选择出生日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="个人简介" prop="introduction">
          <el-input
            v-model="form.introduction"
            type="textarea"
            :rows="4"
            placeholder="请介绍自己的情况和二手交易经验（选填）"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            提交申请
          </el-button>
          <el-button @click="router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { applySeller, getApplyStatus, cancelApply } from '@/api/modules/user';
import { useUserStore } from '@/stores/userStore';
import type { ApplySellerParams, ApplyStatusResponse } from '@/api/types/user';

const router = useRouter();
const userStore = useUserStore();

const formRef = ref<FormInstance>();
const submitting = ref(false);
const applyStatus = ref<'none' | 'pending' | 'approved' | 'rejected'>('none');
const application = ref<ApplyStatusResponse['application'] | null>(null);

const form = reactive<ApplySellerParams>({
  realName: '',
  phone: '',
  gender: '',
  age: 18,
  school: '',
  address: '',
  birthday: '',
  introduction: '',
});

const rules: FormRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { type: 'number', min: 18, max: 100, message: '年龄需在 18-100 之间', trigger: 'blur' },
  ],
  school: [{ required: true, message: '请输入学校', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
};

const statusMessage = computed(() => {
  switch (applyStatus.value) {
    case 'pending': return '您的申请正在审核中，请耐心等待（通常 1-3 个工作日）';
    case 'approved': return '🎉 恭喜您已成为卖家，现在可以发布商品了！';
    case 'rejected': return '您的申请已被拒绝，请根据原因修改后重新申请。';
    default: return '';
  }
});

const statusAlertType = computed(() => {
  switch (applyStatus.value) {
    case 'pending': return 'warning';
    case 'approved': return 'success';
    case 'rejected': return 'error';
    default: return 'info';
  }
});

const fetchStatus = async () => {
  try {
    const res = await getApplyStatus();
    const data = res.data;
    applyStatus.value = data.status;
    application.value = data.application || null;

    if (data.status === 'approved') {
      await userStore.fetchUserInfo();
    }
  } catch (error) {
    console.error('获取申请状态失败:', error);
  }
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate();

  submitting.value = true;
  try {
    await applySeller(form);
    ElMessage.success('申请提交成功，请等待审核');
    await fetchStatus();
  } catch (error: any) {
    ElMessage.error(error?.message || '申请失败，请重试');
  } finally {
    submitting.value = false;
  }
};

const reapply = () => {
  applyStatus.value = 'none';
  form.realName = '';
  form.phone = '';
  form.gender = '';
  form.age = 18;
  form.school = '';
  form.address = '';
  form.birthday = '';
  form.introduction = '';
};

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await cancelApply();
    ElMessage.success('申请已取消');
    await fetchStatus();
  } catch (error) {
    // 用户取消
  }
};

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  fetchStatus();
});
</script>

<style scoped>
.apply-seller-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
  background: #f5f7fa;
}

.apply-card {
  width: 100%;
  max-width: 600px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h2 {
  margin: 0;
}

.status-info {
  margin-bottom: 20px;
}

.reapply-section,
.pending-section,
.approved-section {
  margin-top: 16px;
  text-align: center;
}

.reject-reason {
  color: #f56c6c;
  margin-bottom: 12px;
}
</style>
