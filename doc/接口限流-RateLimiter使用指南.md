# 接口限流 @RateLimiter 使用指南

## 一、功能简介

基于 Redisson `RRateLimiter` 令牌桶算法实现的分布式接口限流组件。通过在 Controller 方法上添加 `@RateLimiter` 注解即可启用限流，支持按 IP、全局、集群实例等维度限流，支持 SpEL 表达式动态构建限流 Key，支持单方法多规则组合。

**核心特性：**

- 声明式注解，零侵入业务代码
- 基于 Redis 的分布式令牌桶，天然支持集群部署
- 支持 `DEFAULT`（全局）、`IP`（按客户端 IP）、`CLUSTER`（按实例）三种限流维度
- 支持 `@Repeatable`，单注解和多注解两种写法
- 触发限流返回 HTTP 429（Too Many Requests）

---

## 二、模块依赖

限流功能集成在 `payment-common-redis` 模块中，**无需额外引入依赖**。只要你的模块依赖了 `payment-common-redis`（业务模块默认已传递依赖），即可直接使用。

前置条件（项目已满足）：
- Redisson（`redisson-spring-boot-starter`）
- Spring AOP（`spring-boot-starter-aop`）
- Redis 连接配置正常

---

## 三、涉及文件清单

```
payment-common/payment-common-core/
└── src/main/java/com/ahead/payment/common/core/exception/
    └── RateLimitException.java              # 限流异常

payment-common/payment-common-redis/
└── src/main/java/com/ahead/payment/common/redis/
    ├── annotation/
    │   ├── RateLimiter.java                 # 限流注解
    │   └── RateLimiters.java                # 限流组注解（容器注解）
    ├── aspect/
    │   └── RateLimiterAspect.java           # 限流 AOP 切面
    ├── config/
    │   └── RateLimiterConfiguration.java    # 自动配置
    ├── enums/
    │   └── LimitType.java                   # 限流类型枚举
    └── util/
        └── SpelUtils.java                   # SpEL 表达式解析工具

payment-common/payment-common-spring/
└── src/main/java/com/ahead/payment/common/spring/advice/
    └── GlobalExceptionAdvice.java           # 全局异常处理（已添加 RateLimitException 处理器）
```

---

## 四、注解参数说明

### @RateLimiter

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `type` | `LimitType` | `DEFAULT` | 限流维度：`DEFAULT` 全局 / `IP` 按客户端 IP / `CLUSTER` 按实例 |
| `name` | `String` | `""` | 缓存 Key 名称前缀，为空时自动生成（格式：`类短名.方法名`） |
| `key` | `String` | `""` | 限流 Key，支持 SpEL 表达式（如 `#userId`、`#req.orderNo`） |
| `rate` | `int` | `Integer.MAX_VALUE` | 令牌数（指定时间间隔内产生的令牌数量） |
| `interval` | `int` | `0` | 时间窗口大小 |
| `unit` | `TimeUnit` | `MILLISECONDS` | 时间窗口单位 |
| `message` | `String` | `"操作过于频繁，请稍后再试"` | 触发限流时返回的提示信息 |

### LimitType 枚举

| 值 | 说明 | Redis Key 后缀 |
|----|------|----------------|
| `DEFAULT` | 全局限流，所有请求共享令牌桶 | 无 |
| `IP` | 按客户端 IP 限流，每个 IP 独立令牌桶 | 客户端 IP 地址 |
| `CLUSTER` | 按实例限流，集群中每个实例独立令牌桶 | Redisson 实例 ID |

### Redis Key 生成规则

```
RateLimiter:{name}:{key}:{suffix}
```

- `RateLimiter` — 固定前缀
- `name` — 注解指定或自动生成（`类短名.方法名`）
- `key` — SpEL 表达式解析结果（可选）
- `suffix` — 限流维度后缀：IP 地址 / 实例 ID / 空（可选）

示例：`RateLimiter:PayOrderController.pay:10001:192.168.1.100`

---

## 五、使用示例

### 1. 基础用法 — 全局限流

每分钟最多 10 次请求，所有用户共享配额：

```java
@RateLimiter(rate = 10, interval = 1, unit = TimeUnit.MINUTES)
@PostMapping("/submit")
public R<?> submit(@RequestBody OrderReq req) {
    // 业务逻辑
}
```

### 2. 按 IP 限流

每个 IP 每分钟最多 5 次请求：

```java
@RateLimiter(
    rate = 5,
    interval = 1,
    unit = TimeUnit.MINUTES,
    type = LimitType.IP,
    message = "请求过于频繁，请1分钟后再试"
)
@PostMapping("/oauth2/token")
public R<?> login(@RequestBody LoginReq req) {
    // 登录逻辑
}
```

### 3. 使用 SpEL 表达式

按用户维度限流（每个用户每小时最多 20 次）：

