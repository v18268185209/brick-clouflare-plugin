DROP TEMPORARY TABLE IF EXISTS tmp_cloudflare_api_defs;
CREATE TEMPORARY TABLE tmp_cloudflare_api_defs (
    router VARCHAR(255) PRIMARY KEY,
    api_desc VARCHAR(255) NOT NULL
);

INSERT INTO tmp_cloudflare_api_defs (router, api_desc) VALUES
('/cloudflare/overview*', '总览接口'),
('/plugins/eqadminPluginsCloudflare/cloudflare/overview*', '总览接口（插件路由）'),
('/cloudflare/accounts*', '账号上下文接口'),
('/plugins/eqadminPluginsCloudflare/cloudflare/accounts*', '账号上下文接口（插件路由）'),
('/cloudflare/dns*', '解析记录接口'),
('/plugins/eqadminPluginsCloudflare/cloudflare/dns*', '解析记录接口（插件路由）'),
('/cloudflare/r2*', '对象存储接口'),
('/plugins/eqadminPluginsCloudflare/cloudflare/r2*', '对象存储接口（插件路由）'),
('/cloudflare/firewall*', '防火墙接口'),
('/plugins/eqadminPluginsCloudflare/cloudflare/firewall*', '防火墙接口（插件路由）'),
('/cloudflare/rulesets*', '安全规则集接口'),
('/plugins/eqadminPluginsCloudflare/cloudflare/rulesets*', '安全规则集接口（插件路由）'),
('/cloudflare/cache*', '缓存策略接口'),
('/plugins/eqadminPluginsCloudflare/cloudflare/cache*', '缓存策略接口（插件路由）'),
('/cloudflare/ssl*', '证书与加密接口'),
('/plugins/eqadminPluginsCloudflare/cloudflare/ssl*', '证书与加密接口（插件路由）'),
('/cloudflare/zone-settings*', '站点高级设置接口'),
('/plugins/eqadminPluginsCloudflare/cloudflare/zone-settings*', '站点高级设置接口（插件路由）'),
('/cloudflare/workers*', '边缘脚本接口'),
('/plugins/eqadminPluginsCloudflare/cloudflare/workers*', '边缘脚本接口（插件路由）'),
('/cloudflare/kv*', '键值存储接口'),
('/plugins/eqadminPluginsCloudflare/cloudflare/kv*', '键值存储接口（插件路由）'),
('/cloudflare/analytics*', '分析报表接口'),
('/plugins/eqadminPluginsCloudflare/cloudflare/analytics*', '分析报表接口（插件路由）'),
('/cloudflare/page-rules*', '页面规则接口'),
('/plugins/eqadminPluginsCloudflare/cloudflare/page-rules*', '页面规则接口（插件路由）'),
('/cloudflare/logs*', '操作日志接口'),
('/plugins/eqadminPluginsCloudflare/cloudflare/logs*', '操作日志接口（插件路由）'),
('/cloudflare/plugin-market-admin*', '插件市场接口'),
('/plugins/eqadminPluginsCloudflare/cloudflare/plugin-market-admin*', '插件市场接口（插件路由）'),
('/cloudflare/worker-admin*', '授权中心接口'),
('/plugins/eqadminPluginsCloudflare/cloudflare/worker-admin*', '授权中心接口（插件路由）');

INSERT INTO qixiaozhu_perms
(
    `type`,
    `desc`,
    router,
    `status`,
    deleted,
    create_time,
    update_time,
    create_user_id,
    update_user_id,
    menu_code
)
SELECT
    'api',
    a.api_desc,
    a.router,
    'ok',
    0,
    NOW(),
    NOW(),
    1,
    1,
    'plugins_cloudflare_manager'
FROM tmp_cloudflare_api_defs a
LEFT JOIN qixiaozhu_perms p
  ON p.router = a.router
 AND p.deleted = 0
 AND p.`type` = 'api'
WHERE p.id IS NULL;

UPDATE qixiaozhu_perms p
JOIN tmp_cloudflare_api_defs a
  ON a.router = p.router
