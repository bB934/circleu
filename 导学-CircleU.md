# 导学-CircleU

> 项目：`circle-microservice`（CircleU 校园二手交易平台，Spring Cloud 微服务后端）
> 框架：Spring Boot 3.2.5 / Java 21 / Spring Cloud 2023.0.2 | 注册中心 Eureka | 网关 Spring Cloud Gateway
> 中间件：MyBatis + MySQL | Redis + Redisson | RocketMQ | Sentinel | OpenFeign
> 定位：从单体演进为微服务架构，独立完成"注册中心 + 网关 + 4 个业务服务 + 公共模块"的拆分与交易闭环

---

## 1. 前置知识（面试高频标注）

| 知识点 | 为何需要 | 在本项目中的位置 | 高频度 |
|---|---|---|---|
| 微服务拆分思路 | 面试第一个问题：为什么拆、怎么拆、拆了之后代价是什么 | 根 `pom.xml` 的 modules、各服务包结构 | 高 |
| Eureka 服务注册与发现 | 所有跨服务调用的基础：服务怎么互相找到 | `circle-registry/src/main/resources/application.yml` | 高 |
| Spring Cloud Gateway 路由与全局过滤器 | 理解统一入口、路由转发、`lb://` 负载均衡 | `circle-gateway/src/main/resources/application.yml`、`gateway/JwtAuthFilter.java` | 高 |
| 网关统一鉴权 + 请求头身份透传 | JWT 只在网关验一次，下游靠 `X-User-Id` 头拿身份 | `JwtAuthFilter.java`、`common/config/GatewayHeaderInterceptor.java`、`MicroServiceWebConfig.java` | 高 |
| OpenFeign 声明式服务间调用 | 订单服务扣库存、查商品全靠 Feign | `common/feign/StockFeignClient.java`、`GoodsFeignClient.java` | 高 |
| 跨服务数据一致性 | 下单跨服务扣库存，本地事务管不住 Redis 和远端；回滚补偿与对账是重点 | `OrderServiceImpl.createOrder`（`registerStockRollbackCompensation`）+ `StockService` | 高 |
| Redis Lua 脚本原子性 | 防超卖核心：GET+比较+DECRBY 为何不被并发打穿 | `circle-common/src/main/resources/lua/decrease_stock.lua`、`goods/service/StockService.java` | 高 |
| Redisson 可重入分布式锁 | Redis 不可用时的降级兜底；锁粒度/超时/释放 | `StockService.decreaseStockWithLock`、`config/RedissonConfig.java` | 高 |
| RocketMQ 延迟消息 | 订单超时自动取消（15 分钟支付 / 24 小时发货两级） | `order/mq/RocketMQOrderProducer.java`、`RocketMQOrderConsumer.java` | 高 |
| Sentinel 流控/熔断 | 下单/支付热点接口 QPS 限制与错误比例熔断 | `common/config/SentinelConfig.java` + `@SentinelResource` | 中高 |
| Spring Cache 注解 + Redis | 商品/分类缓存与失效策略 | `config/RedisConfig.java`、`goods/service/GoodsServiceImpl.java` | 中高 |
| 事务注解与失效边界 | 下单循环、扣库存与订单插入的一致性边界 | `OrderServiceImpl`（`@Transactional`） | 中高 |
| 状态机建模 | 订单 6 状态的合法流转 | `common/enums/OrderStatusEnum.java` + `OrderServiceImpl` 分支 | 高 |
| AOP + 自定义注解 | 操作日志切面、异步落库不阻塞主线程 | `common/aspect/OperationLogAspect.java`、`annotation/OperationLog.java` | 中 |
| MyBatis XML + PageHelper | 分页查询、动态 SQL（Mapper 接口与 XML 集中在公共模块） | `circle-common/src/main/java/com/secondhand/mapper/` | 中 |

---

## 2. 重点亮点与学习顺序（先看这个）

