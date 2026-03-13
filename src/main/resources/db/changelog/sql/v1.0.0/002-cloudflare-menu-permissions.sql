SET @cf_parent_id := (
    SELECT m.id
    FROM qixiaozhu_menu m
    WHERE m.deleted = 0
      AND m.status = 'ok'
      AND m.menu_code = 'configmanager'
    ORDER BY m.id DESC
    LIMIT 1
);

SET @cf_parent_id := COALESCE(
    @cf_parent_id,
    (
        SELECT m.id
        FROM qixiaozhu_menu m
        WHERE m.deleted = 0
          AND m.status = 'ok'
          AND m.menu_code = 'sysmanager'
        ORDER BY m.id DESC
        LIMIT 1
    ),
    21
);

SET @cf_app_id := (
    SELECT m.app_id
    FROM qixiaozhu_menu m
    WHERE m.id = @cf_parent_id
    LIMIT 1
);

SET @cf_app_id := COALESCE(@cf_app_id, 21);
SET @cf_microapp_name := 'eqadminPluginsCloudflare';

INSERT INTO qixiaozhu_menu
(
    name,
    `type`,
    icon,
    icon_type,
    router,
    open_type,
    parent_id,
    create_time,
    update_time,
    create_user_id,
    update_user_id,
    deleted,
    status,
    component,
    redirect,
    sort,
    app_id,
    microapp_name,
    menu_code
)
SELECT
    '边缘云平台',
    'dir',
    'fas fa-cloud',
    'fontawesome',
    '/dashboard/plugins/cloudflare',
    'menu',
    @cf_parent_id,
    NOW(),
    NOW(),
    1,
    1,
    0,
    'ok',
    NULL,
    '/dashboard/plugins/cloudflare/overview',
    87,
    @cf_app_id,
    @cf_microapp_name,
    'plugins_cloudflare_manager'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM qixiaozhu_menu m
    WHERE m.deleted = 0
      AND (m.menu_code = 'plugins_cloudflare_manager' OR m.router = '/dashboard/plugins/cloudflare')
);

UPDATE qixiaozhu_menu m
SET m.name = '边缘云平台',
    m.`type` = 'dir',
    m.icon = 'fas fa-cloud',
    m.icon_type = 'fontawesome',
    m.router = '/dashboard/plugins/cloudflare',
    m.open_type = 'menu',
    m.parent_id = @cf_parent_id,
    m.update_time = NOW(),
    m.update_user_id = 1,
    m.deleted = 0,
    m.status = 'ok',
    m.component = NULL,
    m.redirect = '/dashboard/plugins/cloudflare/overview',
    m.sort = 87,
    m.app_id = COALESCE(m.app_id, @cf_app_id),
    m.microapp_name = @cf_microapp_name,
    m.menu_code = 'plugins_cloudflare_manager'
WHERE m.menu_code = 'plugins_cloudflare_manager'
   OR m.router = '/dashboard/plugins/cloudflare';

SET @cf_root_id := (
    SELECT m.id
    FROM qixiaozhu_menu m
    WHERE m.deleted = 0
      AND m.menu_code = 'plugins_cloudflare_manager'
    ORDER BY m.id DESC
    LIMIT 1
);

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
    'menu',
    '边缘云平台',
    '/dashboard/plugins/cloudflare',
    'ok',
    0,
    NOW(),
    NOW(),
    1,
    1,
    'plugins_cloudflare_manager'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM qixiaozhu_perms p
    WHERE p.deleted = 0
      AND p.router = '/dashboard/plugins/cloudflare'
);

UPDATE qixiaozhu_perms p
SET p.`type` = 'menu',
    p.`desc` = '边缘云平台',
    p.menu_code = 'plugins_cloudflare_manager',
    p.status = 'ok',
    p.deleted = 0,
    p.update_time = NOW(),
    p.update_user_id = 1
WHERE p.router = '/dashboard/plugins/cloudflare';

DROP TEMPORARY TABLE IF EXISTS tmp_cloudflare_page_defs;
CREATE TEMPORARY TABLE tmp_cloudflare_page_defs (
    menu_code VARCHAR(128) PRIMARY KEY,
    menu_name VARCHAR(128) NOT NULL,
    router VARCHAR(255) NOT NULL,
    component VARCHAR(255) NOT NULL,
    sort_no INT NOT NULL
);

