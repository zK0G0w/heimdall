# Heimdall 海姆达尔

基于 Spring Boot 3 + SA-Token 的统一认证中心，提供 OAuth2 授权服务、RBAC 权限管理、多租户等能力。改造自 [ContiNew Admin 4.2.0](https://github.com/continew-org/continew-admin) 脚手架。

## 技术栈

| 分类 | 技术 |
|------|------|
| 基础框架 | Spring Boot 3、ContiNew Starter 2.15.0 |
| 认证授权 | SA-Token（JWT 模式）、OAuth2 自研实现 |
| 数据层 | MyBatis-Plus、Liquibase（自动建表） |
| 缓存 | Redis + Caffeine（JetCache 两级缓存） |
| 数据库 | MySQL 8 |
| 构建工具 | Maven |

## 模块结构

```
heimdall (parent pom)
├── heimdall-common            # 公共模块：基类、工具、全局配置
├── heimdall-system            # 核心业务：认证 + 系统管理（用户/角色/部门/菜单/字典）
├── heimdall-oauth2            # OAuth2 授权服务（授权码、客户端凭证、PKCE、令牌管理）
├── heimdall-plugin
│   ├── heimdall-plugin-open         # 能力开放（API 签名验证）
│   ├── heimdall-plugin-tenant       # 多租户（行级隔离）
│   ├── heimdall-plugin-schedule     # 任务调度（SnailJob 客户端）
│   └── heimdall-plugin-generator    # 代码生成器
├── heimdall-server            # 启动模块（聚合依赖、Liquibase 迁移脚本）
└── heimdall-extension
    └── heimdall-extension-schedule-server  # SnailJob 调度服务端
```

## 核心能力

- **多种登录方式**：账号密码、手机号、邮箱、社交登录（JustAuth）
- **MFA 多因素认证**：TOTP（Google Authenticator 兼容）、恢复码、管理员可按角色/全局强制开启
- **OAuth2 授权服务**：授权码模式（+PKCE）、客户端凭证模式、刷新令牌、令牌撤销与自省、OIDC UserInfo
- **RBAC 权限管理**：用户、角色、部门、菜单、数据权限
- **多租户**：行级隔离，请求头传递租户标识
- **API 开放能力**：参数签名验证
- **任务调度**：基于 SnailJob 的分布式任务调度

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8
- Redis

### 本地开发

1. 创建 MySQL 数据库

```sql
CREATE DATABASE heimdall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

2. 启动 Redis（默认连接 `127.0.0.1:6379`，密码 `redis123456`，数据库索引 `6`）

3. 如果你的本地 MySQL/Redis 连接信息与默认值不同，可通过环境变量覆盖：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `DB_HOST` | 127.0.0.1 | MySQL 地址 |
| `DB_PORT` | 3306 | MySQL 端口 |
| `DB_USER` | root | MySQL 用户名 |
| `DB_PWD` | root123456 | MySQL 密码 |
| `DB_NAME` | heimdall | 数据库名 |
| `REDIS_HOST` | 127.0.0.1 | Redis 地址 |
| `REDIS_PORT` | 6379 | Redis 端口 |
| `REDIS_PWD` | redis123456 | Redis 密码 |
| `REDIS_DB` | 6 | Redis 数据库索引 |

4. 编译并启动

```bash
# 编译（自动执行 Spotless 代码格式化）
mvn clean compile

# 启动
# 启动类：top.wain.heimdall.HeimdallApplication
# 默认端口：8000，Profile：dev
# 首次启动 Liquibase 自动建表，无需手动导入 SQL
```

5. 访问接口文档：http://localhost:8000/doc.html

默认管理员账号：`admin` / `admin123`

## 环境变量

开发环境（dev）内置默认值，开箱即用。生产环境（prod）部署时需配置以下环境变量：

| 变量 | 说明 |
|------|------|
| `DB_HOST` | 数据库地址 |
| `DB_PORT` | 数据库端口 |
| `DB_USER` | 数据库用户名 |
| `DB_PWD` | 数据库密码 |
| `DB_NAME` | 数据库名 |
| `REDIS_HOST` | Redis 地址 |
| `REDIS_PORT` | Redis 端口 |
| `REDIS_PWD` | Redis 密码 |
| `REDIS_DB` | Redis 数据库索引 |
| `SA_TOKEN_JWT_SECRET` | SA-Token JWT 签名密钥 |
| `ENCRYPT_AES_PASSWORD` | 字段加密 AES 密钥 |
| `ENCRYPT_RSA_PUBLIC_KEY` | 传输加密 RSA 公钥 |
| `ENCRYPT_RSA_PRIVATE_KEY` | 传输加密 RSA 私钥 |
| `SCHEDULE_PASSWORD` | SnailJob 密码 |
| `SCHEDULE_TOKEN` | SnailJob 接入 token |

## 相关项目

- [ContiNew Admin](https://github.com/continew-org/continew-admin) — 上游脚手架
- [ContiNew Starter](https://github.com/continew-org/continew-starter) — 基础设施组件库
- [SA-Token](https://sa-token.cc/) — 认证框架

## 许可证

[Apache-2.0](LICENSE)
