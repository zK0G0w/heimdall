-- liquibase formatted sql

-- changeset WainZeng:mfa-1
-- comment 新增 MFA 用户绑定表
CREATE TABLE IF NOT EXISTS `sys_user_mfa` (
    `id`           bigint       NOT NULL COMMENT 'ID',
    `user_id`      bigint       NOT NULL COMMENT '用户 ID',
    `type`         varchar(16)  NOT NULL DEFAULT 'totp' COMMENT 'MFA 类型',
    `secret`       varchar(128) NOT NULL COMMENT 'TOTP 密钥（加密）',
    `backup_codes` varchar(512) DEFAULT NULL COMMENT '恢复码 JSON（加密）',
    `enabled`      tinyint      NOT NULL DEFAULT 0 COMMENT '是否已激活（0：否；1：是）',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_mfa_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户 MFA 绑定表';

-- changeset WainZeng:mfa-2
-- comment 角色表新增 force_mfa 字段
ALTER TABLE `sys_role` ADD COLUMN `force_mfa` tinyint NOT NULL DEFAULT 0 COMMENT '是否强制 MFA（0：否；1：是）' AFTER `dept_check_strictly`;

-- changeset WainZeng:mfa-3
-- comment 新增全局 MFA 强制配置
INSERT INTO `sys_option` (`id`, `category`, `name`, `code`, `value`, `default_value`, `description`)
VALUES (30, 'MFA', '强制多因素认证', 'MFA_FORCE_ENABLED', NULL, 'false', '全局强制所有用户开启 MFA（true/false）');