INSERT INTO tmp_cloudflare_page_defs (menu_code, menu_name, router, component, sort_no) VALUES
('plugins_cloudflare_overview', '总览', '/dashboard/plugins/cloudflare/overview', '/pages/cloudflare/overview/index.vue', 10),
('plugins_cloudflare_accounts', '账号管理', '/dashboard/plugins/cloudflare/accounts', '/pages/cloudflare/accounts/index.vue', 20),
('plugins_cloudflare_dns', '解析记录', '/dashboard/plugins/cloudflare/dns', '/pages/cloudflare/dns/index.vue', 30),
('plugins_cloudflare_r2', '对象存储', '/dashboard/plugins/cloudflare/r2', '/pages/cloudflare/r2/index.vue', 40),
('plugins_cloudflare_firewall', '防火墙规则', '/dashboard/plugins/cloudflare/firewall', '/pages/cloudflare/firewall/index.vue', 50),
('plugins_cloudflare_rulesets', '安全规则集', '/dashboard/plugins/cloudflare/rulesets', '/pages/cloudflare/rulesets/index.vue', 60),
('plugins_cloudflare_cache', '缓存策略', '/dashboard/plugins/cloudflare/cache', '/pages/cloudflare/cache/index.vue', 70),
('plugins_cloudflare_ssl', '证书与加密', '/dashboard/plugins/cloudflare/ssl', '/pages/cloudflare/ssl/index.vue', 80),
('plugins_cloudflare_zone_settings', '站点高级设置', '/dashboard/plugins/cloudflare/zone-settings', '/pages/cloudflare/zone-settings/index.vue', 90),
('plugins_cloudflare_workers', '边缘脚本', '/dashboard/plugins/cloudflare/workers', '/pages/cloudflare/workers/index.vue', 100),
('plugins_cloudflare_kv', '键值存储', '/dashboard/plugins/cloudflare/kv', '/pages/cloudflare/kv/index.vue', 110),
('plugins_cloudflare_analytics', '分析报表', '/dashboard/plugins/cloudflare/analytics', '/pages/cloudflare/analytics/index.vue', 120),
('plugins_cloudflare_page_rules', '页面规则', '/dashboard/plugins/cloudflare/page-rules', '/pages/cloudflare/page-rules/index.vue', 130),
('plugins_cloudflare_logs', '操作日志', '/dashboard/plugins/cloudflare/logs', '/pages/cloudflare/logs/index.vue', 140),
('plugins_cloudflare_plugin_market', '插件市场', '/dashboard/plugins/cloudflare/plugin-market', '/pages/cloudflare/plugin-market/index.vue', 150),
('plugins_cloudflare_worker_admin', '授权中心', '/dashboard/plugins/cloudflare/worker-admin', '/pages/cloudflare/worker-admin/index.vue', 160);

INSERT INTO qixiaozhu_menu
(
    name,
    `type`,
    icon,
    icon_type,
    router,
    open_type,
    parent_id,
    create_time,
    update_time,
    create_user_id,
    update_user_id,
    deleted,
    status,
    component,
    redirect,
    sort,
    app_id,
    microapp_name,
    menu_code
)
SELECT
    p.menu_name,
    'menu',
    'fas fa-puzzle-piece',
    'fontawesome',
    p.router,
    'menu',
    @cf_root_id,
    NOW(),
    NOW(),
    1,
    1,
    0,
    'ok',
    p.component,
    NULL,
    p.sort_no,
    @cf_app_id,
    @cf_microapp_name,
    p.menu_code
FROM tmp_cloudflare_page_defs p
WHERE @cf_root_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM qixiaozhu_menu m
      WHERE m.deleted = 0
        AND (m.menu_code = p.menu_code OR m.router = p.router)
  );

UPDATE qixiaozhu_menu m
JOIN tmp_cloudflare_page_defs p
  ON m.menu_code = p.menu_code OR m.router = p.router
