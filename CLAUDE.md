# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Heimdall（海姆达尔）— 基于 ContiNew Admin 4.2.0 脚手架改造的统一认证中心。父 POM 为 `continew-starter:2.15.0`，提供了 SA-Token、MyBatis-Plus、Redis 等组件的自动配置。

## 构建与运行

```bash
# 编译（Spotless 代码格式化会在 compile 阶段自动执行）
mvn clean compile

# 打包（默认瘦包模式，输出到 heimdall-server/target/app/）
mvn clean package

# 胖包模式打包
mvn clean package -Pfat-jar

# 仅格式化代码（不编译）
mvn spotless:apply

# 启动（需先启动 MySQL 和 Redis，首次启动 Liquibase 会自动建表）
# 启动类：top.wain.heimdall.HeimdallApplication
# 默认端口：8000，Profile：dev
```

本地开发依赖：MySQL 8（端口 3306，数据库名 heimdall）、Redis（端口 6379）。本地已通过 Docker 独立运行基础中间件。

## 模块结构

```
heimdall (parent pom)
├── heimdall-common          # 公共模块：基类、工具、全局配置、跨模块 API 接口
├── heimdall-system          # 核心业务：认证(auth) + 系统管理(system)
├── heimdall-oauth2          # OAuth2 应用管理模块（多应用注册、密钥、scope）
├── heimdall-plugin          # 插件集合
│   ├── heimdall-plugin-open       # 能力开放（对外 API 签名验证）
│   ├── heimdall-plugin-tenant     # 多租户（行级隔离）
│   ├── heimdall-plugin-schedule   # 任务调度（SnailJob 客户端）
│   └── heimdall-plugin-generator  # 代码生成器
├── heimdall-server          # 启动 & 部署模块（聚合所有依赖，含 Liquibase 迁移脚本）
└── heimdall-extension
    └── heimdall-extension-schedule-server  # SnailJob 服务端（独立应用）
```

依赖方向：server → system + plugins → common。common 中定义跨模块 API 接口（`common.api.*`），由 system/plugin 提供实现。

## 架构要点

### 认证体系（heimdall-system/auth）

采用策略模式：`LoginHandler<T extends LoginReq>` 接口 + `LoginHandlerFactory` 工厂。已有实现：
- AccountLoginHandler（账号密码）
- PhoneLoginHandler（手机号）
- EmailLoginHandler（邮箱）
- SocialLoginHandler（社交登录，基于 JustAuth）

SA-Token 使用 jwt-simple 模式（`sa-token.extension.enableJwt: true`），持久层为 Redis。

### 数据层

- ORM：MyBatis-Plus，Mapper 接口扫描 `top.wain.heimdall.**.mapper`
- 主键策略：CosId 雪花算法（ASSIGN_ID）
- 逻辑删除：`deleted` 字段，未删除=0，已删除=id（解决唯一索引冲突）
- 数据权限：通过 `DataPermissionMapper` 基类实现
- 数据库版本管理：Liquibase，脚本在 `heimdall-server/src/main/resources/db/changelog/`

### 缓存

两级缓存架构（JetCache）：本地 Caffeine + 远程 Redisson，支持广播失效。

### 多租户

行级隔离模式，通过请求头 `X-Tenant-Id` / `X-Tenant-Code` 传递租户信息，MyBatis-Plus 拦截器自动拼接条件。

## 包结构约定

```
top.wain.heimdall.{模块名}
├── config/          # Spring 配置类
├── constant/        # 常量
├── controller/      # REST 接口
├── enums/           # 枚举
├── mapper/          # MyBatis Mapper 接口
├── model/
│   ├── entity/      # 数据库实体（DO）
│   ├── req/         # 请求参数
│   ├── resp/        # 响应参数
│   └── query/       # 查询条件
├── service/         # Service 接口
│   └── impl/        # Service 实现
└── handler/         # 处理器（策略实现等）
```

## 代码风格

- 格式化：Spotless + P3C 阿里巴巴规范（`.style/p3c-codestyle.xml`），compile 阶段自动应用
- License Header：`.style/license-header`，自动添加到每个 Java 文件头部
- Lombok：全局启用（`lombok.config` 在项目根目录）
- 工具库：Hutool（hutool-all）

## 配置文件

主配置在 `heimdall-server/src/main/resources/config/`：
- `application.yml` — 通用配置（SA-Token、MyBatis-Plus、SpringDoc 等）
- `application-dev.yml` — 开发环境（数据源、Redis、跨域等）
- `application-prod.yml` — 生产环境
- `application-generator.yml` — 代码生成器配置

环境变量覆盖：`DB_HOST`、`DB_PORT`、`DB_USER`、`DB_PWD`、`DB_NAME`、`REDIS_HOST`、`REDIS_PORT`、`REDIS_PWD`、`REDIS_DB`、`PROFILES_ACTIVE`。

## 接口文档

启动后访问 `http://localhost:8000/doc.html`（NextDoc4j 增强的 Swagger UI）。

## 前端项目

位置：`~/WorkSpace/personal/heimdall/Front-end/heimdall-ui/`（与后端同仓不同目录）

技术栈：Vue 3 + Arco Design + Vite + TypeScript + pnpm

```bash
# 安装依赖
pnpm bootstrap

# 开发启动（默认 http://localhost:5173）
pnpm dev

# 类型检查
pnpm typecheck

# lint
pnpm lint:fix
```

目录结构约定：
- `src/apis/{模块名}/` — API 请求封装
- `src/views/{模块名}/` — 页面组件
- `src/router/` — 路由配置（菜单由后端动态返回）

现有页面模块：system（用户/角色/部门/菜单/字典/配置）、tenant、open、schedule、monitor 等。
