# Heimdall OAuth2 授权服务器 — 第三方应用接入指南

## 概述

Heimdall 提供标准的 OAuth2 授权服务器能力，支持第三方应用通过 OAuth2 协议获取用户授权并访问受保护资源。

**支持的授权模式：**

| 模式 | 适用场景 |
|------|----------|
| 授权码模式（authorization_code） | 有用户参与的 Web/移动应用，最安全 |
| 客户端凭证模式（client_credentials） | 服务间调用，无用户参与 |
| 刷新令牌模式（refresh_token） | access_token 过期后无感续期 |

**基础信息：**

| 项目 | 值 |
|------|------|
| 授权端点 | `{server}/oauth2/authorize` |
| 令牌端点 | `{server}/oauth2/token` |
| 令牌撤销端点 | `{server}/oauth2/revoke` |
| 令牌自省端点 | `{server}/oauth2/introspect` |
| 用户信息端点 | `{server}/oauth2/userinfo` |

---

## 一、接入前准备

### 1.1 注册应用

在 Heimdall 管理后台「OAuth2 管理 → 应用管理」中创建应用，获取以下信息：

- **client_id** — 应用唯一标识（自动生成，32 位十六进制）
- **client_secret** — 应用密钥（创建时仅展示一次，请妥善保存）

### 1.2 配置回调地址

在应用详情中配置 `redirect_uri`（授权码模式必须）。

要求：
- 必须是完整的 URL（含协议、域名、路径）
- 授权请求中的 `redirect_uri` 必须与注册的完全一致（精确匹配，不支持通配符）
- 支持配置多个回调地址

### 1.3 配置授权范围（Scope）

在应用详情中关联需要的 Scope：

| Scope | 说明 | userinfo 返回字段 |
|-------|------|------------------|
| openid | 用户唯一标识 | sub（用户 ID） |
| profile | 基本信息 | nickname, avatar |
| email | 邮箱地址 | email |

### 1.4 配置授权类型

在应用的 `allowedGrantTypes` 中配置允许使用的授权模式（逗号分隔）：

| 应用类型 | 推荐配置 |
|----------|----------|
| Web 应用（有后端） | `authorization_code,refresh_token` |
| 移动端 App（无 secret） | `authorization_code,refresh_token`（搭配 PKCE） |
| 纯后端服务 | `client_credentials` |

---

## 二、授权码模式（Authorization Code）

适用于有用户参与的场景。流程分两步：引导用户授权 → 用授权码换取令牌。

### 2.1 发起授权请求

引导用户浏览器访问授权端点：

```
GET {server}/oauth2/authorize
    ?response_type=code
    &client_id={your_client_id}
    &redirect_uri={your_callback_url}
    &scope=openid profile email
    &state={random_string}
```

**参数说明：**

| 参数 | 必填 | 说明 |
|------|------|------|
| response_type | 是 | 固定值 `code` |
| client_id | 是 | 应用的 client_id |
| redirect_uri | 是 | 注册的回调地址 |
| scope | 否 | 请求的权限范围（空格分隔），不传则使用应用配置的全部 scope |
| state | 强烈建议 | 随机字符串，用于防止 CSRF 攻击，授权完成后原样回传 |
| code_challenge | 公开客户端必填 | PKCE challenge 值 |
| code_challenge_method | 公开客户端必填 | `S256`（推荐）或 `plain` |

**流程说明：**

1. Heimdall 校验请求参数合法性
2. 跳转到 Heimdall 授权页面
3. 用户登录（如果尚未登录）
4. 用户确认授权（首次授权需确认，后续相同 scope 静默通过）
5. Heimdall 302 重定向到你的 `redirect_uri`，附带授权码

### 2.2 接收授权回调

授权成功后，用户浏览器被重定向到：

```
{redirect_uri}?code={authorization_code}&state={your_state}
```

授权失败时：