| 亮点标题 | 为什么重要 | 通用技术关键词 | 先看哪些文件 | 建议学习顺序 |
|---|---|---|---|---|
| 单体到微服务的拆分与统一入口治理 | 面试必问：拆分粒度、网关鉴权、身份透传 | 服务拆分、API 网关、全局过滤器、身份透传 | 根 `pom.xml`、`gateway/application.yml`、`JwtAuthFilter.java`、`common/config/GatewayHeaderInterceptor.java` | 1 |
| 服务间通信：OpenFeign + Eureka | 理解声明式调用如何走服务发现与负载均衡 | 注册中心、声明式调用、客户端负载均衡 | `common/feign/StockFeignClient.java`、`GoodsFeignClient.java`、`order/CircleOrderServiceApplication.java` | 2 |
| 高并发防超卖的双层库存扣减 | 最深挖点：Lua 原子扣减 + 锁降级，且已独立成库存能力归属商品服务 | 原子脚本、分布式锁、降级路径、库存一致性 | `goods/service/StockService.java`、`common/resources/lua/decrease_stock.lua` | 3 |
| 订单状态机 + 双超时自动取消 | 完整业务闭环：下单→支付→发货→确认→评价，超时靠 MQ 驱动 | 状态机、延迟消息、幂等消费、库存回补、上下架同步 | `order/service/impl/OrderServiceImpl.java`、`order/mq/*` | 4 |
| 跨服务一致性与降级缺口 | 诚实认知：Feign 无 fallback、扣库存与建单不在同一事务（已有回滚补偿，残余漏补风险待对账） | 分布式事务、事务同步器、本地消息表、对账补偿 | `OrderServiceImpl.createOrder`、`RocketMQOrderConsumer.cancelAndRestore` | 5 |
| 缓存治理与流量治理 | Redis 故障不拖垮主流程；热点接口限流熔断 | Spring Cache、CacheErrorHandler、QPS 流控、熔断降级 | `config/RedisConfig.java`、`GoodsServiceImpl`、`config/SentinelConfig.java` | 6 |

---

## 3. 必备知识点（精简 checklist）

- [ ] 七个 Maven 模块各是什么职责：registry（注册中心）/ gateway（网关）/ user / goods / order / content 四业务服务 / common（实体、DTO、Mapper、Feign、工具、切面全在公共包）
- [ ] 请求全链路：前端 → 网关（8080）→ 路由匹配（`/user/**` `/goods/**` `/order/**,/cart/**` `/admin/**` 等）→ 业务服务（user 8081 / goods 8082 / order 8083 / content 8084）
- [ ] 网关鉴权：`JwtAuthFilter` 白名单放行 → 解析 JWT → 把 `X-User-Id`/`X-User-Role` 头写进转发请求；`GlobalFilter` + `Ordered(-100)` 的含义
- [ ] 下游取身份：`GatewayHeaderInterceptor` 把头转成 RequestAttribute → Controller `@RequestAttribute("userId")`；身份信任边界在内网
- [ ] Feign 调用：`@FeignClient(name="circle-goods-service")` + `/internal/stock/decrease` 等内部接口；走 Eureka 服务名解析 + 客户端负载均衡，不经过网关
- [ ] 下单链路各步顺序与失败点：校验购物车/地址归属 → 逐商品校验在售 → **Feign 调商品服务扣库存**（记为待补偿项） → 本地建单(待付款) → 删购物车 → **syncGoodsStatus 同步上下架** → 发 15min 延迟消息
- [ ] 事务回滚补偿：`registerStockRollbackCompensation` 用 `TransactionSynchronizationManager` 注册 `afterCompletion`，回滚时遍历已扣减列表调 `increaseStock` 补回（补偿自身失败会漏补，需对账兜底）
- [ ] Lua 防超卖脚本返回值语义：`-2` 无库存键、`-1` 库存不足、其余为剩余库存；`decreaseStock` 把剩余库存一路返回给订单服务用于判定售罄
- [ ] 库存降级链路：Redis Lua 异常 → Redisson `tryLock(3, 10, SECONDS)` → 锁内复查 DB 并更新（同样返回剩余库存）
- [ ] 商品上下架同步口径：可售库存 = 数据库 `inventory` − `OrderMapper.sumPendingStock`（待付款/待发货/待收货的 `SUM(num)`）；可售归零置已售(2)、恢复则重新上架(1)；调用点 createOrder/cancelOrder/confirmReceipt/超时消费
- [ ] 延迟消息两种发送分支：`delayLevel`（18 固定级）与 `syncSendDelayTimeSeconds`（24h 长延时）
- [ ] 消费端为何要二次校验订单状态（防延迟消息到来时订单已支付/已取消）
- [ ] 订单状态机：待付款→待发货→待收货→已完成→已评价；待付款→已取消；哪些状态可取消/退款
- [ ] 确认收货后 `sign_in_score` 何时写、以及确认收货如何触发上下架同步；评价后如何双写佣金表与商品评价表（按 order_id upsert）
- [ ] Spring Cache：`@Cacheable`/`@CacheEvict` 用法；差异化 TTL（goods 30min / categories 1h）
- [ ] Sentinel：`blockHandler`（被流控）与 `fallback`（异常/熔断）区别；流控规则 goodsList 50 / goodsDetail 100 / createOrder 20 / payOrder 20
- [ ] AOP 切面如何拿请求上下文、为何用 `CompletableFuture.runAsync` 异步落日志
- [ ] 微服务化的代价清单：共库未拆分、Feign 无降级、internal 接口暴露、docker-compose 未同步微服务版

