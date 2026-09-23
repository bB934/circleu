import request from '../request';
import type {
  ApiResponse,
  PageResult,
  Goods,
  Category,
  GoodsQueryParams,
  PublishGoodsParams,
} from '@/api/types';

export * from '@/api/types/goods';

export const getGoodsList = (params: GoodsQueryParams): Promise<ApiResponse<PageResult<Goods>>> =>
  request.get('/goods/list', { params });

export const getGoodsDetail = (id: number): Promise<ApiResponse<Goods>> =>
  request.get(`/goods/${id}`);

export const publishGoods = (data: PublishGoodsParams): Promise<ApiResponse<Goods>> =>
  request.post('/goods', data);

export const updateGoods = (id: number, data: Partial<PublishGoodsParams>): Promise<ApiResponse<Goods>> =>
  request.put(`/goods/${id}`, data);

export const deleteGoods = (id: number): Promise<ApiResponse<null>> =>
  request.delete(`/goods/${id}`);

export const getCategories = (): Promise<ApiResponse<Category[]>> =>
  request.get('/goods/categories');

export const favoriteGoods = (id: number): Promise<ApiResponse<null>> =>
  request.post(`/goods/${id}/favorite`);

export const unfavoriteGoods = (id: number): Promise<ApiResponse<null>> =>
  request.delete(`/goods/${id}/favorite`);

export const getFavorites = (): Promise<ApiResponse<Goods[]>> =>
  request.get('/goods/favorites');

// 获取多个卖家的首张发布图（作为缺封面商品的兜底），返回 sellerId -> coverImg
export const getSellerFirstImages = (
  userIds: number[],
): Promise<ApiResponse<Record<number, string>>> =>
  request.get('/seller/first-images', {
    params: { userIds },
    paramsSerializer: (params) =>
      `userIds=${(params.userIds as number[]).join(',')}`,
  });