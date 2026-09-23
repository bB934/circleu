import { createRouter, createWebHistory } from "vue-router";
import type { RouteRecordRaw } from "vue-router";

const routes: RouteRecordRaw[] = [
  {
    path: "/",
    name: "Home",
    component: () => import("@/views/home/Index.vue"),
  },
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/user/Login.vue"),
    meta: { guest: true },
  },
  {
    path: "/register",
    name: "Register",
    component: () => import("@/views/user/Register.vue"),
    meta: { guest: true },
  },
  {
    path: "/goods/list",
    name: "GoodsList",
    component: () => import("@/views/goods/List.vue"),
  },
  {
    path: "/goods/detail/:id",
    name: "GoodsDetail",
    component: () => import("@/views/goods/Detail.vue"),
  },
  {
    path: "/goods/publish",
    name: "GoodsPublish",
    component: () => import("@/views/goods/Publish.vue"),
    meta: { auth: true, role: "seller" },
  },
  {
    path: "/cart",
    name: "Cart",
    component: () => import("@/views/cart/Index.vue"),
    meta: { auth: true },
  },
  {
    path: "/order/confirm",
    name: "OrderConfirm",
    component: () => import("@/views/order/Confirm.vue"),
    meta: { auth: true },
  },
  {
    path: "/order/list",
    name: "OrderList",
    component: () => import("@/views/order/List.vue"),
    meta: { auth: true },
  },
  {
    path: "/order/detail/:id",
    name: "OrderDetail",
    component: () => import("@/views/order/Detail.vue"),
    meta: { auth: true },
  },
  {
    path: "/user/profile",
    name: "UserProfile",
    component: () => import("@/views/user/Profile.vue"),
    meta: { auth: true },
  },
  {
    path: "/apply-seller",
    name: "ApplySeller",
    component: () => import("@/views/user/ApplySeller.vue"),
    meta: { auth: true },
  },
  {
    path: "/seller/info",
    name: "SellerInfo",
    component: () => import("@/views/seller/Info.vue"),
    meta: { auth: true, role: "seller" },
  },
  {
    path: "/seller/orders",
    name: "SellerOrders",
    component: () => import("@/views/seller/OrderManage.vue"),
    meta: { auth: true, role: "seller" },
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/Index.vue'),
    meta: { auth: true, role: 'admin' },
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('@/views/admin/UserManage.vue'),
    meta: { auth: true, role: 'admin' },
  },
  {
    path: '/admin/goods',
    name: 'AdminGoods',
    component: () => import('@/views/admin/GoodsManage.vue'),
    meta: { auth: true, role: 'admin' },
  },
  {
    path: '/admin/orders',
    name: 'AdminOrders',
    component: () => import('@/views/admin/OrderManage.vue'),
    meta: { auth: true, role: 'admin' },
  },
  {
    path: '/admin/seller-applications',
    name: 'AdminSellerApplications',
    component: () => import('@/views/admin/SellerAudit.vue'),
    meta: { auth: true, role: 'admin' },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