```java
@RateLimiter(
    key = "#req.userId",
    rate = 20,
    interval = 1,
    unit = TimeUnit.HOURS,
    message = "操作过于频繁，请稍后再试"
)
@PostMapping("/pay")
public R<?> pay(@RequestBody PayReq req) {
    // 支付逻辑
}
```

SpEL 支持复杂表达式：

```java
// 拼接多个参数
@RateLimiter(key = "#email + ':' + #type", rate = 2, interval = 1, unit = TimeUnit.MINUTES)

// 调用 Spring 静态方法
@RateLimiter(key = "#phone + ':' + T(cn.hutool.extra.spring.SpringUtil).getProperty('sms.templateId')")
```

### 4. 多规则组合限流（@RateLimiters）

同一接口配置多条限流规则，所有规则必须同时通过：

```java
@RateLimiters({
    @RateLimiter(
        name = "pay_minute",
        key = "#req.userId",
        rate = 5,
        interval = 1,
        unit = TimeUnit.MINUTES,
        message = "支付操作过于频繁，请1分钟后再试"
    ),
    @RateLimiter(
        name = "pay_hour",
        key = "#req.userId",
        rate = 30,
        interval = 1,
        unit = TimeUnit.HOURS,
        message = "支付操作过于频繁，请1小时后再试"
    ),
    @RateLimiter(
        name = "pay_ip",
        rate = 100,
        interval = 1,
        unit = TimeUnit.MINUTES,
        type = LimitType.IP,
        message = "当前网络请求过于频繁"
    )
})
@PostMapping("/pay")
public R<?> pay(@RequestBody PayReq req) {
    // 支付逻辑
}
```

### 5. 利用 @Repeatable 简化写法

由于 `@RateLimiter` 支持 `@Repeatable`，也可以直接叠加多个注解（效果等同于 `@RateLimiters`）：

```java
@RateLimiter(name = "pay_minute", key = "#req.userId", rate = 5, interval = 1, unit = TimeUnit.MINUTES)
@RateLimiter(name = "pay_hour", key = "#req.userId", rate = 30, interval = 1, unit = TimeUnit.HOURS)
@PostMapping("/pay")
public R<?> pay(@RequestBody PayReq req) {
    // 支付逻辑
}
```

---

## 六、限流响应

触发限流时，接口返回 HTTP 状态码 **429 Too Many Requests**，响应体为标准 `R` 格式：

```json
{
  "code": 1,
  "msg": "操作过于频繁，请稍后再试",
  "data": null
}
```

`msg` 内容为注解中 `message` 参数指定的文本。

---

## 七、推荐接入接口

以下为建议优先添加限流的接口，按优先级排序：

### P0 — 安全必需

| 接口 | 方法 | 建议规则 |
|------|------|---------|
| 登录 `/oauth2/token` | POST | IP 维度，每分钟 10 次 |
| 创建订单 `/payOrder` | POST | IP 维度，每分钟 30 次 |
| 发起支付 `/payOrder/pay` | GET | IP 维度，每分钟 10 次 |
| 退款 `/payOrder/refund` | GET | IP 维度，每分钟 5 次 |

### P1 — 建议添加

| 接口 | 方法 | 建议规则 |
|------|------|---------|
| 打包支付 `/payOrder/packagePay` | GET | IP 维度，每分钟 10 次 |
| 订单导入 `/payOrder/importOrder` | POST | 全局维度，每分钟 5 次 |
| 获取 openId `/payOrder/getWxOpenId` | GET | IP 维度，每分钟 20 次 |
| 支付回调 `/payOrder/callback/**` | POST | 全局维度，每秒 100 次 |

---

## 八、注意事项

1. **注解仅作用于 Controller 方法**：切面基于 Spring AOP 代理，内部方法调用（`this.method()`）不会触发限流。

2. **多规则组合时 `name` 必须不同**：多个 `@RateLimiter` 规则如果 `name`、`key`、`type` 完全相同，会命中同一个令牌桶，导致规则冲突。建议为每条规则指定不同的 `name`。

3. **SpEL 表达式中的参数名**：确保编译时保留了参数名（Java 8+ 的 `-parameters` 编译选项，或使用 Spring 的 `@Param` 注解）。当前项目的 Maven 配置已包含此选项。

4. **限流器配置自动更新**：如果修改了注解参数（如调整 `rate`），重启后切面会自动检测配置变化并更新 Redis 中的限流器配置，无需手动清理 Redis Key。

5. **Redis 不可用时的行为**：如果 Redis 连接异常，切面会抛出 `RateLimitException("服务器限流异常，请稍候再试")`，不会放行请求，确保安全。

6. **非 Web 环境**：`LimitType.IP` 依赖 `HttpServletRequest`，如果在非 Web 上下文中使用会抛异常。定时任务、MQ 消费者等场景请使用 `LimitType.DEFAULT` 或 `LimitType.CLUSTER`。