---

## 4. 推荐阅读（结合仓库）

| 主题 | 通用技术点 | 建议阅读位置 | 预计时间 | 读完能回答什么 |
|---|---|---|---|---|
| 模块划分与依赖 | 微服务拆分粒度 | 根 `pom.xml`（modules）、各服务 `pom.xml` | 10min | 为什么是 4 个业务服务而不是按表拆？公共模块放了什么？ |
| 网关路由与鉴权 | Gateway 全局过滤器、白名单、头注入 | `circle-gateway/src/main/resources/application.yml`、`src/main/java/com/secondhand/gateway/JwtAuthFilter.java` | 20min | 路由怎么配？JWT 在哪验？身份怎么传给下游？ |
| 身份透传与拦截器 | HandlerInterceptor、RequestAttribute | `circle-common/src/main/java/com/secondhand/common/config/GatewayHeaderInterceptor.java`、`MicroServiceWebConfig.java` | 10min | 下游 Controller 为什么能 `@RequestAttribute("userId")`？ |
| 服务间调用 | OpenFeign + Eureka + 负载均衡 | `circle-common/src/main/java/com/secondhand/feign/StockFeignClient.java`、`GoodsFeignClient.java`、`circle-order-service/.../CircleOrderServiceApplication.java` | 15min | Feign 怎么找到目标服务？调用走不走网关？失败会怎样？ |
| 防超卖核心 | Redis 原子扣减 + 分布式锁降级 + 回滚补偿 | `circle-goods-service/src/main/java/com/secondhand/goods/service/StockService.java`、`circle-common/src/main/resources/lua/decrease_stock.lua` | 20min | Lua 为何原子？降级条件与锁超时怎么设？扣减结果如何被上层用来判售罄？ |
| 订单核心链路 | 状态机 + 事务 + 跨服务调用 + 回滚补偿 | `circle-order-service/src/main/java/com/secondhand/order/service/impl/OrderServiceImpl.java` | 30min | 一车多件如何建单？扣库存为什么是 Feign 调用？事务回滚后 Redis 怎么补？ |
| 超时自动取消 | RocketMQ 延迟消息与幂等消费 | `circle-order-service/src/main/java/com/secondhand/order/mq/RocketMQOrderProducer.java`、`RocketMQOrderConsumer.java`、`circle-common/src/main/java/com/secondhand/mq/OrderDelayMessage.java` | 20min | 15min/24h 怎么发？消费端如何防重复取消？ |
| 缓存与序列化 | Spring Cache + Redis 降级 | `circle-common/src/main/java/com/secondhand/config/RedisConfig.java`、`circle-goods-service/.../GoodsServiceImpl.java` | 15min | 哪些缓存、TTL 多少、Redis 挂了会怎样？ |
| Sentinel 治理 | 流控 + 熔断 + block/fallback | `circle-common/src/main/java/com/secondhand/config/SentinelConfig.java`、`OrderServiceImpl` 注解 | 15min | 阈值怎么定？block 与 fallback 谁先触发？ |
| 操作日志 | AOP 注解 + 异步落库 | `circle-common/src/main/java/com/secondhand/aspect/OperationLogAspect.java`、`annotation/OperationLog.java` | 15min | 日志写失败影响主流程吗？为什么异步？ |
| 表结构 | 17 表关系与订单外键 | `circle-common/src/main/resources/circleu.sql` | 20min | orders 与 sign_in_score、goods、sellers 如何关联？为什么四服务还共用一个库？ |
| 项目约定与坑位 | 强制约定、历史坑位 | `AGENTS.md`（注意：其目录结构描述仍偏单体，模块部分以本文档为准） | 10min | 哪些约定必须沿用？哪些是已知待补？ |

