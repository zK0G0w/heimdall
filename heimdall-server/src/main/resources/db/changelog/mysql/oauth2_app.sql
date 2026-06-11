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

--changeset WainZeng:oauth2_menu_data
-- 初始化 OAuth2 模块菜单
INSERT INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `redirect`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`)
VALUES
(4000, 'OAuth2 管理', 0, 1, '/oauth2', 'Oauth2', 'Layout', '/oauth2/app', 'safe', b'0', b'0', b'0', NULL, 5, 1, 1, NOW()),
(4010, '应用管理', 4000, 2, '/oauth2/app', 'Oauth2App', 'oauth2/app/index', NULL, 'apps', b'0', b'0', b'0', NULL, 1, 1, 1, NOW()),
(4011, '列表', 4010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'oauth2:app:list', 1, 1, 1, NOW()),
(4012, '详情', 4010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'oauth2:app:get', 2, 1, 1, NOW()),
(4013, '新增', 4010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'oauth2:app:create', 3, 1, 1, NOW()),
(4014, '修改', 4010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'oauth2:app:update', 4, 1, 1, NOW()),
(4015, '删除', 4010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'oauth2:app:delete', 5, 1, 1, NOW()),
(4020, 'Scope 管理', 4000, 2, '/oauth2/scope', 'Oauth2Scope', 'oauth2/scope/index', NULL, 'common', b'0', b'0', b'0', NULL, 2, 1, 1, NOW()),
(4021, '列表', 4020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'oauth2:scope:list', 1, 1, 1, NOW()),
(4022, '新增', 4020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'oauth2:scope:create', 2, 1, 1, NOW()),
(4023, '修改', 4020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'oauth2:scope:update', 3, 1, 1, NOW()),
(4024, '删除', 4020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'oauth2:scope:delete', 4, 1, 1, NOW());

--changeset WainZeng:oauth2_app_add_consent_ttl
ALTER TABLE `oauth2_app`
    ADD COLUMN `consent_ttl` INT DEFAULT NULL COMMENT '用户授权 Consent 有效期（秒），NULL 表示使用系统默认值' AFTER `allowed_grant_types`;