```
{redirect_uri}?error=access_denied&error_description=用户拒绝授权&state={your_state}
```

**你的后端应该：**
1. 校验 `state` 参数与发起时一致（防 CSRF）
2. 用 `code` 换取 access_token（见下一步）

### 2.3 用授权码换取令牌

**你的后端**向 Heimdall 令牌端点发起 POST 请求：

```http
POST {server}/oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
&code={authorization_code}
&client_id={your_client_id}
&client_secret={your_client_secret}
&redirect_uri={your_callback_url}
```

如果使用了 PKCE，需额外带上：

```
&code_verifier={original_code_verifier}
```

**客户端认证方式（二选一）：**

方式一：参数传递（client_secret_post）
```
client_id=xxx&client_secret=yyy
```

方式二：HTTP Basic Auth（client_secret_basic）
```
Authorization: Basic {base64(client_id:client_secret)}
```

**成功响应：**

```json
{
  "code": "0",
  "msg": "ok",
  "data": {
    "access_token": "a51db9aa454e4a06947f4462ad516029",
    "refresh_token": "14d66f4789294487975ee7f3cf997efe",
    "token_type": "Bearer",
    "expires_in": 7200,
    "scope": "openid profile email"
  }
}
```

| 字段 | 说明 |
|------|------|
| access_token | 访问令牌，用于调用资源 API |
| refresh_token | 刷新令牌，用于续期 access_token |
| token_type | 令牌类型，固定 `Bearer` |
| expires_in | access_token 有效期（秒） |
| scope | 实际授权的 scope |

### 2.4 PKCE 说明（公开客户端必须）

无法安全存储 client_secret 的客户端（移动 App、SPA）必须使用 PKCE：

**发起授权前：**

```python
# 1. 生成随机 code_verifier（43-128 字符）
code_verifier = base64url(random_bytes(32))

# 2. 计算 code_challenge
code_challenge = base64url(sha256(code_verifier))
```

**授权请求中带上：**
```
code_challenge={code_challenge}&code_challenge_method=S256
```

**换码时带上原始值：**
```
code_verifier={code_verifier}
```

---

## 三、客户端凭证模式（Client Credentials）

适用于服务间调用，不涉及用户身份。一步到位获取 access_token。

```http
POST {server}/oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
&client_id={your_client_id}
&client_secret={your_client_secret}
&scope=openid profile
```

**成功响应：**

```json
{
  "code": "0",
  "msg": "ok",
  "data": {
    "access_token": "e33a2d1757634932b2d35e1b9c891d86",
    "refresh_token": null,
    "token_type": "Bearer",
    "expires_in": 7200,
    "scope": "openid profile"
  }
}
```

注意：此模式不颁发 refresh_token（到期重新用 secret 换即可），且 access_token 不关联用户身份。

---

## 四、刷新令牌模式（Refresh Token）

当 access_token 过期后，使用 refresh_token 获取新的令牌对，无需用户重新授权。

```http
POST {server}/oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=refresh_token
&refresh_token={your_refresh_token}
&client_id={your_client_id}
&client_secret={your_client_secret}
```

**成功响应：**

```json
{
  "code": "0",
  "msg": "ok",
  "data": {
    "access_token": "新的access_token",
    "refresh_token": "新的refresh_token",
    "token_type": "Bearer",
    "expires_in": 7200,
    "scope": "openid profile email"
  }
}
```

**重要**：每次刷新后，旧的 access_token 和 refresh_token 同时失效（令牌轮换机制），必须使用新返回的令牌。

---

## 五、使用 Access Token 调用 API

### 5.1 获取用户信息

```http
GET {server}/oauth2/userinfo
Authorization: Bearer {access_token}
```

**响应（按授权的 scope 返回对应字段）：**

```json
{
  "code": "0",
  "msg": "ok",
  "data": {
    "sub": "10001",
    "nickname": "张三",
    "avatar": "https://example.com/avatar.png",
    "email": "zhangsan@example.com"
  }
}
```