---

## 5. 自学提醒

> 若某文件或原理看不懂，请继续追问 AI；本技能负责给学习路径与题目，不提供逐行讲解。建议按"先通整体链路（网关→路由→服务→Feign→中间件），再逐文件精读"的顺序来；启动类上的 `@EnableDiscoveryClient`、`@EnableFeignClients`、`@ComponentScan` 包扫描范围是理解公共模块如何被各服务共享的关键。

---

## 6. 项目技术定位

**后端**。依据：独立完成单体到 Spring Cloud 微服务的拆分（注册中心、网关、4 个业务服务、公共模块），落地了网关统一鉴权、Feign 服务间调用、订单状态机、Redis/RocketMQ/Sentinel 中间件集成，无前端代码参与，专注服务端能力。

---

## 7. 核心原理解析（问题 → 机制 → 在本项目中的落点）

1. **问题：四个服务各自维护登录态，同一套 JWT 解析逻辑要写四遍，且改动要全量发布。**
   机制：把鉴权上收到网关——`JwtAuthFilter` 实现 `GlobalFilter`，对所有路由统一执行"白名单判断 → 取 Authorization 头 → 验签"，通过后用 `request.mutate()` 把 `X-User-Id`/`X-User-Role` 写进转发请求；下游服务用 `GatewayHeaderInterceptor`（注册于 `MicroServiceWebConfig`）把头转成 RequestAttribute，Controller 继续用 `@RequestAttribute` 取值，业务代码零改动。
   落点：`circle-gateway/.../JwtAuthFilter.java`（order=-100 保证最先执行）、`circle-common/.../common/config/GatewayHeaderInterceptor.java`。

2. **问题：订单服务要扣商品服务的库存，两个服务不可能共享同一个本地事务。**
   机制：订单服务通过 `StockFeignClient`（`@FeignClient(name="circle-goods-service")`）声明式调用商品服务的 `/internal/stock/decrease`；Feign 由 Eureka 服务名解析 + 客户端负载均衡找到实例直连，不经过网关。库存扣减失败时 Feign 抛异常 → 订单服务本地事务回滚，"失败即回滚"靠异常传播保证；但 Redis 扣减不参与本地事务，因此 `createOrder` 用 `TransactionSynchronizationManager.registerSynchronization` 注册 `afterCompletion`，事务回滚时遍历已扣减列表调 `increaseStock` 补偿回补。
   落点：`circle-common/.../feign/StockFeignClient.java`、`OrderServiceImpl.createOrder` 的 `stockFeignClient.decreaseStock(...)` 与 `registerStockRollbackCompensation(...)`。

3. **问题：最后一件商品被并发下单导致超卖。**
   机制：Redis 单线程执行 Lua 脚本，`GET` → 比较 → `DECRBY` 一气呵成，天然原子。
   落点：`circle-common/resources/lua/decrease_stock.lua`；调用方 `StockService.decreaseStock`（商品服务内）用 `DefaultRedisScript<Long>` 装载执行，按 `-2/-1` 区分"无库存键/库存不足"并抛业务异常。

