import type { PageParams } from './common';

export interface Category {
  categoryId: number;
  categoryName: string;
  parentId: number;
}

export interface Goods {
  secondHandMallId: number;
  sellerId: number;
  categoryId: number;
  title: string;
  description: string;
  content: string;
  price: number;
  priceAgo: number;
  inventory: number;
  hits: number;
  status: 0 | 1 | 2;
  coverImg: string;
  img1?: string;
  img2?: string;
  img3?: string;
  img4?: string;
  img5?: string;
  createTime: string;
  updateTime: string;
  sellerName?: string;
  categoryName?: string;
}

export interface GoodsQueryParams extends PageParams {
  categoryId?: number;
  minPrice?: number;
  maxPrice?: number;
  sortBy?: 'price_asc' | 'price_desc' | 'time_desc' | 'hits_desc';
  status?: 0 | 1 | 2;
}

export interface PublishGoodsParams {
  title: string;
  description: string;
  content: string;
  price: number;
  priceAgo?: number;
  inventory: number;
  categoryId: number;
  images: string[];
}