| 字段 | 对应 Scope | 说明 |
|------|-----------|------|
| sub | openid | 用户唯一标识 |
| nickname | profile | 用户昵称 |
| avatar | profile | 用户头像 URL |
| email | email | 邮箱地址 |

### 5.2 令牌自省（Token Introspection）

验证令牌是否有效，获取令牌元数据：

```http
POST {server}/oauth2/introspect
Content-Type: application/x-www-form-urlencoded

token={access_token}
&client_id={your_client_id}
&client_secret={your_client_secret}
```

**令牌有效时：**

```json
{
  "code": "0",
  "msg": "ok",
  "data": {
    "active": true,
    "client_id": "acb0588ffbb34b96b50d7f767d499779",
    "scope": "openid profile email",
    "token_type": "Bearer",
    "sub": "10001"
  }
}
```

**令牌无效时：**

```json
{
  "code": "0",
  "msg": "ok",
  "data": {
    "active": false
  }
}
```

### 5.3 撤销令牌

主动使令牌失效（用户登出场景）：

```http
POST {server}/oauth2/revoke
Content-Type: application/x-www-form-urlencoded

token={access_token_or_refresh_token}
&client_id={your_client_id}
&client_secret={your_client_secret}
```

成功响应 HTTP 200 空 body。撤销 access_token 时关联的 refresh_token 也会同时失效。

---

## 六、错误处理

### 6.1 令牌端点错误格式

```json
{
  "code": "0",
  "msg": "ok",
  "data": {
    "error": "invalid_grant",
    "error_description": "授权码无效或已过期"
  }
}
```

### 6.2 错误码列表

| 错误码 | HTTP 状态 | 说明 |
|--------|-----------|------|
| invalid_request | 400 | 缺少必要参数或参数格式错误 |
| invalid_client | 401 | client_id 不存在、secret 校验失败、应用已禁用 |
| invalid_grant | 400 | 授权码过期/已使用、refresh_token 无效、PKCE 校验失败 |
| unauthorized_client | 400 | 应用未被授权使用该 grant_type |
| unsupported_grant_type | 400 | 不支持的授权类型 |
| invalid_scope | 400 | 请求的 scope 超出应用配置范围 |
| access_denied | 403 | 用户拒绝授权 |

### 6.3 授权端点错误

授权端点错误分两类：

- **可回调错误**（scope 无效、用户拒绝）：重定向到 redirect_uri 并附带 error 参数
- **不可回调错误**（client_id 无效、redirect_uri 不匹配）：直接返回错误页，不会重定向到未验证的地址

---

## 七、安全建议

### 7.1 state 参数（防 CSRF）

每次授权请求必须携带随机生成的 `state` 参数，回调时校验一致性。推荐实现：

```java
// 发起授权前
String state = UUID.randomUUID().toString();
session.setAttribute("oauth2_state", state);

// 收到回调后
String callbackState = request.getParameter("state");
String savedState = session.getAttribute("oauth2_state");
if (!savedState.equals(callbackState)) {
    throw new SecurityException("CSRF 校验失败");
}
```

### 7.2 PKCE（防授权码截获）

- 移动 App 和 SPA 等公开客户端**必须**使用 PKCE
- 机密客户端（有 secret 的 Web 应用）建议也使用 PKCE
- code_challenge_method 优先使用 `S256`

### 7.3 令牌存储

- **access_token**：存储在后端 session 或内存中，不暴露给浏览器前端
- **refresh_token**：仅存储在后端，绝不传递给前端
- **client_secret**：仅存储在后端配置中，绝不硬编码在前端代码或 App 中

### 7.4 redirect_uri 安全

- 注册时使用具体路径（如 `https://app.com/oauth2/callback`），避免使用根路径
- 生产环境必须使用 HTTPS
- 不要注册 `localhost` 作为生产回调地址

### 7.5 令牌有效期

