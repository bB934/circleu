import request from '../request';
import type { ApiResponse, Seller, UpdateSellerParams } from '@/api/types';

// 获取当前登录卖家的信息
export const getSellerInfo = (): Promise<ApiResponse<Seller>> =>
  request.get('/seller/info');

// 更新当前登录卖家的信息
export const updateSellerInfo = (data: UpdateSellerParams): Promise<ApiResponse<Seller>> =>
  request.put('/seller/info', data);
