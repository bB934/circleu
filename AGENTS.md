# AGENTS.md — 二手交易后台（secondhand-trading）

面向接手本项目的 AI/开发者。业务为中文二手交易平台后端。

## 1. 基本信息
- 项目名/包：`secondhand-trading`，根包 `com.secondhand`
- 框架：Spring Boot 3.2.5（**Java 21**，Maven 构建）
- 持久层：MyBatis + MyBatis-Spring-Boot 3.0.4，XML Mapper 在 `src/main/resources/mapper/*.xml`
- 分页：PageHelper（`helper-dialect: mysql`，`reasonable: true`）
- 数据源：Druid，MySQL 库 `second_hand_trading`（本地 `localhost:3306`，账号 root）
- 鉴权：JJWT 自研 JWT；`JwtInterceptor` 解析 token 后将 `userId` 放入 `RequestAttribute`，Controller 用 `@RequestAttribute("userId")` 取当前用户
- 安全：Spring Security 只做 CORS/放行；业务权限在 Service 层手校（校验 `userId`/`merchantId` 归属）
- 工具：Lombok、`IdGenerator`（订单号等）、`PasswordUtil`、`FileUploadUtil`、`AvatarUtil`、`JwtUtil`

## 2. 强制约定（务必沿用）
- 统一响应 `ApiResponse<T>`（`success` / `error` 静态方法）
- 分页 `PageResult<T>`（list, total, page, size）
- 业务异常 `BusinessException`，由 `GlobalExceptionHandler` 兜底
- 状态值用**中文字符串**（如 `待付款`），`enums/` 仅作参考，DB/代码以字符串字面量为主
- 校验注解用 **`jakarta.validation`**（不是 `javax.validation`）
- 编译必须用 **JDK 21**：默认 `JAVA_HOME` 是 JDK 8 会报 `--release` 无效，需 `JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home ./mvnw compile`

## 3. 目录结构
```
src/main/java/com/secondhand/
  entity/        17 个实体（与表一一对应）
  mapper/         17 个 Mapper 接口 + resources/mapper/*.xml
  dto/request/    入参 DTO（jakarta.validation 校验）
       response/  出参 DTO（如 OrderResponse.fromEntity）
  service/ + impl/ 业务逻辑
  controller/     9 个 Controller
  common/         ApiResponse / PageResult / BusinessException / GlobalExceptionHandler
  config/         JwtInterceptor / WebConfig / SecurityConfig
  util/           IdGenerator / JwtUtil / PasswordUtil / FileUploadUtil / AvatarUtil
  SecondHandApplication.java  启动类
```
`src/test` 当前为空。

## 4. 数据模型（17 张表）
`users` / `buyers` / `sellers` / `addresses` / `categories` / `second_hand_mall`(商品，主键 `second_hand_mall_id`) / `favorites` / `cart` / `orders` / `sign_in_score`(签收/佣金) / `comments` / `announcements` / `carousels` / `news` + `news_categories` / `forum_posts` / `seller_applications`。

关键外键：`orders.user_id → users`、`orders.merchant_id → sellers`、`orders.goods_id → second_hand_mall`。

## 5. 各模块 Controller（对外接口）
- `UserController`：注册/登录/改密/资料
- `SellerController`：卖家资料
- `SellerApplicationController`：申请成为卖家 + 管理员审核
- `GoodsController`：商品发布/列表/详情/改/下架（`status` 1=在售，2=已售）
- `CartController`：购物车增删改查
- `OrderController`（买家，`/order`）：
  - `POST /order/create`（按 cartIds 逐条生成订单、扣库存）
  - `GET /order/list`（`status`/`keyword`/`page`/`size`）
  - `GET /order/{id}` 详情
  - `POST /order/{id}/pay` → 返回 `{payUrl}`（当前模拟链接）
  - `PUT /order/{id}/cancel`（仅待付款，回补库存）
  - `PUT /order/{id}/confirm`（待收货→已完成，生成 sign_in_score、商品置已售）
  - `PUT /order/{id}/rate`（已完成→已评价，写 star_rating/remarks，同步 sign_in_score）
  - `DELETE /order/{id}`（仅已完成/已评价/已取消，物理删除）
- `SellerOrderController`（卖家）：
  - `GET /seller/orders`（`merchantId=当前userId`，status/keyword/分页）
  - `PUT /order/{id}/ship`（待发货→待收货，校验归属）
- `AdminController`：统计、强制退款 `forceRefund`（仅待付款/待发货，回补库存→已取消）
- `ExtraController`：聚合/杂项接口

## 6. 订单状态机（强约束，Service 层校验，前端只触发）
```
待付款 --支付(pay)--> 待发货 --发货(ship)--> 待收货 --确认收货(confirm)--> 已完成 --评价(rate)--> 已评价
待付款 --取消(cancel)--> 已取消
```
枚举 `OrderStatusEnum`：`待付款/待发货/待收货/已完成/已取消/已评价`。`orders` 表含 `type`、`star_rating`、`remarks` 三列。

## 7. 坑位与历史（接手必读）
- **下单是“一车一件一单”**：`createOrder` 对购物车每个商品各生成一条订单，返回第一条；并非设计稿“整单合一”版本——有意为之，除非明确要求合一。
- **状态字符串硬编码**于 Service（如 `"待发货"`），改流程时同步 `OrderStatusEnum`、`AdminServiceImpl.forceRefund` 与 DB 存量数据。
- **DB 与脚本需同步**：`circleu.sql` 是建表源，但运行库是独立 MySQL；改表结构后必须手动 `ALTER` 运行库（曾因缺列导致 500）。
- **`sign_in_score` 与 `orders` 通过 `order_number` 关联**，评价/退款逻辑会双写，保持两表一致。
- 文件上传落在 `uploads/`（`application.yaml` 的 `upload.path`，当前 Mac 绝对路径）。

## 8. 当前数据状态（测试参考）
`orders` 共 13 条，目前**全部为 `已评价`**，`star_rating`(1–5) 与 `remarks` 已随机填充；对应 `sign_in_score` 已同步。还原真实流程测试建议 reset 状态。

## 9. 常用命令
```bash
# 编译（必须 JDK 21）
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home ./mvnw compile
# 运行
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home ./mvnw spring-boot:run
```
