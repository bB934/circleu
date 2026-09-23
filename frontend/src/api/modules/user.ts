import request from '../request';
import type {
  ApiResponse,
  PageResult,
  User,
  LoginParams,
  LoginResult,
  RegisterParams,
  Address,
  UpdateUserParams,
  ChangePasswordParams,
  ApplySellerParams,
  ApplyStatusResponse,
  SellerApplication,
  ApplicationListParams,
  AuditApplicationParams,
} from '@/api/types';

export * from '@/api/types/user';
export * from '@/api/types/common';

export const login = (data: LoginParams): Promise<ApiResponse<LoginResult>> =>
  request.post('/user/login', data);

export const register = (data: RegisterParams): Promise<ApiResponse<null>> =>
  request.post('/user/register', data);

export const getUserInfo = (): Promise<ApiResponse<User>> =>
  request.get('/user/info');

export const updateUserInfo = (data: UpdateUserParams): Promise<ApiResponse<User>> =>
  request.put('/user/info', data);

export const changePassword = (data: ChangePasswordParams): Promise<ApiResponse<null>> =>
  request.put('/user/password', data);

export const getAddressList = (): Promise<ApiResponse<Address[]>> =>
  request.get('/user/addresses');

export const addAddress = (data: Omit<Address, 'addressId' | 'userId' | 'createTime' | 'updateTime'>): Promise<ApiResponse<Address>> =>
  request.post('/user/address', data);

export const updateAddress = (id: number, data: Partial<Omit<Address, 'addressId' | 'userId' | 'createTime' | 'updateTime'>>): Promise<ApiResponse<Address>> =>
  request.put(`/user/address/${id}`, data);

export const deleteAddress = (id: number): Promise<ApiResponse<null>> =>
  request.delete(`/user/address/${id}`);

export const setDefaultAddress = (id: number): Promise<ApiResponse<null>> =>
  request.put(`/user/address/${id}/default`);

// 申请成为卖家
export const applySeller = (data: ApplySellerParams): Promise<ApiResponse<null>> =>
  request.post('/user/apply-seller', data);

// 获取申请状态
export const getApplyStatus = (): Promise<ApiResponse<ApplyStatusResponse>> =>
  request.get('/user/apply-status');

// 取消申请
export const cancelApply = (): Promise<ApiResponse<null>> =>
  request.delete('/user/apply-seller');

// 管理员 - 获取卖家申请列表
export const getApplicationList = (params: ApplicationListParams): Promise<ApiResponse<PageResult<SellerApplication>>> =>
  request.get('/admin/seller-applications', { params });

// 管理员 - 审核卖家申请
export const auditApplication = (id: number, data: AuditApplicationParams): Promise<ApiResponse<null>> =>
  request.put(`/admin/seller-applications/${id}/audit`, data);