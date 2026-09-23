<template>
  <div class="my-goods">
    <h2>我的商品</h2>
    <el-empty description="暂无商品，去发布一件吧" />
    <div class="my-goods-actions">
      <el-button type="primary" @click="$router.push('/goods/publish')">
        发布商品
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts"></script>

<style scoped>
.my-goods h2 {
  margin-top: 0;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.my-goods-actions {
  text-align: center;
  margin-top: 16px;
}
</style>
