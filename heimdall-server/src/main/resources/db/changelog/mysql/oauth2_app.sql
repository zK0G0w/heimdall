--liquibase formatted sql

--changeset WainZeng:oauth2_app_table
CREATE TABLE `oauth2_app` (
    `id` BIGINT NOT NULL COMMENT 'ID',
    `app_name` VARCHAR(100) NOT NULL COMMENT '应用名称',
    `app_type` TINYINT NOT NULL DEFAULT 1 COMMENT '应用类型：1=Web应用 2=移动应用 3=服务端应用',
    `client_id` VARCHAR(64) NOT NULL COMMENT '客户端标识',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '应用描述',
    `logo` VARCHAR(500) DEFAULT NULL COMMENT 'Logo URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=启用 2=禁用',
    `access_token_ttl` INT NOT NULL DEFAULT 7200 COMMENT 'Access Token 有效期（秒）',
    `refresh_token_ttl` INT NOT NULL DEFAULT 604800 COMMENT 'Refresh Token 有效期（秒）',
    `allow_silent_auth` TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许静默授权：0=否 1=是',
    `allowed_grant_types` VARCHAR(200) NOT NULL DEFAULT 'authorization_code' COMMENT '允许的授权类型，逗号分隔',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_user` BIGINT DEFAULT NULL COMMENT '修改人',
    `update_time` DATETIME DEFAULT NULL COMMENT '修改时间',
    `deleted` BIGINT NOT NULL DEFAULT 0 COMMENT '是否已删除（0：否；id：是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_client_id` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='OAuth2 应用表';

--changeset WainZeng:oauth2_app_secret_table
CREATE TABLE `oauth2_app_secret` (
    `id` BIGINT NOT NULL COMMENT 'ID',
    `app_id` BIGINT NOT NULL COMMENT '应用 ID',
    `client_secret` VARCHAR(128) NOT NULL COMMENT '密钥值（加密存储）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=启用 2=禁用',
    `expires_at` DATETIME DEFAULT NULL COMMENT '过期时间',
    `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_app_id` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='OAuth2 应用密钥表';

--changeset WainZeng:oauth2_app_redirect_uri_table
CREATE TABLE `oauth2_app_redirect_uri` (
    `id` BIGINT NOT NULL COMMENT 'ID',
    `app_id` BIGINT NOT NULL COMMENT '应用 ID',
    `uri` VARCHAR(500) NOT NULL COMMENT '回调地址',
    PRIMARY KEY (`id`),
    KEY `idx_app_id` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='OAuth2 应用回调地址表';

--changeset WainZeng:oauth2_scope_table
CREATE TABLE `oauth2_scope` (
    `id` BIGINT NOT NULL COMMENT 'ID',
    `scope_code` VARCHAR(100) NOT NULL COMMENT 'Scope 标识',
    `scope_name` VARCHAR(100) NOT NULL COMMENT 'Scope 名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_user` BIGINT DEFAULT NULL COMMENT '修改人',
    `update_time` DATETIME DEFAULT NULL COMMENT '修改时间',
    `deleted` BIGINT NOT NULL DEFAULT 0 COMMENT '是否已删除（0：否；id：是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_scope_code` (`scope_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='OAuth2 Scope 定义表';

--changeset WainZeng:oauth2_app_scope_table
CREATE TABLE `oauth2_app_scope` (
    `app_id` BIGINT NOT NULL COMMENT '应用 ID',
    `scope_id` BIGINT NOT NULL COMMENT 'Scope ID',
    PRIMARY KEY (`app_id`, `scope_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='OAuth2 应用与 Scope 关联表';
