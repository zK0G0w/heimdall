# 字段加解密 @FieldEncrypt 使用指南

## 一、功能简介

基于 MyBatis-Plus 拦截器实现的透明字段加解密组件。通过在实体类（Entity）或 DTO 字段上添加 `@FieldEncrypt` 注解，数据库写入时自动加密、查询时自动解密，业务代码无需任何额外处理。

**核心特性：**

- 声明式注解，对业务代码完全透明
- 写入拦截器（`InnerInterceptor`）在 SQL 执行前加密字段值
- 查询拦截器（`ResultSetHandler Interceptor`）在结果集返回后解密字段值
- 支持 AES 算法，密钥可全局配置或按字段覆盖
- 支持加密过渡期：解密失败时回退返回原文，兼容历史明文数据

---

## 二、模块依赖

加解密功能集成在 `payment-common-mybatis` 模块中，**无需额外引入依赖**。业务模块依赖 `payment-common-mybatis` 即可直接使用。

前置条件（项目已满足）：
- MyBatis-Plus（`mybatis-plus-boot-starter`）
- Hutool（`hutool-all`）

---

## 三、涉及文件清单

```
payment-common/payment-common-mybatis/
└── src/main/java/com/ahead/payment/common/mybatis/
    ├── annotation/
    │   └── FieldEncrypt.java                    # 加解密注解
    ├── config/
    │   ├── FieldEncryptProperties.java           # 配置属性（前缀 payment.field-encrypt）
    │   └── MybatisAutoConfiguration.java         # 自动配置（注册拦截器）
    ├── encrypt/
    │   ├── AbstractFieldEncryptInterceptor.java  # 拦截器抽象基类（扫描注解字段）
    │   ├── MyBatisEncryptInterceptor.java        # 加密拦截器（写入前）
    │   ├── MyBatisDecryptInterceptor.java        # 解密拦截器（查询后）
    │   └── FieldEncryptHelper.java               # 加解密工具（委托 EncryptUtil）
    └── enums/
        └── FieldEncryptAlgorithm.java            # 加密算法枚举

payment-common/payment-common-core/
└── src/main/java/com/ahead/payment/common/core/
    ├── constant/
    │   └── CommonConstants.java                  # AES_KEY 兜底密钥（AES_KEY 字段）
    └── util/
        └── EncryptUtil.java                      # AES 加解密底层实现
```

---

## 四、注解参数说明

### @FieldEncrypt

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `FieldEncryptAlgorithm` | `DEFAULT` | 加密算法，目前支持 `DEFAULT`（跟随全局配置）和 `AES` |
| `password` | `String` | `""` | 字段级密钥，覆盖全局配置；为空时使用全局密钥 |

### FieldEncryptAlgorithm 枚举

| 枚举值 | 说明 |
|--------|------|
| `DEFAULT` | 使用 `payment.field-encrypt.algorithm` 全局配置（默认 AES） |
| `AES` | 强制使用 AES 算法 |

### 密钥优先级（从高到低）

```
注解 password 参数 → payment.field-encrypt.password 配置 → CommonConstants.AES_KEY 兜底
```

---

## 五、YAML 配置

在 `application.yml` 中添加以下配置（可选，不配置时使用默认值）：

```yaml
payment:
  field-encrypt:
    enabled: true          # 是否启用加解密，默认 true
    algorithm: AES         # 全局加密算法，默认 AES
    password: ""           # 全局加密密钥，为空时使用代码中的兜底密钥
```

> **安全建议**：生产环境应通过环境变量或配置中心注入 `password`，避免密钥硬编码在配置文件中。

---

## 六、使用示例

### 1. 基础用法 — 实体类字段加密

标注需要加密存储的敏感字段（推荐加在 Entity 和 DTO 上）：

```java
import com.ahead.payment.common.mybatis.annotation.FieldEncrypt;

@Data
@TableName("sys_student_info")
@Schema(description = "学生信息实体")
public class SysStudentInfoEntity extends BaseEntity {

    @FieldEncrypt
    @Schema(description = "手机号")
    private String phone;

    @FieldEncrypt
    @Schema(description = "身份证号")
    private String idCard;
}
```

- **写入时**：`phone`、`idCard` 字段值自动 AES 加密后存入数据库
- **查询时**：从数据库取出后自动解密，返回明文

### 2. DTO 字段加密

用于接收外部参数时加密，确保入参解析后写入数据库时字段值已加密：

```java
@Data
@Schema(description = "创建订单请求")
public class PayOrderDto {

    @FieldEncrypt
    @Schema(description = "手机号")
    private String phone;

    @FieldEncrypt
    @Schema(description = "身份证号")
    private String idCard;
}
```

### 3. 指定算法

```java
@FieldEncrypt(FieldEncryptAlgorithm.AES)
private String secretData;
```

### 4. 字段级密钥覆盖

某字段需要与全局密钥不同的独立密钥（如对接第三方系统的密钥隔离场景）：

```java
@FieldEncrypt(password = "my-field-specific-key-32bytes!")
private String thirdPartyToken;
```

### 5. Mapper 方法参数加密（用加密字段做查询条件）