SET p.`type` = 'api',
    p.`desc` = a.api_desc,
    p.menu_code = COALESCE(NULLIF(TRIM(p.menu_code), ''), 'plugins_cloudflare_manager'),
    p.status = 'ok',
    p.deleted = 0,
    p.update_time = NOW(),
    p.update_user_id = 1
WHERE p.`type` = 'api';

DROP TEMPORARY TABLE IF EXISTS tmp_cloudflare_api_bind_defs;
CREATE TEMPORARY TABLE tmp_cloudflare_api_bind_defs (
    menu_code VARCHAR(128) NOT NULL,
    router VARCHAR(255) NOT NULL,
    PRIMARY KEY (menu_code, router)
);

INSERT INTO tmp_cloudflare_api_bind_defs (menu_code, router) VALUES
('plugins_cloudflare_overview', '/cloudflare/overview*'),
('plugins_cloudflare_overview', '/plugins/eqadminPluginsCloudflare/cloudflare/overview*'),
('plugins_cloudflare_accounts', '/cloudflare/accounts*'),
('plugins_cloudflare_accounts', '/plugins/eqadminPluginsCloudflare/cloudflare/accounts*'),
('plugins_cloudflare_dns', '/cloudflare/dns*'),
('plugins_cloudflare_dns', '/plugins/eqadminPluginsCloudflare/cloudflare/dns*'),
('plugins_cloudflare_r2', '/cloudflare/r2*'),
('plugins_cloudflare_r2', '/plugins/eqadminPluginsCloudflare/cloudflare/r2*'),
('plugins_cloudflare_firewall', '/cloudflare/firewall*'),
('plugins_cloudflare_firewall', '/plugins/eqadminPluginsCloudflare/cloudflare/firewall*'),
('plugins_cloudflare_rulesets', '/cloudflare/rulesets*'),
('plugins_cloudflare_rulesets', '/plugins/eqadminPluginsCloudflare/cloudflare/rulesets*'),
('plugins_cloudflare_cache', '/cloudflare/cache*'),
('plugins_cloudflare_cache', '/plugins/eqadminPluginsCloudflare/cloudflare/cache*'),
('plugins_cloudflare_ssl', '/cloudflare/ssl*'),
('plugins_cloudflare_ssl', '/plugins/eqadminPluginsCloudflare/cloudflare/ssl*'),
('plugins_cloudflare_zone_settings', '/cloudflare/zone-settings*'),
('plugins_cloudflare_zone_settings', '/plugins/eqadminPluginsCloudflare/cloudflare/zone-settings*'),
('plugins_cloudflare_workers', '/cloudflare/workers*'),
('plugins_cloudflare_workers', '/plugins/eqadminPluginsCloudflare/cloudflare/workers*'),
('plugins_cloudflare_kv', '/cloudflare/kv*'),
('plugins_cloudflare_kv', '/plugins/eqadminPluginsCloudflare/cloudflare/kv*'),
('plugins_cloudflare_analytics', '/cloudflare/analytics*'),
('plugins_cloudflare_analytics', '/plugins/eqadminPluginsCloudflare/cloudflare/analytics*'),
('plugins_cloudflare_page_rules', '/cloudflare/page-rules*'),
('plugins_cloudflare_page_rules', '/plugins/eqadminPluginsCloudflare/cloudflare/page-rules*'),
('plugins_cloudflare_logs', '/cloudflare/logs*'),
('plugins_cloudflare_logs', '/plugins/eqadminPluginsCloudflare/cloudflare/logs*'),
('plugins_cloudflare_plugin_market', '/cloudflare/plugin-market-admin*'),
('plugins_cloudflare_plugin_market', '/plugins/eqadminPluginsCloudflare/cloudflare/plugin-market-admin*'),
('plugins_cloudflare_worker_admin', '/cloudflare/worker-admin*'),
('plugins_cloudflare_worker_admin', '/plugins/eqadminPluginsCloudflare/cloudflare/worker-admin*');

