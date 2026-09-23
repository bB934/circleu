<template>
  <div class="seller-info">
    <h2>卖家信息</h2>

    <el-alert
      v-if="!isComplete"
      type="warning"
      :closable="false"
      show-icon
      title="请完善您的卖家信息"
      description="完善卖家资料有助于买家建立信任，提升成交率。"
      class="tip"
    />

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      v-loading="loading"
    >
      <el-form-item label="卖家编号">
        <el-input :value="seller?.sellerNumber || '—'" disabled />
      </el-form-item>

      <el-form-item label="性别" prop="sellerGender">
        <el-radio-group v-model="form.sellerGender">
          <el-radio value="男">男</el-radio>
          <el-radio value="女">女</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="年龄" prop="sellerAge">
        <el-input-number v-model="form.sellerAge" :min="16" :max="100" />
      </el-form-item>

      <el-form-item label="学校" prop="sellerSchool">
        <el-input v-model="form.sellerSchool" placeholder="请输入所在学校" />
      </el-form-item>

      <el-form-item label="地址" prop="sellerAddress">
        <el-input v-model="form.sellerAddress" placeholder="请输入详细地址" />
      </el-form-item>

      <el-form-item label="生日" prop="sellerBirthday">
        <el-date-picker
          v-model="form.sellerBirthday"
          type="date"
          placeholder="选择出生日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="个人简介" prop="briefIntroduction">
        <el-input
          v-model="form.briefIntroduction"
          type="textarea"
          :rows="4"
          placeholder="介绍您的经营类目、特色与承诺（选填）"
        />
      </el-form-item>

      <el-form-item label="信用分">
        <el-input :value="seller?.creditScore ?? '—'" disabled />
      </el-form-item>

      <el-form-item label="审核状态">
        <el-tag :type="examineTagType">{{ examineLabel }}</el-tag>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isComplete ? '保存修改' : '提交资料' }}
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { getSellerInfo, updateSellerInfo } from '@/api/modules/seller';
import type { Seller } from '@/api/types/seller';

const seller = ref<Seller | null>(null);
const formRef = ref<FormInstance>();
const loading = ref(false);
const submitting = ref(false);

const form = reactive({
  sellerGender: '',
  sellerAge: 18,
  sellerSchool: '',
  sellerAddress: '',
  sellerBirthday: '',
  briefIntroduction: '',
});

const rules: FormRules = {
  sellerGender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  sellerAge: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { type: 'number', min: 16, max: 100, message: '年龄需在 16-100 之间', trigger: 'blur' },
  ],
  sellerSchool: [{ required: true, message: '请输入学校', trigger: 'blur' }],
  sellerAddress: [{ required: true, message: '请输入地址', trigger: 'blur' }],
};

const isComplete = computed(() => {
  const s = seller.value;
  if (!s) return false;
  return !!(s.sellerGender && s.sellerSchool && s.sellerAddress && s.sellerBirthday);
});

const examineLabel = computed(() => seller.value?.examineState || '未知');
const examineTagType = computed(() => {
  const map: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    '已通过': 'success',
    '待审核': 'warning',
    '未通过': 'danger',
  };
  return map[seller.value?.examineState || ''] || 'info';
});

const syncForm = () => {
  const s = seller.value;
  if (!s) return;
  form.sellerGender = s.sellerGender || '';
  form.sellerAge = s.sellerAge ?? 18;
  form.sellerSchool = s.sellerSchool || '';
  form.sellerAddress = s.sellerAddress || '';
  form.sellerBirthday = s.sellerBirthday || '';
  form.briefIntroduction = s.briefIntroduction || '';
};

const fetchInfo = async () => {
  loading.value = true;
  try {
    const res = await getSellerInfo();
    seller.value = res.data;
    syncForm();
  } catch (error) {
    ElMessage.error('获取卖家信息失败');
  } finally {
    loading.value = false;
  }
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch {
    return;
  }

  submitting.value = true;
  try {
    const res = await updateSellerInfo({
      sellerGender: form.sellerGender,
      sellerAge: form.sellerAge,
      sellerSchool: form.sellerSchool,
      sellerAddress: form.sellerAddress,
      sellerBirthday: form.sellerBirthday,
      briefIntroduction: form.briefIntroduction,
    });
    seller.value = res.data;
    ElMessage.success(isComplete.value ? '信息更新成功' : '资料提交成功');
  } catch (error) {
    ElMessage.error('提交失败，请重试');
  } finally {
    submitting.value = false;
  }
};

onMounted(fetchInfo);
</script>

<style scoped>
.seller-info h2 {
  margin-top: 0;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.tip {
  margin-bottom: 20px;
}
</style>