SET m.name = p.menu_name,
    m.`type` = 'menu',
    m.icon = 'fas fa-puzzle-piece',
    m.icon_type = 'fontawesome',
    m.router = p.router,
    m.open_type = 'menu',
    m.parent_id = COALESCE(@cf_root_id, m.parent_id),
    m.update_time = NOW(),
    m.update_user_id = 1,
    m.deleted = 0,
    m.status = 'ok',
    m.component = p.component,
    m.sort = p.sort_no,
    m.app_id = COALESCE(m.app_id, @cf_app_id),
    m.microapp_name = @cf_microapp_name,
    m.menu_code = p.menu_code
WHERE m.deleted = 0
   OR m.menu_code = p.menu_code
   OR m.router = p.router;

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
    'menu',
    p.menu_name,
    p.router,
    'ok',
    0,
    NOW(),
    NOW(),
    1,
    1,
    p.menu_code
FROM tmp_cloudflare_page_defs p
LEFT JOIN qixiaozhu_perms ep
  ON ep.router = p.router
 AND ep.deleted = 0
WHERE ep.id IS NULL;

UPDATE qixiaozhu_perms pr
JOIN tmp_cloudflare_page_defs p
  ON p.router = pr.router
SET pr.`type` = 'menu',
    pr.`desc` = p.menu_name,
    pr.menu_code = p.menu_code,
    pr.status = 'ok',
    pr.deleted = 0,
    pr.update_time = NOW(),
    pr.update_user_id = 1;

DROP TEMPORARY TABLE IF EXISTS tmp_cloudflare_button_defs;
CREATE TEMPORARY TABLE tmp_cloudflare_button_defs (
    router VARCHAR(255) PRIMARY KEY,
    button_desc VARCHAR(64) NOT NULL
);

INSERT INTO tmp_cloudflare_button_defs (router, button_desc) VALUES
('btn:plugins_cloudflare_accounts:create', '新增'),
('btn:plugins_cloudflare_accounts:update', '编辑'),
('btn:plugins_cloudflare_accounts:verify', '验证'),
('btn:plugins_cloudflare_accounts:loadzones', '加载站点'),
('btn:plugins_cloudflare_accounts:delete', '删除'),
('btn:plugins_cloudflare_dns:create', '新增'),
('btn:plugins_cloudflare_dns:update', '编辑'),
('btn:plugins_cloudflare_dns:delete', '删除'),
('btn:plugins_cloudflare_r2:create', '新建存储桶'),
('btn:plugins_cloudflare_r2:delete', '删除存储桶'),
('btn:plugins_cloudflare_firewall:create', '新增'),
('btn:plugins_cloudflare_firewall:toggle', '切换'),
('btn:plugins_cloudflare_firewall:delete', '删除'),
('btn:plugins_cloudflare_rulesets:create', '新增'),
('btn:plugins_cloudflare_rulesets:toggle', '切换'),
('btn:plugins_cloudflare_rulesets:delete', '删除'),
('btn:plugins_cloudflare_cache:update', '保存'),
('btn:plugins_cloudflare_cache:purge', '清空缓存'),
('btn:plugins_cloudflare_ssl:update', '保存'),
('btn:plugins_cloudflare_zone_settings:update', '保存'),
('btn:plugins_cloudflare_workers:read', '查看脚本'),
('btn:plugins_cloudflare_workers:publish', '发布脚本'),
('btn:plugins_cloudflare_workers:delete', '删除脚本'),
('btn:plugins_cloudflare_workers:routecreate', '新增路由'),
('btn:plugins_cloudflare_workers:routedelete', '删除路由'),
('btn:plugins_cloudflare_kv:nscreate', '新建命名空间'),
('btn:plugins_cloudflare_kv:nsdelete', '删除命名空间'),
('btn:plugins_cloudflare_kv:keyread', '查看键值'),
('btn:plugins_cloudflare_kv:keywrite', '保存键值'),
('btn:plugins_cloudflare_kv:keydelete', '删除键值'),
('btn:plugins_cloudflare_page_rules:create', '新增'),
('btn:plugins_cloudflare_page_rules:toggle', '切换'),
('btn:plugins_cloudflare_page_rules:delete', '删除'),
('btn:plugins_cloudflare_logs:clear', '清空'),
('btn:plugins_cloudflare_plugin_market:pluginsave', '保存插件'),
('btn:plugins_cloudflare_plugin_market:pluginpublish', '发布插件'),
('btn:plugins_cloudflare_plugin_market:pluginoffline', '下线插件'),
('btn:plugins_cloudflare_plugin_market:plugindraft', '转为草稿'),
('btn:plugins_cloudflare_plugin_market:plugindelete', '删除插件'),
('btn:plugins_cloudflare_plugin_market:releasesave', '保存版本'),
('btn:plugins_cloudflare_plugin_market:orderview', '查看订单'),
('btn:plugins_cloudflare_worker_admin:save', '保存配置'),
('btn:plugins_cloudflare_worker_admin:emailtest', '发送测试邮件'),
('btn:plugins_cloudflare_worker_admin:passwordchange', '修改密码'),
('btn:plugins_cloudflare_worker_admin:licenseedit', '调整授权');

