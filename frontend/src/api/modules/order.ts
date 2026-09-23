import request from "../request";
import type {
  ApiResponse,
  PageResult,
  Order,
  CreateOrderParams,
  OrderQueryParams,
  ConfirmReceiptParams,
  RateOrderParams,
} from "@/api/types";

export * from "@/api/types/order";

export const createOrder = (
  data: CreateOrderParams,
): Promise<ApiResponse<Order>> => request.post("/order/create", data);

export const getOrderList = (
  params: OrderQueryParams,
): Promise<ApiResponse<PageResult<Order>>> =>
  request.get("/order/list", { params });

export const getOrderDetail = (id: number): Promise<ApiResponse<Order>> =>
  request.get(`/order/${id}`);

export const payOrder = (
  id: number,
): Promise<ApiResponse<{ payUrl: string }>> => request.post(`/order/${id}/pay`);

export const cancelOrder = (id: number): Promise<ApiResponse<null>> =>
  request.put(`/order/${id}/cancel`);

// 确认收货：待收货 -> 已完成
export const confirmReceipt = (
  id: number,
  data: ConfirmReceiptParams,
): Promise<ApiResponse<null>> => request.put(`/order/${id}/confirm`, data);

// 评价：已完成 -> 已评价
export const rateOrder = (
  id: number,
  data: RateOrderParams,
): Promise<ApiResponse<null>> => request.put(`/order/${id}/rate`, data);

// 卖家发货：待发货 -> 待收货
export const shipOrder = (id: number): Promise<ApiResponse<null>> =>
  request.put(`/order/${id}/ship`);

// 卖家订单列表
export const getSellerOrderList = (
  params: OrderQueryParams,
): Promise<ApiResponse<PageResult<Order>>> =>
  request.get("/seller/orders", { params });

export const deleteOrder = (id: number): Promise<ApiResponse<null>> =>
  request.delete(`/order/${id}`);
