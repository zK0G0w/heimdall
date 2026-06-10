# 接口幂等 @Idempotent 使用指南

## 一、功能简介

基于 Redis `setIfAbsent`（分布式锁思想）实现的接口幂等组件。通过在 Controller 方法上添加 `@Idempotent` 注解即可防止重复提交，支持 SpEL 表达式动态构建幂等 Key，方法执行失败自动释放锁允许重试。

**核心特性：**

- 声明式注解，零侵入业务代码
- 基于 Redis 的分布式锁，天然支持集群部署
- 方法执行成功 → 锁保留至超时自动过期，窗口期内拦截重复请求
- 方法执行异常 → 自动删除锁，允许客户端重试
- 触发幂等拦截返回 HTTP 409（Conflict）

**典型场景：**

- 用户连续点击"提交订单"按钮
- 网络抖动导致支付请求重发
- 前端误触发多次退款操作

---

## 二、模块依赖

幂等功能集成在 `payment-common-redis` 模块中，**无需额外引入依赖**。只要你的模块依赖了 `payment-common-redis`（业务模块默认已传递依赖），即可直接使用。

前置条件（项目已满足）：
- Redis + Redisson（`redisson-spring-boot-starter`）
- Spring AOP（`spring-boot-starter-aop`）
- Redis 连接配置正常

---

## 三、涉及文件清单

```
payment-common/payment-common-core/
└── src/main/java/com/ahead/payment/common/core/exception/
    └── IdempotentException.java              # 幂等异常

payment-common/payment-common-redis/
└── src/main/java/com/ahead/payment/common/redis/
    ├── annotation/
    │   └── Idempotent.java                   # 幂等注解
    ├── aspect/
    │   └── IdempotentAspect.java             # 幂等 AOP 切面
    ├── config/
    │   └── IdempotentConfiguration.java      # 自动配置
    └── util/
        └── SpelUtils.java                    # SpEL 表达式解析工具（与限流共用）

payment-common/payment-common-spring/
└── src/main/java/com/ahead/payment/common/spring/advice/
    └── GlobalExceptionAdvice.java            # 全局异常处理（已添加 IdempotentException 处理器）
```

---

## 四、注解参数说明

### @Idempotent

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `name` | `String` | `""` | 缓存 Key 名称，为空时自动生成（格式：`类短名.方法名`） |
| `key` | `String` | `""` | 幂等 Key，支持 SpEL 表达式（如 `#orderId`、`#dto.orderNo`） |
| `timeout` | `int` | `1000` | 幂等窗口时长 |
| `unit` | `TimeUnit` | `MILLISECONDS` | 时间单位 |
| `message` | `String` | `"请勿重复操作"` | 重复操作时返回的提示信息 |

### Redis Key 生成规则

```
Idempotent:{name}:{key}
```

- `Idempotent` — 固定前缀
- `name` — 注解指定或自动生成（`类短名.方法名`）
- `key` — SpEL 表达式解析结果（可选）

示例：`Idempotent:PayOrderController.pay:10001`

---

## 五、核心流程

```
请求进入
  │
  ▼
AOP 拦截 @Idempotent 方法
  │
  ▼
组装 Redis Key = Idempotent:{name}:{spelValue}
  │
  ▼
Redis setIfAbsent(key, "1", timeout)
  │
  ├── 设置成功（首次请求）──▶ 执行目标方法
  │                             │
  │                     ┌───────┴───────┐
  │                     │               │
  │                 正常返回          抛出异常
  │                     │               │
  │              锁保留至超时       删除锁（允许重试）
  │              自动过期
  │
  └── 设置失败（重复请求）──▶ 抛出 IdempotentException
                                │
                                ▼
                      HTTP 409 + "请勿重复操作"
```

**关键行为：**
- 方法执行**成功** → 锁保留，窗口期内相同请求被拦截
- 方法执行**异常** → 锁删除，客户端可以立即重试
- 锁到期**自动过期** → 无需手动清理

---

## 六、使用示例

### 1. 基础用法 — 防重复提交

3 秒内禁止重复提交，所有调用者共享锁（适用于管理后台操作）：

```java
@Idempotent(timeout = 3, unit = TimeUnit.SECONDS, message = "正在处理中，请勿重复提交")
@PostMapping("/submit")
public R<?> submit(@RequestBody OrderReq req) {
    // 业务逻辑
}
```

### 2. 使用 SpEL 表达式 — 按业务维度防重

按订单号防重（不同订单互不影响）：

```java
@Idempotent(
    key = "#dto.orderNo",
    timeout = 5,
    unit = TimeUnit.SECONDS,
    message = "订单正在处理中，请勿重复提交"
)
@PostMapping("/payOrder")
public R<?> addOrder(@RequestBody @Validated PayOrderAddDto dto) {
    // 创建订单
}
```

### 3. 按用户 + 操作维度防重

