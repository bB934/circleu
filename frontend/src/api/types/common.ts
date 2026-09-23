export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

export interface PageParams {
  page: number;
  size: number;
  keyword?: string;
}

export interface PageResult<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}