INSERT INTO qixiaozhu_menu
(
    name,
    `type`,
    icon,
    icon_type,
    router,
    open_type,
    parent_id,
    create_time,
    update_time,
    create_user_id,
    update_user_id,
    deleted,
    status,
    component,
    redirect,
    sort,
    app_id,
    microapp_name,
    menu_code
)
SELECT
    CASE
        WHEN CHAR_LENGTH(CONCAT(pm.name, '::', b.button_desc)) <= 32
            THEN CONCAT(pm.name, '::', b.button_desc)
        ELSE b.button_desc
    END,
    'button',
    NULL,
    NULL,
    b.router,
    NULL,
    pm.id,
    NOW(),
    NOW(),
    1,
    1,
    0,
    'ok',
    NULL,
    NULL,
    pm.sort,
    @cf_app_id,
    @cf_microapp_name,
    pm.menu_code
FROM tmp_cloudflare_button_defs b
JOIN qixiaozhu_menu pm
  ON pm.deleted = 0
 AND pm.menu_code = SUBSTRING_INDEX(SUBSTRING_INDEX(b.router, ':', 2), ':', -1)
WHERE NOT EXISTS (
    SELECT 1
    FROM qixiaozhu_menu m
    WHERE m.deleted = 0
      AND m.router = b.router
);

UPDATE qixiaozhu_menu m
JOIN tmp_cloudflare_button_defs b
  ON b.router = m.router
JOIN qixiaozhu_menu pm
  ON pm.deleted = 0
 AND pm.menu_code = SUBSTRING_INDEX(SUBSTRING_INDEX(b.router, ':', 2), ':', -1)
SET m.name = CASE
                 WHEN CHAR_LENGTH(CONCAT(pm.name, '::', b.button_desc)) <= 32
                     THEN CONCAT(pm.name, '::', b.button_desc)
                 ELSE b.button_desc
             END,
    m.`type` = 'button',
    m.parent_id = pm.id,
    m.sort = pm.sort,
    m.app_id = COALESCE(m.app_id, @cf_app_id),
    m.microapp_name = @cf_microapp_name,
    m.menu_code = pm.menu_code,
    m.status = 'ok',
    m.deleted = 0,
    m.update_time = NOW(),
    m.update_user_id = 1
WHERE m.`type` = 'button'
   OR m.router = b.router;

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
    'button',
    m.name,
    m.router,
    'ok',
    0,
    NOW(),
    NOW(),
    1,
    1,
    m.menu_code
FROM qixiaozhu_menu m
JOIN tmp_cloudflare_button_defs b
  ON b.router = m.router
LEFT JOIN qixiaozhu_perms p
  ON p.router = m.router
 AND p.deleted = 0
WHERE m.deleted = 0
  AND m.`type` = 'button'
  AND p.id IS NULL;

UPDATE qixiaozhu_perms p
JOIN qixiaozhu_menu m
  ON m.router = p.router
JOIN tmp_cloudflare_button_defs b
  ON b.router = m.router
SET p.`type` = 'button',
    p.`desc` = m.name,
    p.menu_code = m.menu_code,
    p.status = 'ok',
    p.deleted = 0,
    p.update_time = NOW(),
    p.update_user_id = 1
WHERE m.`type` = 'button';

DROP TEMPORARY TABLE IF EXISTS tmp_cloudflare_button_defs;
DROP TEMPORARY TABLE IF EXISTS tmp_cloudflare_page_defs;
