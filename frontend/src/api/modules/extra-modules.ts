import request from '../request';
import type { ApiResponse, PageResult } from '../types/common';

// ---------- Announcement Management ----------
export interface Announcement {
  title: string;
  content: string;
  status?: number;
}

export const getAnnouncements = (): Promise<ApiResponse<Announcement[]>> => 
  request.get('/announcements');

export const addAnnouncement = (data: Omit<Announcement, 'status'>): Promise<ApiResponse<null>> => 
  request.post('/admin/announcement', data);

// ---------- Carousel Management ----------
export interface Carousel {
  title?: string;
  imageUrl: string;
  linkUrl?: string;
  sortOrder?: number;
}

export const getCarousels = (): Promise<ApiResponse<Carousel[]>> => 
  request.get('/carousels');

export const addCarousel = (data: Omit<Carousel, 'carousel_id'>): Promise<ApiResponse<null>> => 
  request.post('/admin/carousel', data);

// ---------- Campus News ----------
export interface NewsCategory {
  category_name: string;
  sort_order?: number;
}

export interface NewsItem {
  categoryId: number;
  title: string;
  summary?: string;
  content: string;
  coverImg?: string;
  hits: number;
  status?: number;
}

export const getNewsList = (params?: { page?: number; size?: number; status?: number }): Promise<ApiResponse<PageResult<NewsItem>>> => 
  request.get('/news/list', { params });

export const getNewsDetail = (id: number): Promise<ApiResponse<NewsItem>> => 
  request.get(`/news/${id}`);

// ---------- Comment Management ----------
export interface Comment {
  commentId: number;
  userId: number;
  nickname: string;
  avatar?: string;
  content: string;
  createTime: string;
  reply?: string;
}

export interface CommentPayload {
  content: string;
  sourceTable: string;
  sourceField: string;
  sourceId: number;
}

export const getComments = (sourceId: number): Promise<ApiResponse<Comment[]>> =>
  request.get(`/goods/${sourceId}/comments`);

export const addComment = (data: CommentPayload): Promise<ApiResponse<null>> =>
  request.post('/comment', data);

// ---------- 商品买家评价（来自订单表，status = 已评价） ----------
export interface GoodsReview {
  reviewId: number;
  goodsId: number;
  userId: number;
  nickname: string;
  avatar?: string;
  rating: number;
  content: string;
  createTime: string;
}

export interface GoodsReviewResult {
  list: GoodsReview[];
  total: number;
  avgRating: number;
}

export const getGoodsReviews = (goodsId: number): Promise<ApiResponse<GoodsReviewResult>> =>
  request.get(`/goods/${goodsId}/reviews`);

export const deleteGoodsReview = (reviewId: number): Promise<ApiResponse<null>> =>
  request.delete(`/goods/reviews/${reviewId}`);

export const deleteReviewByOrder = (orderId: number): Promise<ApiResponse<null>> =>
  request.delete(`/goods/reviews/order/${orderId}`);

// ---------- Forum Post Management ----------
export interface ForumPost {
  title: string;
  content: string;
  category?: string;
}

export const getForumPosts = (params?: { category?: string }): Promise<ApiResponse<PageResult<ForumPost>>> => 
  request.get('/forum_posts', { params });

export const addForumPost = (data: Omit<ForumPost, 'post_id'>): Promise<ApiResponse<null>> => 
  request.post('/forum_post', data);