4. **问题：Redis 抖动或宕机时，扣库存主路径整体不可用。**
   机制：先走 Redis Lua，捕获非业务异常后降级为 Redisson 可重入锁 + DB 校验更新；`tryLock(3, 10, SECONDS)` 提供等待与持有上限，锁内复查商品状态与库存，解锁前 `isHeldByCurrentThread` 校验持有者。
   落点：`StockService.decreaseStockWithLock`；`RedissonConfig` 提供 client。

5. **问题：用户下单后不支付、支付后卖家不发货，订单与库存长期占用。**
   机制：RocketMQ 延迟消息做"到点检查"：下单发 15 分钟支付超时消息，支付后发 24 小时发货超时消息；消费者收到后二次校验当前状态，仍处目标态才置已取消并回补库存（DB + Feign 调 Redis 回补），天然幂等。
   落点：`OrderDelayMessage`（TYPE_PAY_TIMEOUT / TYPE_SHIP_TIMEOUT）、`RocketMQOrderProducer.toDelayLevel`（≤2h 映射固定级别，24h 走 `syncSendDelayTimeSeconds`）、`RocketMQOrderConsumer.cancelAndRestore`。

6. **问题：商品详情高频读取 + Redis 故障会打穿 DB；写操作需要审计但不能拖慢接口。**
   机制：Spring Cache 声明式缓存 + 自定义 `CacheErrorHandler`，缓存读写清异常统一降级为 DB 访问并告警；操作日志用自定义 `@OperationLog` + `@Aspect` 环绕切面，`CompletableFuture.runAsync` 异步落库，日志自身失败仅 warn。
   落点：`RedisConfig.cacheManager`（差异化 TTL）与 `cacheErrorHandler()`；`OperationLogAspect` 落 `operation_log` 表。

7. **问题：下单只扣库存不改商品状态，会出现"商品仍显示上架、库存却已为 0"，他人点进去才报库存不足。**
   机制：把上下架判断收敛为一个同步方法，口径是"可售库存 = 数据库剩余库存 − 未完结订单占用量"（占用量 = 待付款/待发货/待收货订单的 `SUM(num)`）；可售归零置已售(2)、恢复则重新上架(1)。因为"已售"是业务终态，下单不当场下架（避免取消后翻回），所以改为在这四个节点同步：下单成功后、买家取消、超时取消、确认收货。
   落点：`OrderServiceImpl.syncGoodsStatus`、`OrderMapper.sumPendingStock` + `OrderMapper.xml`；调用方 `OrderServiceImpl.createOrder/cancelOrder/confirmReceipt`、`RocketMQOrderConsumer.cancelAndRestore`。残余风险：判断是"读-算-写"三步，极端并发下仍有窗口；且 Redis 扣减与数据库库存是两条链路，彻底方案是对账。

---

## 8. 关键设计决策

