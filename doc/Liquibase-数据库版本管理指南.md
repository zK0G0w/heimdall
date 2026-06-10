# Liquibase 数据库版本管理指南

## 概述

项目使用 Liquibase 管理数据库 Schema 变更，每次表结构或初始数据的变动都必须通过 changeset 文件记录，**禁止直接在数据库中手动执行 DDL**。

---

## 目录结构

```
payment-module/payment-app/src/main/resources/db/
└── changelog/
    ├── db.changelog-master.xml        ← 主入口，只做 include，不写业务变更
    ├── 20260420-init.xml              ← 初始化变更集（历史遗留，勿改）
    ├── 20260420-init.sql              ← 初始化全量 SQL（历史遗留，勿改）
    └── YYYYMMDD-{描述}.sql            ← 后续所有变更均为格式化 SQL 文件
```

---

## 命名规范

| 内容 | 规范 | 示例 |
|------|------|------|
| 文件名 | `YYYYMMDD-{描述}.sql` | `20260512-add-payment-order.sql` |
| changeset id | `YYYYMMDD-{描述}` | `20260512-add-payment-order` |
| author | 开发者姓名 | `WainZeng` |

- 描述用英文小写 + 连字符，清晰表达本次变更目的
- 同一天多个变更追加序号：`20260512-01-add-order.sql`、`20260512-02-add-index.sql`

---

## 新增变更的标准流程

### 第一步：新建格式化 SQL 文件

在 `db/changelog/` 下新建 `YYYYMMDD-{描述}.sql`，**必须以 `--liquibase formatted sql` 开头**：

```sql
--liquibase formatted sql

--changeset WainZeng:20260512-add-payment-order
CREATE TABLE pay_order
(
    id          BIGINT      NOT NULL COMMENT '主键',
    order_no    VARCHAR(64) NOT NULL COMMENT '订单号',
    amount      DECIMAL(12, 2)       COMMENT '金额',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '支付订单表';

--rollback DROP TABLE IF EXISTS pay_order;
```

### 第二步：在 master 末尾追加 include

编辑 `db.changelog-master.xml`，在占位注释处追加：

```xml
<include file="db/changelog/20260512-add-payment-order.sql"/>
```

### 第三步：启动验证

启动应用，Liquibase 自动执行新 changeset，控制台输出类似：

```
Running Changeset: db/changelog/20260512-add-payment-order.sql::20260512-add-payment-order::WainZeng
ChangeSet ran successfully
```

---

## 格式化 SQL 速查

### 新增列

```sql
--liquibase formatted sql

--changeset WainZeng:20260520-add-remark-to-order
ALTER TABLE pay_order ADD COLUMN remark VARCHAR(255) COMMENT '备注';

--rollback ALTER TABLE pay_order DROP COLUMN remark;
```

### 新增索引

```sql
--liquibase formatted sql

--changeset WainZeng:20260520-add-idx-create-time
CREATE INDEX idx_create_time ON pay_order (create_time);

--rollback DROP INDEX idx_create_time ON pay_order;
```

### 插入初始数据

```sql
--liquibase formatted sql

--changeset WainZeng:20260520-insert-pay-status-dict
INSERT INTO sys_dict (id, dict_type, description, create_time)
VALUES (100, 'pay_status', '支付状态', NOW());

--rollback DELETE FROM sys_dict WHERE id = 100;
```

### 同一文件包含多个变更

```sql
--liquibase formatted sql

--changeset WainZeng:20260520-01-create-refund
CREATE TABLE pay_refund (...);
--rollback DROP TABLE IF EXISTS pay_refund;

--changeset WainZeng:20260520-02-add-refund-idx
CREATE INDEX idx_refund_order ON pay_refund (order_id);
--rollback DROP INDEX idx_refund_order ON pay_refund;
```

---

## 注意事项

### 已执行的 changeset 禁止修改

Liquibase 通过 `checksum` 校验文件内容，修改已执行的 changeset 会导致启动报错：

```
Validation failed: ... checksum ... was: ...
```

需要修正时，必须**新建一个 changeset**，不能改旧文件。

### --rollback 不是强制的，但推荐写

`--rollback` 定义回滚逻辑，在需要版本回退时使用。简单的 DDL 可以省略，但涉及数据写入的 changeset 建议写上。

### 现有数据库首次接入（基线同步）

对于已有表结构的数据库，首次引入 Liquibase 需执行一次基线同步，避免重跑初始化 SQL 导致数据丢失：

```bash
liquibase \
  --url="jdbc:mysql://{host}:{port}/{database}?useSSL=false&characterEncoding=utf8" \
  --username={username} \
  --password={password} \
  --changeLogFile=payment-module/payment-app/src/main/resources/db/changelog/db.changelog-master.xml \
  changelogSync
```

执行后 Liquibase 在数据库中创建 `DATABASECHANGELOG` 表并将所有现有 changeset 标记为已完成，后续启动只执行新 changeset。
