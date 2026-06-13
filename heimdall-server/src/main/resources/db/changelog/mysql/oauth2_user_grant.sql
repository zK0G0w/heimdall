--liquibase formatted sql

--changeset WainZeng:oauth2_user_grant_table
CREATE TABLE `oauth2_user_grant` (
    `id`         BIGINT       NOT NULL COMMENT '主键',
    `user_id`    BIGINT       NOT NULL COMMENT '用户 ID',
    `app_id`     BIGINT       NOT NULL COMMENT '应用 ID',
    `client_id`  VARCHAR(64)  NOT NULL COMMENT '客户端标识',
    `scope`      VARCHAR(500) NOT NULL COMMENT '授权的 scope（逗号分隔）',
    `granted_at` DATETIME     NOT NULL COMMENT '首次授权时间',
    `updated_at` DATETIME     NOT NULL COMMENT '最近授权更新时间',
    `deleted`    BIGINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0=未删除，删除时填入id）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_app` (`user_id`, `app_id`, `deleted`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户 OAuth2 授权记录';
