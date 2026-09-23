<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { getCategories, publishGoods } from '@/api/modules/goods';
import { useUserStore } from '@/stores/userStore';
import { useAuth } from '@/composables/useAuth';
import type { Category, PublishGoodsParams } from '@/api/types/goods';

const router = useRouter();
const userStore = useUserStore();
const { requireAuth, requireRole } = useAuth();

// 进入页面即校验身份：未登录或非卖家/管理员直接拦截
onMounted(() => {
  fetchCategories();
  if (!requireRole('seller')) return;
});

const formRef = ref<FormInstance>();
const loading = ref(false);
const categories = ref<Category[]>([]);
const imageFiles = ref<File[]>([]);
const imagePreviews = ref<string[]>([]);

const publishForm = reactive<PublishGoodsParams>({
  title: '',
  description: '',
  content: '',
  price: 0,
  priceAgo: 0,
  inventory: 1,
  categoryId: 0,
  images: [],
});

const rules: FormRules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 5, max: 50, message: '标题长度为5-50位', trigger: 'blur' },
  ],
  description: [
    { required: true, message: '请输入商品简述', trigger: 'blur' },
    { max: 200, message: '简述不超过200字', trigger: 'blur' },
  ],
  content: [{ required: true, message: '请输入商品详情', trigger: 'blur' }],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    { type: 'number', message: '请输入有效价格', trigger: 'blur' },
  ],
  priceAgo: [{ type: 'number', message: '请输入有效原价', trigger: 'blur' }],
  inventory: [
    { required: true, message: '请输入库存', trigger: 'blur' },
    { type: 'number', min: 1, message: '库存至少为1', trigger: 'blur' },
  ],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  images: [
    {
      validator: (rule: any, value: any, callback: any) => {
        if (!publishForm.images || publishForm.images.length === 0) {
          callback(new Error('请上传至少一张图片'));
        } else {
          callback();
        }
      },
      trigger: 'change',
    },
  ],
};

const fetchCategories = async () => {
  const res = await getCategories();
  categories.value = res.data;
};

const handleImageChange = (uploadFile: any, uploadFiles: any[]) => {
  // el-upload 在 auto-upload=false 时，原始文件位于 raw；做一个兜底取值避免 raw 为 undefined
  const file: File | undefined =
    uploadFile?.raw ?? uploadFiles?.[uploadFiles.length - 1]?.raw;
  if (!(file instanceof File)) {
    ElMessage.error('文件读取失败，请重试');
    return;
  }
  const isImage =
    (file.type && file.type.startsWith('image/')) ||
    /\.(jpe?g|png|gif|webp|bmp|svg)$/i.test(file.name || '');
  if (!isImage) {
    ElMessage.error('请上传图片文件');
    return;
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB');
    return;
  }
  if (imageFiles.value.length >= 5) {
    ElMessage.error('最多上传 5 张图片');
    return;
  }
  imageFiles.value.push(file);
  const reader = new FileReader();
  reader.onload = (e) => {
    const url = e.target?.result as string;
    imagePreviews.value.push(url);
    // ✅ 同步到表单字段，供 el-form 校验检测
    publishForm.images.push(url);
    formRef.value?.validateField('images');
  };
  reader.readAsDataURL(file);
};

const removeImage = (index: number) => {
  imageFiles.value.splice(index, 1);
  imagePreviews.value.splice(index, 1);
  publishForm.images.splice(index, 1);
  formRef.value?.validateField('images');
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch {
    return;
  }

  if (imageFiles.value.length === 0) {
    ElMessage.error('请至少上传一张商品图片');
    return;
  }

  if (!requireAuth() || !requireRole('seller')) return;

  loading.value = true;
  try {
    // 这里应该先上传图片到服务器/对象存储，获取图片URL
    // 暂时使用本地预览URL作为演示
    publishForm.images = imagePreviews.value;

    await publishGoods(publishForm);
    ElMessage.success('发布成功');
    router.push('/goods/list');
  } catch (error) {
  } finally {
    loading.value = false;
  }
};

const handleCancel = () => {
  ElMessageBox.confirm('确定要放弃发布吗？', '提示', {
    type: 'warning',
  }).then(() => {
    router.back();
  }).catch(() => {});
};
</script>

<template>
  <div class="publish-page">
    <div class="page-header">
      <h1>发布商品</h1>
    </div>

    <el-card class="publish-card">
      <el-form ref="formRef" :model="publishForm" :rules="rules" label-width="100px" class="publish-form">
        <el-form-item label="商品标题" prop="title">
          <el-input v-model="publishForm.title" placeholder="请输入商品标题（5-50字）" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="publishForm.categoryId" placeholder="请选择商品分类" style="width: 300px">
            <el-option v-for="cat in categories" :key="cat.categoryId" :label="cat.categoryName" :value="cat.categoryId" />
          </el-select>
        </el-form-item>

        <el-form-item label="商品简述" prop="description">
          <el-input v-model="publishForm.description" type="textarea" :rows="3" placeholder="请输入商品简述（不超过200字）" maxlength="200" show-word-limit />
        </el-form-item>

        <el-form-item label="商品价格" prop="price">
          <el-row :gutter="10">
            <el-col :span="12">
              <el-input-number v-model="publishForm.price" :min="0.01" :step="0.01" :precision="2" placeholder="当前价" controls-position="right" style="width: 100%" />
            </el-col>
            <el-col :span="12">
              <el-input-number v-model="publishForm.priceAgo" :min="0" :step="0.01" :precision="2" placeholder="原价（选填）" controls-position="right" style="width: 100%" />
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item label="库存数量" prop="inventory">
          <el-input-number v-model="publishForm.inventory" :min="1" placeholder="请输入库存" controls-position="right" style="width: 200px" />
        </el-form-item>

        <el-form-item label="商品图片" prop="images">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="handleImageChange"
            :limit="5"
            :on-exceed="() => ElMessage.warning('最多上传 5 张图片')"
            list-type="picture-card"
          >
            <el-icon v-if="imagePreviews.length < 5"><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">点击或拖拽上传，最多 5 张，单张不超过 5MB</div>
            </template>
          </el-upload>
          <div class="image-previews" v-if="imagePreviews.length > 0">
            <div v-for="(preview, index) in imagePreviews" :key="index" class="preview-item">
              <img :src="preview" />
              <el-icon class="remove-icon" @click="removeImage(index)"><Close /></el-icon>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="商品详情" prop="content">
          <el-input v-model="publishForm.content" type="textarea" :rows="10" placeholder="请输入商品详细描述，支持富文本..." />
          <p class="form-hint">建议包含：商品成色、使用情况、配件清单、交易方式、注意事项等</p>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">发布商品</el-button>
          <el-button @click="handleCancel" style="margin-left: 12px">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.publish-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 20px;
}
.publish-form {
  padding: 10px 0;
}
.upload-demo {
  width: 100%;
}
.image-previews {
  display: flex;
  gap: 10px;
  margin-top: 12px;
  flex-wrap: wrap;
}
.preview-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 4px;
  overflow: hidden;
}
.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.remove-icon {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 14px;
}
.remove-icon:hover {
  background: rgba(0, 0, 0, 0.8);
}
.form-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: #909399;
}
</style>