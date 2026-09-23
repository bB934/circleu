# CircleU - 校园二手交易平台

基于 Spring Boot 微服务架构 + Vue3 前端的校园二手交易平台。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3 + Spring Security + JWT |
| 微服务 | 5 个业务微服务 + API 网关 + 公共模块 |
| 消息队列 | RocketMQ（延迟消息、订单削峰） |
| 缓存 | Redis + Redisson |
| 数据库 | MySQL + MyBatis-Plus |
| 限流/熔断 | Sentinel |
| 前端 | Vue 3 + TypeScript + Vite + Element Plus |
| 容器化 | Docker + Docker Compose |

## 项目架构

```
circleu/
├── circle-common/          # 公共模块（DTO、Entity、Mapper、工具类）
├── circle-user-service/    # 用户服务
├── circle-goods-service/   # 商品服务
├── circle-order-service/   # 订单服务
├── circle-content-service/ # 内容服务（公告、轮播、新闻）
├── circle-gateway/         # API 网关
├── frontend/               # Vue3 前端项目
├── docker-compose.yml      # 一键部署
└── pom.xml                 # 父工程
```

## 功能模块

### 前端
- **首页** (home) - 商品浏览、搜索、轮播图
- **商品** (goods) - 商品详情、发布、发布管理
- **购物车** (cart) - 购物车管理
- **订单** (order) - 下单、订单详情、订单列表
- **用户** (user) - 登录注册、个人信息、卖家申请
- **卖家** (seller) - 卖家中心、订单管理
- **后台管理** (admin) - 用户管理、商品管理、订单管理、审核

### 后端
- 用户注册登录（JWT 鉴权）
- 商品发布与管理
- 购物车 + 下单流程
- 订单状态流转 + 延迟消息（自动关闭超时订单）
- 公告/轮播/新闻管理
- 卖家入驻审核
- 后台数据统计

## 快速开始

### 方式一：Docker 一键部署

```bash
docker-compose up -d
```

### 方式二：本地运行

1. 启动 MySQL、Redis、RocketMQ
2. 导入 SQL：`circle-common/src/main/resources/circleu.sql`
3. 依次启动各微服务（Spring Boot 3）
4. 启动前端：`cd frontend && npm install && npm run dev`

## 开发说明

- 后端端口：各服务 8080 + 服务序号（具体见 application.yml）
- 前端端口：5173（Vite 默认）
- 数据库配置：`application.yml` 中修改
- SQL 脚本位置：`circle-common/src/main/resources/circleu.sql`

## 文档

- [导学文档](./导学-CircleU.md)
- [数据库设计文档](./数据库设计文档.md)

## 许可证

仅用于学习交流。