**实体类字段上的 `@FieldEncrypt` 仅对写入（insert/update）有效**，不会自动加密 select 查询时的入参。  
当 Mapper 方法以加密字段作为查询条件时，必须在方法参数上额外标注 `@FieldEncrypt`，并配合 `@Param` 使用，拦截器才会在执行 SQL 前将入参值加密，与数据库密文匹配：

```java
// 正确：拦截器能识别参数注解，查询前自动加密 phone
SysUser findByPhone(@Param("phone") @FieldEncrypt String phone);

// 错误：缺少参数注解，查询时传入明文无法匹配库中密文，查询结果为空
SysUser findByPhone(@Param("phone") String phone);
```

> **原理**：`beforeQuery` 拦截器扫描的是 Mapper 方法参数上的 `@FieldEncrypt`（`AbstractFieldEncryptInterceptor#getEncryptParameters`），而 `beforeUpdate` 扫描的是实体类字段上的注解，两者触发路径不同。

| 操作 | 注解位置 | Mapper 入参是否需要加注解 |
|------|---------|------------------------|
| insert / update 实体 | 实体类字段 | **不需要** |
| select 用加密字段做查询条件 | Mapper 方法参数（配合 `@Param`） | **必须加** |

### 6. VO 字段解密（查询回显）

VO 类中如果需要从数据库查询的加密字段解密后回显，同样加 `@FieldEncrypt`：

```java
@Data
@Schema(description = "订单详情 VO")
public class PayOrderVo extends PayOrderEntity {

    @FieldEncrypt
    @Schema(description = "手机号")
    private String phone;

    @FieldEncrypt
    @Schema(description = "身份证号")
    private String idCard;
}
```

---

## 七、加密与脱敏组合使用

`@FieldEncrypt` 负责**存储安全**（数据库密文），`@JsonMask` 负责**传输安全**（接口明文脱敏）。两者可叠加使用：

```java
@FieldEncrypt                              // 存储加密
@JsonMask(MaskType.MOBILE_PHONE)           // 接口返回脱敏
@Schema(description = "手机号")
private String phone;
```

数据流转过程：

```
用户输入明文
    │
    ▼ @FieldEncrypt（写入拦截）
数据库存储密文
    │
    ▼ @FieldEncrypt（查询拦截）
Java 对象持有明文
    │
    ▼ @JsonMask（Jackson 序列化）
接口返回脱敏文本（如 138****1234）
```

---

## 八、推荐接入字段

以下字段建议添加 `@FieldEncrypt`（项目中已标注的字段供参考）：

### Entity — 加密存储

| 实体类 | 字段 | 说明 |
|--------|------|------|
| `SysUser` | `phone`、`idCard` | 系统用户手机号、身份证 |
| `SysStudentInfoEntity` | `phone`、`idCard` | 学生手机号、身份证 |

### DTO — 入参加密

| DTO 类 | 字段 | 说明 |
|--------|------|------|
| `PayOrderDto` | `phone`、`idCard` | 创建订单时的学生信息 |

### VO — 查询解密

| VO 类 | 字段 | 说明 |
|-------|------|------|
| `PayOrderVo` | `phone`、`idCard` | 订单详情回显 |
| `PayOrderArrearsVo` | `phone`、`idCard` | 欠费记录回显 |
| `StatisticalFormVo` | `phone`、`idCard` | 统计报表回显 |
| `UserVO` | `phone`、`idCard` | 用户信息回显 |

---

## 九、注意事项

1. **仅对 String 类型字段生效**：拦截器扫描注解时只处理 `String` 类型字段，其他类型标注 `@FieldEncrypt` 会被忽略。

2. **同一字段只能用一个密钥**：Entity 和 VO 对同一字段加密/解密时必须使用相同的密钥，否则解密失败。注解的 `password` 参数应保持一致。

3. **解密失败不报错**：`FieldEncryptHelper.decrypt` 在解密异常时会捕获异常并返回原始密文，用于兼容历史明文数据的过渡期。过渡期结束后建议监控异常日志确认是否存在异常数据。

4. **不要对已有存量明文数据直接开启加密**：已有记录为明文，开启后新写入的是密文；查询时解密拦截器会尝试解密明文，因解密失败降级回退为原文，功能上仍正常，但数据库中明文/密文混存。**建议在低峰期执行存量数据加密迁移脚本后再开启。**

5. **`enabled: false` 不会解密已加密数据**：关闭加解密开关后，数据库中的密文不会自动还原，查询会直接返回密文字符串。关闭前需确认数据库中无密文数据。

6. **MyBatis 原生 `@Select` 注解查询也支持解密**：解密拦截器作用于 `ResultSetHandler`，只要通过 MyBatis 的 Mapper 查询都会触发解密，无论是 XML 方式还是注解方式。

7. **用加密字段做查询条件时，Mapper 入参必须加 `@FieldEncrypt`**：实体类字段上的注解只影响写入拦截，查询拦截器(`beforeQuery`)只扫描 Mapper 方法参数上的注解。漏加会导致传入明文无法匹配数据库密文，返回空结果，且**不会报错**，排查困难。

8. **联合查询/`resultType` 为 `Map` 时不生效**：解密拦截器基于字段注解扫描，如果查询结果映射为 `Map<String, Object>` 而非实体类，字段注解无法被识别，需手动解密。
