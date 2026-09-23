export interface Order {
  orderId: number;
  orderNumber: string;
  goodsId: number;
  title: string;
  img: string;
  price: number;
  priceAgo: number;
  num: number;
  priceCount: number;
  norms: string;
  type: string;
  contactName: string;
  contactPhone: string;
  contactAddress: string;
  postalCode: string;
  userId: number;
  merchantId: number;
  description: string;
  status: '待付款' | '待发货' | '待收货' | '已完成' | '已评价' | '已取消';
  createTime: string;
  updateTime: string;
}

export interface CreateOrderParams {
  cartIds: number[];
  addressId: number;
  remark?: string;
}

export interface OrderQueryParams {
  page: number;
  size: number;
  keyword?: string;
  status?: '待付款' | '待发货' | '待收货' | '已完成' | '已评价' | '已取消';
}

export interface ConfirmReceiptParams {
  starRating: number;
  remarks?: string;
}

export interface RateOrderParams {
  starRating: number;
  remarks?: string;
}