按订单 ID 防止同一笔订单重复支付：

```java
@Idempotent(
    key = "#orderId",
    timeout = 10,
    unit = TimeUnit.SECONDS,
    message = "订单正在支付中，请勿重复操作"
)
@GetMapping("/pay")
public R<?> pay(Long orderId, PayReqTypeEnum payType, HttpServletRequest request) {
    // 支付逻辑
}
```

### 4. 自定义 name — 手动指定缓存名称

```java
@Idempotent(
    name = "refund",
    key = "#dto.orderId",
    timeout = 10,
    unit = TimeUnit.SECONDS,
    message = "退款处理中，请勿重复操作"
)
@GetMapping("/refund")
public R<?> refund(@ParameterObject @Validated PayOrderRefundDto dto) {
    // 退款逻辑
}
```

### 5. SpEL 复杂表达式

```java
// 拼接多个参数
@Idempotent(key = "#userId + ':' + #orderId", timeout = 5, unit = TimeUnit.SECONDS)

// 访问嵌套属性
@Idempotent(key = "#req.user.id + ':' + #req.orderNo", timeout = 5, unit = TimeUnit.SECONDS)
```

---

## 七、幂等响应

触发幂等拦截时，接口返回 HTTP 状态码 **409 Conflict**，响应体为标准 `R` 格式：

```json
{
  "code": 1,
  "msg": "请勿重复操作",
  "data": null
}
```

`msg` 内容为注解中 `message` 参数指定的文本。

---

## 八、推荐接入接口

以下为建议优先添加幂等保护的接口：

### P0 — 支付核心

| 接口 | 方法 | 建议配置 |
|------|------|---------|
| 创建订单 `POST /payOrder` | `addOrder` | `key = "#dto.clientOrderId"`, timeout = 5s |
| 发起支付 `GET /payOrder/pay` | `pay` | `key = "#orderId"`, timeout = 10s |
| 退款 `GET /payOrder/refund` | `refund` | `key = "#dto.orderId"`, timeout = 10s |

### P1 — 建议添加

| 接口 | 方法 | 建议配置 |
|------|------|---------|
| 打包支付 `GET /payOrder/packagePay` | `packagePay` | timeout = 10s |
| 修改金额 `POST /payOrder/changeAmount` | `changeAmount` | `key = "#amountChangeHistory.orderId"`, timeout = 3s |
| 导入订单 `POST /payOrder/importOrder` | `importOrder` | timeout = 10s |

---

## 九、与 @RateLimiter 的区别

| 对比项 | @Idempotent（幂等） | @RateLimiter（限流） |
|--------|---------------------|----------------------|
| 目的 | 防止同一操作重复执行 | 控制操作频率上限 |
| 锁粒度 | 精确到一次操作（通常绑定业务 Key） | 时间窗口内的总次数 |
| 失败后重试 | 方法异常自动释放锁，可立即重试 | 不区分成功/失败，令牌消耗不可退还 |
| HTTP 状态码 | 409 Conflict | 429 Too Many Requests |
| 典型场景 | 创建订单、支付、退款 | 登录、验证码、公开 API |

**两者可以组合使用**，例如支付接口同时添加限流（防刷）和幂等（防重）：

```java
@RateLimiter(rate = 10, interval = 1, unit = TimeUnit.MINUTES, type = LimitType.IP)
@Idempotent(key = "#orderId", timeout = 10, unit = TimeUnit.SECONDS, message = "订单正在支付中")
@GetMapping("/pay")
public R<?> pay(Long orderId, PayReqTypeEnum payType, HttpServletRequest request) {
    // 支付逻辑
}
```

---

## 十、注意事项

1. **不指定 `key` 时所有调用者共享锁**：如果不传 `key`，幂等 Key 仅由 `name`（类名.方法名）组成，所有用户调用同一方法共享同一把锁。对于需要区分用户或业务实体的场景，**务必指定 `key`**。

2. **注解仅作用于被 Spring 代理的方法**：切面基于 Spring AOP 代理，类内部方法调用（`this.method()`）不会触发幂等拦截。

3. **SpEL 表达式中的参数名**：确保编译时保留了参数名（Java 8+ 的 `-parameters` 编译选项）。当前项目的 Maven 配置已包含此选项。

4. **timeout 设置建议**：
   - 太短（< 1s）：可能无法有效防重
   - 太长（> 30s）：用户等待时间过长，体验差
   - 建议根据业务操作耗时设置，一般 3~10 秒

5. **与前端防重的关系**：前端按钮 loading 状态只能防止正常操作，无法防止网络重发、接口重放等场景。`@Idempotent` 是服务端兜底保障，建议前后端配合使用。

6. **Redis 不可用时的行为**：如果 Redis 连接异常，`setCacheObjectIfAbsent` 会抛出异常，切面不会吞掉异常，请求不会被放行。