| 令牌类型 | 默认有效期 | 说明 |
|----------|-----------|------|
| 授权码 | 5 分钟 | 一次性使用，使用后立即失效 |
| access_token | 2 小时 | 可通过应用配置调整 |
| refresh_token | 7 天 | 可通过应用配置调整 |
| 授权记忆（consent） | 180 天 | 可通过应用配置调整，期间相同 scope 静默授权 |

---

## 八、接入示例

### 8.1 Java（Spring Boot）

```java
@RestController
public class OAuth2CallbackController {

    private static final String CLIENT_ID = "your_client_id";
    private static final String CLIENT_SECRET = "your_client_secret";
    private static final String REDIRECT_URI = "https://your-app.com/oauth2/callback";
    private static final String HEIMDALL_SERVER = "https://auth.example.com";

    /**
     * 生成授权链接，放在「使用 Heimdall 登录」按钮上
     */
    @GetMapping("/login/heimdall")
    public void login(HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();
        // 存储 state 用于回调校验
        // session.setAttribute("oauth2_state", state);

        String authUrl = HEIMDALL_SERVER + "/oauth2/authorize"
            + "?response_type=code"
            + "&client_id=" + CLIENT_ID
            + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
            + "&scope=" + URLEncoder.encode("openid profile email", StandardCharsets.UTF_8)
            + "&state=" + state;
        response.sendRedirect(authUrl);
    }

    /**
     * 授权回调接口
     */
    @GetMapping("/oauth2/callback")
    public String callback(@RequestParam String code, @RequestParam String state) {
        // 1. 校验 state
        // 2. 用 code 换 token
        Map<String, String> params = Map.of(
            "grant_type", "authorization_code",
            "code", code,
            "client_id", CLIENT_ID,
            "client_secret", CLIENT_SECRET,
            "redirect_uri", REDIRECT_URI
        );
        // POST to HEIMDALL_SERVER + "/oauth2/token"
        // 3. 拿到 access_token 后调用 /oauth2/userinfo 获取用户信息
        // 4. 建立本地会话
        return "登录成功";
    }
}
```

### 8.2 cURL 快速验证

```bash
# 1. 客户端凭证模式（最简单，验证接入是否成功）
curl -X POST https://auth.example.com/oauth2/token \
  -d "grant_type=client_credentials" \
  -d "client_id=your_client_id" \
  -d "client_secret=your_client_secret" \
  -d "scope=openid profile"

# 2. 用 access_token 查询用户信息
curl -X GET https://auth.example.com/oauth2/userinfo \
  -H "Authorization: Bearer {access_token}"

# 3. 令牌自省
curl -X POST https://auth.example.com/oauth2/introspect \
  -d "token={access_token}" \
  -d "client_id=your_client_id" \
  -d "client_secret=your_client_secret"

# 4. 撤销令牌
curl -X POST https://auth.example.com/oauth2/revoke \
  -d "token={access_token}" \
  -d "client_id=your_client_id" \
  -d "client_secret=your_client_secret"
```

---

## 九、常见问题

**Q: access_token 过期了怎么办？**
A: 使用 refresh_token 调用令牌端点获取新令牌。如果 refresh_token 也过期了，需要用户重新授权。

**Q: 授权码可以使用多次吗？**
A: 不可以。授权码是一次性的，使用后立即失效。如果换码失败，需要用户重新发起授权。

**Q: client_credentials 模式能获取用户信息吗？**
A: 不能。该模式不关联用户身份，access_token 中没有 user_id，调用 userinfo 接口会返回空。

**Q: 如何判断用户是否已授权过？**
A: 直接发起授权请求即可。如果用户之前授权过相同的 scope（且在 consent 有效期内），Heimdall 会自动跳过确认页直接回调。

**Q: 支持哪些 scope？**
A: 当前支持 `openid`、`profile`、`email`。具体可用 scope 取决于应用配置。
