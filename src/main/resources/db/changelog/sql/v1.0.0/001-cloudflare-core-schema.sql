CREATE TABLE IF NOT EXISTS `cf_account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `display_name` VARCHAR(128) NOT NULL COMMENT '账号名称',
    `auth_type` VARCHAR(32) NOT NULL COMMENT '鉴权类型',
    `api_token_enc` TEXT NULL COMMENT '加密后的访问令牌',
    `api_key_enc` TEXT NULL COMMENT '加密后的全局密钥',
    `api_email_enc` VARCHAR(512) NULL COMMENT '加密后的邮箱',
    `cloudflare_account_id` VARCHAR(128) NULL COMMENT '平台账号标识',
    `default_zone_id` VARCHAR(128) NULL COMMENT '默认站点标识',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    `remark` VARCHAR(512) NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_cf_account_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Cloudflare 账号表';

CREATE TABLE IF NOT EXISTS `cf_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `account_id` BIGINT NULL COMMENT '插件账号主键',
    `module` VARCHAR(64) NULL COMMENT '功能模块',
    `action` VARCHAR(64) NULL COMMENT '操作动作',
    `endpoint` VARCHAR(1024) NULL COMMENT '请求地址',
    `request_body` LONGTEXT NULL COMMENT '请求体',
    `response_code` INT NULL COMMENT '响应状态码',
    `success` TINYINT NOT NULL DEFAULT 0 COMMENT '是否成功',
    `message` VARCHAR(1000) NULL COMMENT '结果摘要',
    `created_at` DATETIME NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_cf_operation_log_account_id` (`account_id`),
    KEY `idx_cf_operation_log_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Cloudflare 操作日志';

CREATE TABLE IF NOT EXISTS `cf_pm_provider_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `provider_code` VARCHAR(64) NOT NULL COMMENT '供应商编码',
    `base_url` VARCHAR(512) NULL COMMENT '服务地址',
    `admin_username` VARCHAR(128) NULL COMMENT '管理员账号',
    `admin_password_enc` TEXT NULL COMMENT '管理员密码',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    `extra_json` LONGTEXT NULL COMMENT '扩展配置',
    `created_at` DATETIME NOT NULL COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cf_pm_provider_code` (`provider_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='插件市场供应商配置';

CREATE TABLE IF NOT EXISTS `cf_pm_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `operator_name` VARCHAR(128) NULL COMMENT '操作人',
    `action` VARCHAR(128) NOT NULL COMMENT '操作动作',
    `target` VARCHAR(256) NULL COMMENT '操作目标',
    `request_body` LONGTEXT NULL COMMENT '请求体',
    `success` TINYINT NOT NULL DEFAULT 0 COMMENT '是否成功',
    `message` VARCHAR(1000) NULL COMMENT '结果摘要',
    `created_at` DATETIME NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_cf_pm_operation_log_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='插件市场操作日志';
