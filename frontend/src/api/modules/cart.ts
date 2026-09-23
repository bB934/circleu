import request from '../request';
import type {
  ApiResponse,
  CartItem,
  AddToCartParams,
  UpdateCartParams,
} from '@/api/types';

export * from '@/api/types/cart';

export const getCartList = (): Promise<ApiResponse<CartItem[]>> =>
  request.get('/cart/list');

export const addToCart = (data: AddToCartParams): Promise<ApiResponse<null>> =>
  request.post('/cart', data);

export const updateCart = (cartId: number, data: UpdateCartParams): Promise<ApiResponse<null>> =>
  request.put(`/cart/${cartId}`, data);

export const deleteCartItem = (cartId: number): Promise<ApiResponse<null>> =>
  request.delete(`/cart/${cartId}`);

export const selectAllCart = (selected: boolean): Promise<ApiResponse<null>> =>
  request.put('/cart/select-all', { selected });

export const deleteSelectedCart = (): Promise<ApiResponse<null>> =>
  request.delete('/cart/selected');