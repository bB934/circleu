import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { login as loginApi, getUserInfo } from '@/api/modules/user';
import type { User, LoginParams } from '@/api/types/user';

export const useUserStore = defineStore('user', () => {
  const token = ref('');
  const userInfo = ref<User | null>(null);

  const isLoggedIn = computed(() => !!token.value);
  const isAdmin = computed(() => userInfo.value?.role === 'admin');
  const isSeller = computed(() => userInfo.value?.role === 'seller');
  const isBuyer = computed(() => userInfo.value?.role === 'buyer');

  const login = async (loginData: LoginParams) => {
    const res = await loginApi(loginData);
    token.value = res.data.token;
    await fetchUserInfo();
    return res;
  };

  const fetchUserInfo = async () => {
    if (!token.value) return;
    const res = await getUserInfo();
    userInfo.value = res.data;
  };

  const logout = () => {
    token.value = '';
    userInfo.value = null;
  };

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    isSeller,
    isBuyer,
    login,
    fetchUserInfo,
    logout,
  };
}, {
  persist: {
    key: 'user-store',
    storage: localStorage,
    paths: ['token', 'userInfo'],
  },
});