| 决策 | 备选方案 | 取舍 | 风险 / 待确认 | 验证 |
|---|---|---|---|---|
| 按业务域拆 4 个服务（user/goods/order/content）+ 网关 + 注册中心 | 按表拆更细粒度 / 保持单体 | 拆分粒度与团队规模匹配，交易核心（order/goods）独立、内容类边缘功能聚合；公共依赖收敛到 common 模块 | common 模块承担过多（实体+Mapper+Feign+工具），有"公共库变上帝包"倾向 | 各服务可独立编译启动并注册到 Eureka |
| 鉴权上收网关 + 请求头透传 | 各服务重复解析 JWT / 下发内部会话 | 只验一次、下游零侵入 | **内部信任边界**：下游服务直接暴露在内网时可被伪造 `X-User-Id` 头；且网关路由含 `/internal/goods/**`，外部理论可达（待收紧为网关白名单拒绝或内网隔离） | 非白名单请求无 Token 返回 401；伪造头待验证 |
| 库存扣减独立为商品服务能力（Redis Lua + Redisson），订单服务经 Feign 调用 | 订单服务直连 Redis 扣减 | 库存口径收敛到 goods 域，避免两服务各写一份扣减逻辑 | 引入网络调用：Feign 无 fallback 配置，goods 服务不可用则下单整体不可用；跨服务失败时 Redis 已扣库存无法随本地事务回滚——已用事务同步器在回滚时补偿回补，但补偿调用自身失败仍会漏补（对账任务是下一步） | 本地全链路下单/取消/超时取消实测回补正确；回滚补偿逻辑已实现待补并发用例 |
| 商品上下架由"可售库存"统一判定（DB 库存 − 未完结订单占用） | 下单即置已售 / 只在确认收货时判定 | 兼顾"已售是业务终态"与"不留上架零库存窗口" | 判定是"读-算-写"三步，极端并发下仍有窗口；读的是 DB 库存，与 Redis 扣减是两条链路 | 下单买走最后一件 → 立即置已售；取消 → 库存回补后重新上架 |
| 四服务共用一个 MySQL 库 | 按服务拆库 | 课程/作品项目规模下避免分布式事务复杂化，跨服务查询简单 | 数据层仍耦合：表结构变更影响多服务；与"微服务"叙事有差距，面试要能说清 | 待测：若拆库，order 域订单表与 goods 域商品表需冗余哪些字段 |
| JWT 密钥在网关与用户服务各配一份 | 配置中心统一下发 | 实现简单 | 密钥两处手工同步，泄露面翻倍；上线应挪环境变量/配置中心 | 登录签发的 token 可被网关正确验证 |
| 状态用中文字符串字面量 | 枚举/数字码 | 直观、与前端展示零转换 | 改流程需同步 `OrderStatusEnum`、`forceRefund` 与存量数据（AGENTS.md 坑位） | 回归订单 6 态流转 |
| 下单发延迟消息失败仅告警 | 事务消息/本地消息表 | 实现简单、不阻断下单 | 极端场景可能丢超时取消消息，无巡检兜底（待补） | 正常 MQ 环境下可复现取消 |
| docker-compose 仍为单体版编排 | 改造为 7 容器微服务编排 | 尚未同步 | `app` 服务仍 `build .` 引用已不存在的单模块 `src/`，`circleu.sql` 路径也已迁移；**按新架构重写 compose 属待办** | 待补：微服务版一键编排 |

---

## 9. 量化与验证（含待测，建议）

- 已验证（本地）：Eureka 注册中心可见各服务实例；网关路由四业务服务转发正常；注册登录 → 发布商品 → 加购 → 下单（Feign 扣库存）→ 支付 → 发货 → 确认收货 → 评价全链路接口实测通过；越权操作返回业务异常。
- **商品上下架同步：待测**。建议用例：商品库存为 1 时下单，断言状态立即变为已售(2) 且列表不再返回；随后取消该订单，断言库存回补至 1 且状态回到上架(1)。
- **事务回滚补偿：待测**。建议用例：构造购物车两件商品，让第二件在校验/扣减阶段失败，断言第一件已扣的 Redis 库存被 `afterCompletion` 补偿回补、无幽灵占用。
- **库存防超卖压测：待测**。建议对同一商品 50 并发下单，断言无超卖；同时验证 Redis 停掉后自动走 Redisson 锁降级仍不超卖。
- **跨服务故障演练：待测**。停掉 goods-service 再下单，观察 Feign 调用报错与订单事务回滚表现；确认无"半成品订单"残留。
- **延迟消息链路：待测（缩短验证）**。把 15 分钟临时改成 5s 观察自动取消与库存回补；先手动支付再等消息，订单不应被取消。
- **Sentinel：待测**。对 `createOrder` 以 >20 QPS 压测触发限流提示；错误比例 >50% 验证熔断窗口。
- **缓存：待测**。二次读同商品应命中 Redis；停 Redis 后读应降级 DB 且不 500。
- 无线上环境/用户量指标（本项目为课程/求职作品），所有压测数字须实测后回填，不得预估。

---

> 交接口径说明：本文件为学习路径。简历表述、追问口径与证据索引见同目录 `面经-CircleU.md`。
