# Open API 对外开放接口使用指南

> 本指南面向需要通过 API 签名方式对接国开分部缴费系统的第三方业务系统。

---

## 一、接入流程

1. **联系管理员创建应用**：提供应用名称和描述，管理员在后台【能力开放 → 应用管理】创建应用并分配密钥。
2. **获取密钥**：通过"查看密钥"接口获取 `accessKey`（访问密钥）和 `secretKey`（私有密钥）。
3. **按本文档生成签名**，携带规定参数发起请求。

---

## 二、签名机制概述

```
第三方系统                                  缴费系统后端
    │                                           │
    │── accessKey + timestamp + nonce +         │
    │    sign + 业务参数 ──────────────────────▶│
    │                                           ├─ 校验参数完整性
    │                                           ├─ 通过 accessKey 查询应用
    │                                           ├─ 校验应用状态（启用/有效期）
    │                                           ├─ 校验 timestamp 时效（防重放）
    │                                           ├─ 校验 nonce 唯一性（防重放）
    │                                           ├─ 重算签名并比对 sign
    │                                           │
    │◀──── 业务响应 ────────────────────────────│
```

---

## 三、必传参数

| 参数名 | 类型 | 必须 | 说明 |
|--------|------|------|------|
| `accessKey` | string | 是 | 应用访问密钥（管理员提供，长度 30 位） |
| `timestamp` | long | 是 | 当前时间戳（**毫秒**），与服务器时差不超过 15 分钟 |
| `nonce` | string | 是 | 随机字符串，每次请求唯一（建议 UUID 去横线，长度 ≥ 16 位） |
| `sign` | string | 是 | 签名值（MD5 生成，见第四章） |
| 业务参数 | - | 按接口 | 各接口定义的业务字段 |

> **注意**：`secretKey` 只参与本地签名计算，**不得传输到服务端**。

---

## 四、签名算法

签名使用 **HMAC-SHA256** 算法，`secretKey` 作为 HMAC 密钥，不参与消息拼接（不传输到服务端）。

### 步骤

1. 收集所有请求参数（**含** `accessKey`、`timestamp`、`nonce`、业务参数，**不含** `secretKey`，**不含** `sign`）
2. 按参数名**字典序（ASCII 升序）**排序
3. 拼接为 `key1=value1&key2=value2&...` 格式（值直接拼接，不需要 URL 编码）
4. 以 `secretKey` 为密钥，对拼接字符串做 **HMAC-SHA256**，结果转小写十六进制
5. 得到的哈希值即为 `sign`

### 伪代码

```
params = {
  accessKey: "Yjgxxxxx...",
  timestamp: 1745049600000,
  nonce:     "a3f8e2c1d7b4...",
  // + 所有业务参数（不含 secretKey、不含 sign）
}
sorted_keys = sort(params.keys())
message     = join([k + "=" + params[k] for k in sorted_keys], "&")
sign        = hmac_sha256(key=secretKey, message=message).toLowerCase()
```

### Java 示例

```java
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.*;

String accessKey  = "Yjgxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
String secretKey  = "MDI2Yxxx..."; // 私有密钥，只用于本地签名，不传服务端

Map<String, String> params = new TreeMap<>(); // TreeMap 自动字典序排序
params.put("accessKey", accessKey);
params.put("timestamp", String.valueOf(System.currentTimeMillis()));
params.put("nonce", UUID.randomUUID().toString().replace("-", ""));
// 添加业务参数
params.put("orderId", "20260419001");

// 拼接消息串
StringJoiner joiner = new StringJoiner("&");
params.forEach((k, v) -> joiner.add(k + "=" + v));
String message = joiner.toString();

// HMAC-SHA256 签名
String sign = new HMac(HmacAlgorithm.HmacSHA256, secretKey.getBytes(StandardCharsets.UTF_8))
        .digestHex(message);

params.put("sign", sign);
// 使用 params 发起 HTTP 请求（GET 拼 query string，POST 同理）
```

> 若不使用 Hutool，使用 `javax.crypto.Mac` 标准 JDK API 或其他语言对应的 HMAC-SHA256 实现均可，算法完全标准。

### curl 示例（GET 请求）

```bash
# 假设参数已计算好
curl "https://your-host/paymentApi/your-endpoint\
?accessKey=Yjgxxxxxxxxxx\
&timestamp=1745049600000\
&nonce=a3f8e2c1d7b4a5c6\
&orderId=20260419001\
&sign=3a7bd3e2360a3d29aa69..."
```

---

## 五、防重放机制

| 机制 | 说明 |
|------|------|
| **timestamp 时效** | 请求的 `timestamp`（毫秒）与服务器当前时间偏差不超过 **15 分钟**，超过则拒绝（防录制回放） |
| **nonce 唯一性** | 每个 `nonce` 在有效时间窗口内只允许使用一次（Redis 去重），重复请求拒绝 |

---

## 六、错误码速查

| 错误信息 | 原因 | 处理方式 |
|----------|------|----------|
| `timestamp 不能为空` | 未传 timestamp | 补传 timestamp |
| `nonce 不能为空` | 未传 nonce | 补传 nonce |
| `sign 不能为空` | 未传 sign | 补传 sign |
| `accessKey 不能为空` | 未传 accessKey | 补传 accessKey |
| `accessKey 无效` | accessKey 不存在 | 确认 accessKey 是否正确 |
| `应用已被禁用，请联系管理员` | 应用状态为禁用 | 联系管理员启用应用 |
| `应用已过期，请联系管理员` | 应用超过失效时间 | 联系管理员续期或新建应用 |
| `timestamp 差距太大` | 客户端时钟偏差 > 15 分钟 | 同步客户端系统时间 |
| `nonce 重复` | nonce 已被使用 | 每次请求生成新的随机 nonce |
| `签名不正确` | sign 验签失败 | 按第四章重新核对签名算法；注意 secretKey 是否正确 |

HTTP 响应状态码：
- `200` — 业务成功
- `401` — 签名验证失败（见 `msg` 字段）
- `400` — 业务参数错误

---

## 七、联调 / 排查清单

1. **时间戳单位**：确认是**毫秒**（13 位），不是秒（10 位）
2. **secretKey 不参与传输**：`secretKey` 只用于本地签名计算，最终请求中不包含 `secretKey` 字段
3. **sign 不参与签名计算**：生成 sign 时参数集合中不包含 `sign`，先算再追加
4. **参数值不做 URL 编码**：拼接原始值，不对值做 `URLEncoder.encode`
5. **字典序大小写**：按字节值排序（大写字母 < 小写字母），建议参数名统一使用小驼峰
6. **编码一致**：签名拼接字符串统一使用 UTF-8 编码
7. **调试方法**：在签名失败时，打印 `raw_string`（排序后拼接字符串）发给联调方交叉比对

---
