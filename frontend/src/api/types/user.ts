export interface User {
  userId: number;
  username: string;
  email: string;
  phone: string;
  gender: string;
  avatar: string;
  role: 'admin' | 'seller' | 'buyer';
  examineState: string;
  createTime: string;
}

export interface LoginParams {
  username: string;
  password: string;
}

export interface LoginResult {
  token: string;
  userInfo: User;
}

export interface RegisterParams {
  username: string;
  password: string;
  email: string;
  phone: string;
  role?: 'buyer' | 'seller';
}

export interface Address {
  addressId: number;
  userId: number;
  name: string;
  phone: string;
  postcode: string;
  address: string;
  isDefault: boolean;
}

export interface UpdateUserParams {
  email?: string;
  phone?: string;
  gender?: string;
  avatar?: string;
}

export interface ChangePasswordParams {
  oldPassword: string;
  newPassword: string;
  confirmPassword?: string;
}

export interface UpdateAddressParams {
  addressId: number;
  name?: string;
  phone?: string;
  postcode?: string;
  address?: string;
  isDefault?: boolean;
}

export interface ApplySellerParams {
  realName: string;
  phone: string;
  gender: string;
  age: number;
  school: string;
  address: string;
  birthday?: string;
  introduction?: string;
}

export interface SellerApplication {
  applicationId: number;
  userId: number;
  realName: string;
  phone: string;
  gender: string;
  age: number;
  school: string;
  address: string;
  birthday: string;
  introduction: string;
  status: 'pending' | 'approved' | 'rejected';
  remark: string;
  createTime: string;
  updateTime: string;
}

export interface ApplyStatusResponse {
  status: 'none' | 'pending' | 'approved' | 'rejected';
  message: string;
  application?: SellerApplication;
}

export interface ApplicationListParams {
  status?: string;
  keyword?: string;
  page?: number;
  size?: number;
}

export interface AuditApplicationParams {
  passed: boolean;
  remark?: string;
}