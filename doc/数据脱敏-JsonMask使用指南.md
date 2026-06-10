# 数据脱敏 @JsonMask 使用指南

## 一、功能简介

基于 Jackson 自定义序列化器实现的字段脱敏组件。通过在 VO/DTO 字段上添加 `@JsonMask` 注解，接口返回 JSON 时自动将敏感字段脱敏（如 `138****1234`），无需在业务代码中手动处理。

**核心特性：**

- 声明式注解，零侵入业务逻辑
- 基于 Jackson `ContextualSerializer`，仅在序列化阶段生效，不修改对象内存值
- 内置 11 种脱敏类型（手机号、邮箱、身份证、银行卡等），支持自定义策略
- 零外部依赖，不需要 AOP 或 Redis
- 支持注入 Spring Bean 作为自定义脱敏策略

---

## 二、模块依赖

脱敏功能集成在 `payment-common-core` 模块中，**无需额外引入依赖**。只要你的模块依赖了 `payment-common-core`（业务模块默认已传递依赖），即可直接使用。

前置条件（项目已满足）：
- Jackson（`spring-boot-starter-json`）
- Hutool（`hutool-all`）

---

## 三、涉及文件清单

```
payment-common/payment-common-core/
└── src/main/java/com/ahead/payment/common/core/
    ├── annotation/
    │   └── JsonMask.java              # 脱敏注解
    ├── enums/
    │   └── MaskType.java              # 内置脱敏类型枚举（同时实现 IMaskStrategy）
    └── jackson/
        ├── IMaskStrategy.java         # 脱敏策略接口（用于自定义扩展）
        └── JsonMaskSerializer.java    # Jackson 序列化器（框架调用，业务无需关心）
```

---

## 四、注解参数说明

### @JsonMask

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `MaskType` | `CUSTOM` | 内置脱敏类型 |
| `strategy` | `Class<? extends IMaskStrategy>` | `IMaskStrategy.class` | 自定义脱敏策略 Bean 的类型，优先级高于 `value` |
| `left` | `int` | `0` | 左侧保留位数，仅在 `MaskType.CUSTOM` 时生效 |
| `right` | `int` | `0` | 右侧保留位数，仅在 `MaskType.CUSTOM` 时生效 |
| `character` | `char` | `'*'` | 脱敏替换符号 |

### MaskType 枚举

| 枚举值 | 说明 | 示例 |
|--------|------|------|
| `CUSTOM` | 自定义，按 `left`/`right` 保留首尾位数 | `left=3, right=4` → `138****1234` |
| `MOBILE_PHONE` | 手机号，保留前 3 后 4 | `138****1234` |
| `FIXED_PHONE` | 固定电话，保留前 4 后 2 | `0755****89` |
| `EMAIL` | 邮箱，前缀保留首字母 | `w***@gmail.com` |
| `ID_CARD` | 身份证，保留前 1 后 2 | `6**************34` |
| `BANK_CARD` | 银行卡，保留前 4 后 1-4 位 | `6222 **** **** **** 6789` |
| `CAR_LICENSE` | 车牌号，遮挡中间段 | `苏D4***0` |
| `CHINESE_NAME` | 中文名，保留首字 | `李**` |
| `PASSWORD` | 密码，全部替换 | `******` |
| `ADDRESS` | 地址，遮挡后 8 位 | `北京市海淀区****` |
| `IPV4` | IPv4 地址，保留第一段 | `192.*.*.*` |
| `IPV6` | IPv6 地址，保留第一段 | `2001:*:*:*:*:*:*:*` |

---

## 五、使用示例

### 1. 基础用法 — 内置脱敏类型

```java
import com.ahead.payment.common.core.annotation.JsonMask;
import com.ahead.payment.common.core.enums.MaskType;

@Data
@Schema(description = "用户信息 VO")
public class UserVO {

    @JsonMask(MaskType.MOBILE_PHONE)
    @Schema(description = "手机号")
    private String phone;

    @JsonMask(MaskType.ID_CARD)
    @Schema(description = "身份证号")
    private String idCard;

    @JsonMask(MaskType.EMAIL)
    @Schema(description = "邮箱")
    private String email;
}
```