INSERT IGNORE INTO tmp_cloudflare_api_bind_defs (menu_code, router) VALUES
('plugins_cloudflare_overview', '/cloudflare/accounts*'),
('plugins_cloudflare_overview', '/plugins/eqadminPluginsCloudflare/cloudflare/accounts*'),
('plugins_cloudflare_accounts', '/cloudflare/accounts*'),
('plugins_cloudflare_accounts', '/plugins/eqadminPluginsCloudflare/cloudflare/accounts*'),
('plugins_cloudflare_dns', '/cloudflare/accounts*'),
('plugins_cloudflare_dns', '/plugins/eqadminPluginsCloudflare/cloudflare/accounts*'),
('plugins_cloudflare_r2', '/cloudflare/accounts*'),
('plugins_cloudflare_r2', '/plugins/eqadminPluginsCloudflare/cloudflare/accounts*'),
('plugins_cloudflare_firewall', '/cloudflare/accounts*'),
('plugins_cloudflare_firewall', '/plugins/eqadminPluginsCloudflare/cloudflare/accounts*'),
('plugins_cloudflare_rulesets', '/cloudflare/accounts*'),
('plugins_cloudflare_rulesets', '/plugins/eqadminPluginsCloudflare/cloudflare/accounts*'),
('plugins_cloudflare_cache', '/cloudflare/accounts*'),
('plugins_cloudflare_cache', '/plugins/eqadminPluginsCloudflare/cloudflare/accounts*'),
('plugins_cloudflare_ssl', '/cloudflare/accounts*'),
('plugins_cloudflare_ssl', '/plugins/eqadminPluginsCloudflare/cloudflare/accounts*'),
('plugins_cloudflare_zone_settings', '/cloudflare/accounts*'),
('plugins_cloudflare_zone_settings', '/plugins/eqadminPluginsCloudflare/cloudflare/accounts*'),
('plugins_cloudflare_workers', '/cloudflare/accounts*'),
('plugins_cloudflare_workers', '/plugins/eqadminPluginsCloudflare/cloudflare/accounts*'),
('plugins_cloudflare_kv', '/cloudflare/accounts*'),
('plugins_cloudflare_kv', '/plugins/eqadminPluginsCloudflare/cloudflare/accounts*'),
('plugins_cloudflare_analytics', '/cloudflare/accounts*'),
('plugins_cloudflare_analytics', '/plugins/eqadminPluginsCloudflare/cloudflare/accounts*'),
('plugins_cloudflare_page_rules', '/cloudflare/accounts*'),
('plugins_cloudflare_page_rules', '/plugins/eqadminPluginsCloudflare/cloudflare/accounts*'),
('plugins_cloudflare_logs', '/cloudflare/accounts*'),
('plugins_cloudflare_logs', '/plugins/eqadminPluginsCloudflare/cloudflare/accounts*'),
('plugins_cloudflare_plugin_market', '/cloudflare/accounts*'),
('plugins_cloudflare_plugin_market', '/plugins/eqadminPluginsCloudflare/cloudflare/accounts*'),
('plugins_cloudflare_worker_admin', '/cloudflare/accounts*'),
('plugins_cloudflare_worker_admin', '/plugins/eqadminPluginsCloudflare/cloudflare/accounts*');

INSERT INTO qixiaozhu_menu_api_bind
(
    menu_code,
    perm_id,
    enabled,
    create_user_id,
    update_user_id,
    create_time,
    update_time
)
SELECT
    b.menu_code,
    api.id,
    1,
    1,
    1,
    NOW(),
    NOW()
FROM tmp_cloudflare_api_bind_defs b
JOIN qixiaozhu_perms api
  ON api.deleted = 0
 AND api.status = 'ok'
 AND api.`type` = 'api'
 AND api.router = b.router
LEFT JOIN qixiaozhu_menu_api_bind mb
  ON mb.menu_code = b.menu_code
 AND mb.perm_id = api.id
WHERE mb.id IS NULL;

UPDATE qixiaozhu_menu_api_bind b
JOIN qixiaozhu_perms api
  ON api.id = b.perm_id
JOIN tmp_cloudflare_api_bind_defs d
  ON d.menu_code = b.menu_code
 AND d.router = api.router
SET b.enabled = 1,
    b.update_time = NOW(),
    b.update_user_id = 1;

DROP TEMPORARY TABLE IF EXISTS tmp_cloudflare_api_bind_defs;
DROP TEMPORARY TABLE IF EXISTS tmp_cloudflare_api_defs;
