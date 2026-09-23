import request from '../request';
import type { ApiResponse, PageResult } from '@/api/types/common';
import type { User, Goods, Order } from '@/api/types';

// ============ 类型定义 ============
export interface AdminStats {
  totalUsers: number;
  totalGoods: number;
  totalOrders: number;
  pendingUsers?: number;
  pendingGoods: number;
  pendingOrders: number;
}

export interface AuditParams {
  passed: boolean;
  remark?: string;
}

// ============ API 接口 ============

// 统计数据
export const getAdminStats = (): Promise<ApiResponse<AdminStats>> =>
  request.get('/admin/stats');

// 用户管理
export const getAdminUserList = (params: {
  keyword?: string;
  role?: string;
  examineState?: string;
  page?: number;
  size?: number;
}): Promise<ApiResponse<PageResult<User>>> =>
  request.get('/admin/users', { params });

export const auditUser = (id: number, data: AuditParams): Promise<ApiResponse<null>> =>
  request.put(`/admin/users/${id}/audit`, data);

export const deleteUser = (id: number): Promise<ApiResponse<null>> =>
  request.delete(`/admin/users/${id}`);

// 商品管理
export const getAdminGoodsList = (params: {
  keyword?: string;
  status?: number;
  page?: number;
  size?: number;
}): Promise<ApiResponse<PageResult<Goods>>> =>
  request.get('/admin/goods', { params });

export const auditGoods = (id: number, data: AuditParams): Promise<ApiResponse<null>> =>
  request.put(`/admin/goods/${id}/audit`, data);

export const deleteGoods = (id: number): Promise<ApiResponse<null>> =>
  request.delete(`/admin/goods/${id}`);

// 订单管理
export const getAdminOrderList = (params: {
  status?: string;
  page?: number;
  size?: number;
}): Promise<ApiResponse<PageResult<Order>>> =>
  request.get('/admin/orders', { params });

export const forceRefund = (id: number): Promise<ApiResponse<null>> =>
  request.put(`/admin/orders/${id}/refund`);