接口返回结果：
```json
{
  "phone": "138****1234",
  "idCard": "6**************34",
  "email": "w***@gmail.com"
}
```

### 2. 自定义保留位数（CUSTOM）

保留前 3 位和后 4 位，适用于长度不固定的自定义字段：

```java
@JsonMask(left = 3, right = 4)
private String customField;
```

### 3. 自定义脱敏符号

```java
@JsonMask(value = MaskType.MOBILE_PHONE, character = '#')
private String phone;
// 返回：138####1234
```

### 4. 自定义脱敏策略（Spring Bean）

实现 `IMaskStrategy` 接口并注册为 Spring Bean：

```java
@Component
public class StudentIdMaskStrategy implements IMaskStrategy {
    @Override
    public String mask(String str, char character, int left, int right) {
        // 学号：保留前 4 位，其余替换
        if (str.length() <= 4) return str;
        return str.substring(0, 4) + String.valueOf(character).repeat(str.length() - 4);
    }
}
```

在字段上引用：

```java
@JsonMask(strategy = StudentIdMaskStrategy.class)
private String studentId;
```

---

## 六、与 @TranslateAnnotation 的关系

项目中原有 `@TranslateAnnotation(desensitize = DesensitizeEnums.PHONE)` 也可以实现脱敏，两者**可并存，互不影响**。

| 维度 | `@TranslateAnnotation` | `@JsonMask` |
|------|----------------------|-------------|
| 生效机制 | AOP 切面 + 反射修改字段值 | Jackson 序列化 |
| 触发条件 | Controller 方法需加 `@ResultTranslatebData` | 全局生效（返回 JSON 即脱敏） |
| 类型覆盖 | PHONE / IDCARD / EMAIL / ADDRESS | 11 种 |
| 推荐场景 | 已有存量代码 | 新增字段优先使用 |

**迁移建议：** 新增字段直接使用 `@JsonMask`；存量字段后续逐步将 `@TranslateAnnotation` 替换为 `@JsonMask`。

---

## 七、推荐接入字段

以下字段建议优先添加 `@JsonMask`：

### P0 — 用户信息类

| 类 | 字段 | 建议类型 |
|----|------|---------|
| `UserVO` | `phone` | `MOBILE_PHONE` |
| `UserVO` | `idCard` | `ID_CARD` |
| `SysStudentVo` | `phone` | `MOBILE_PHONE` |
| `SysStudentVo` | `idCard` | `ID_CARD` |

### P1 — 订单信息类

| 类 | 字段 | 建议类型 |
|----|------|---------|
| `PayOrderVo` | `phone` | `MOBILE_PHONE` |
| `PayOrderVo` | `idCard` | `ID_CARD` |
| `PayOrderArrearsVo` | `phone` | `MOBILE_PHONE` |
| `PayOrderArrearsVo` | `idCard` | `ID_CARD` |
| `StatisticalFormVo` | `phone` | `MOBILE_PHONE` |
| `StatisticalFormVo` | `idCard` | `ID_CARD` |

---

## 八、注意事项

1. **仅对 String 类型字段生效**：序列化器内部判断了字段类型，非 String 字段标注 `@JsonMask` 会被忽略，不会报错。

2. **不修改内存对象值**：`@JsonMask` 只在 JSON 序列化时脱敏，读到 Java 对象后字段值仍然是原始值。如果需要持久化脱敏，请使用 `@FieldEncrypt` 或手动处理。

3. **空值处理**：字段为 `null` 或空白字符串时，序列化输出为 `""`，不会报错。

4. **CUSTOM 模式边界**：`left + right > 字符串长度` 时，Hutool `replaceByCodePoint` 会按实际长度截断，不会抛出异常。

5. **自定义 strategy 必须是 Spring Bean**：使用 `strategy` 参数时，对应的 Class 必须已在 Spring 容器中注册（`@Component`），否则运行时会抛出 `NoSuchBeanDefinitionException`。

6. **Jackson ObjectMapper 需开启模块扫描**：项目默认配置已支持 `ContextualSerializer`，无需额外配置。
