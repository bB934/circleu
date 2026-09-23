import axios, { AxiosInstance, AxiosRequestConfig, AxiosError, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/userStore';
import router from '@/router';
import type { ApiResponse } from '@/api/types';

class Request {
  private instance: AxiosInstance;

  constructor(config?: AxiosRequestConfig) {
    this.instance = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
      timeout: 10000,
      ...config,
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    this.instance.interceptors.request.use(
      (config: InternalAxiosRequestConfig) => {
        const userStore = useUserStore();
        if (userStore.token) {
          config.headers.Authorization = `Bearer ${userStore.token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    this.instance.interceptors.response.use(
      (response: AxiosResponse<ApiResponse>) => {
        const res = response.data;
        if (res.code !== 200) {
          ElMessage.error(res.message || '请求失败');
          return Promise.reject(new Error(res.message || '请求失败'));
        }
        // 返回 ApiResponse 本身（含 code/message/data），
        // 与各调用方使用的 ApiResponse<T> 类型及 res.data 取值保持一致
        return res;
      },
      (error: AxiosError<ApiResponse>) => {
        const status = error.response?.status;
        if (status === 401) {
          const userStore = useUserStore();
          const url = error.config?.url || '';
          const isAuthEndpoint = url.includes('/user/login') || url.includes('/user/register');
          // 仅在「原本已登录」且不是登录/注册接口返回 401 时才清登录态，
          // 避免误杀刚登录成功写入的 token 或登录失败（凭证错误）时被登出。
          if (userStore.token && !isAuthEndpoint) {
            userStore.logout();
            if (window.location.pathname !== '/login') {
              router.push('/login');
              ElMessage.error('登录已过期，请重新登录');
            }
          } else if (!isAuthEndpoint) {
            ElMessage.error(error.response?.data?.message || '请先登录');
          }
        } else {
          ElMessage.error(error.response?.data?.message || '网络错误');
        }
        return Promise.reject(error);
      }
    );
  }

  public get<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return this.instance.get(url, config);
  }

  public post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return this.instance.post(url, data, config);
  }

  public put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return this.instance.put(url, data, config);
  }

  public delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return this.instance.delete(url, config);
  }
}

export default new Request();