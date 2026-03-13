-- Seed Cloudflare menu tree and permissions for EqAdmin role `sys`.
-- Idempotent statements.

INSERT INTO qixiaozhu_menu
(`name`, `type`, icon, icon_type, router, open_type, parent_id, create_time, update_time,
 create_user_id, update_user_id, deleted, status, component, sort, app_id, microapp_name)
SELECT
    'Cloudflare',
    'dir',
    'fas fa-cloud',
    'fontawesome',
    '/plugins/cloudflare',
    'menu',
    COALESCE(
      (SELECT parent_id FROM qixiaozhu_menu WHERE router = '/sys/app-market/list' ORDER BY id DESC LIMIT 1),
      (SELECT id FROM qixiaozhu_menu WHERE `type` = 'app' ORDER BY sort ASC, id ASC LIMIT 1),
      21
    ),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    0,
    'ok',
    '/layui/default/default.vue',
    COALESCE(
      (SELECT sort + 1 FROM qixiaozhu_menu WHERE router = '/sys/app-market/list' ORDER BY id DESC LIMIT 1),
      120
    ),
    COALESCE(
      (SELECT app_id FROM qixiaozhu_menu WHERE router = '/sys/app-market/list' ORDER BY id DESC LIMIT 1),
      (SELECT id FROM qixiaozhu_menu WHERE `type` = 'app' ORDER BY sort ASC, id ASC LIMIT 1),
      21
    ),
    ''
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare'
);

INSERT INTO qixiaozhu_menu
(`name`, `type`, icon, icon_type, router, open_type, parent_id, create_time, update_time,
 create_user_id, update_user_id, deleted, status, component, sort, app_id, microapp_name)
SELECT
    'Overview',
    'menu',
    'fas fa-chart-line',
    'fontawesome',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/dashboard',
    'iframe',
    COALESCE((SELECT id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 0),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    0,
    'ok',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/dashboard',
    1,
    COALESCE((SELECT app_id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 21),
    ''
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_menu WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/dashboard'
);

INSERT INTO qixiaozhu_menu
(`name`, `type`, icon, icon_type, router, open_type, parent_id, create_time, update_time,
 create_user_id, update_user_id, deleted, status, component, sort, app_id, microapp_name)
SELECT
    'Accounts',
    'menu',
    'fas fa-user-shield',
    'fontawesome',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/accounts',
    'iframe',
    COALESCE((SELECT id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 0),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    0,
    'ok',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/accounts',
    2,
    COALESCE((SELECT app_id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 21),
    ''
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_menu WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/accounts'
);

INSERT INTO qixiaozhu_menu
(`name`, `type`, icon, icon_type, router, open_type, parent_id, create_time, update_time,
 create_user_id, update_user_id, deleted, status, component, sort, app_id, microapp_name)
SELECT
    'DNS',
    'menu',
    'fas fa-network-wired',
    'fontawesome',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/dns',
    'iframe',
    COALESCE((SELECT id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 0),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    0,
    'ok',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/dns',
    3,
    COALESCE((SELECT app_id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 21),
    ''
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_menu WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/dns'
);

INSERT INTO qixiaozhu_menu
(`name`, `type`, icon, icon_type, router, open_type, parent_id, create_time, update_time,
 create_user_id, update_user_id, deleted, status, component, sort, app_id, microapp_name)
SELECT
    'R2 Buckets',
    'menu',
    'fas fa-database',
    'fontawesome',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/r2',
    'iframe',
    COALESCE((SELECT id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 0),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    0,
    'ok',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/r2',
    4,
    COALESCE((SELECT app_id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 21),
    ''
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_menu WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/r2'
);

INSERT INTO qixiaozhu_menu
(`name`, `type`, icon, icon_type, router, open_type, parent_id, create_time, update_time,
 create_user_id, update_user_id, deleted, status, component, sort, app_id, microapp_name)
SELECT
    'Firewall',
    'menu',
    'fas fa-shield-alt',
    'fontawesome',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/firewall',
    'iframe',
    COALESCE((SELECT id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 0),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    0,
    'ok',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/firewall',
    5,
    COALESCE((SELECT app_id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 21),
    ''
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_menu WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/firewall'
);

INSERT INTO qixiaozhu_menu
(`name`, `type`, icon, icon_type, router, open_type, parent_id, create_time, update_time,
 create_user_id, update_user_id, deleted, status, component, sort, app_id, microapp_name)
SELECT
    'Cache',
    'menu',
    'fas fa-bolt',
    'fontawesome',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/cache',
    'iframe',
    COALESCE((SELECT id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 0),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    0,
    'ok',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/cache',
    6,
    COALESCE((SELECT app_id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 21),
    ''
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_menu WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/cache'
);

INSERT INTO qixiaozhu_menu
(`name`, `type`, icon, icon_type, router, open_type, parent_id, create_time, update_time,
 create_user_id, update_user_id, deleted, status, component, sort, app_id, microapp_name)
SELECT
    'SSL/TLS',
    'menu',
    'fas fa-lock',
    'fontawesome',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/ssl',
    'iframe',
    COALESCE((SELECT id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 0),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    0,
    'ok',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/ssl',
    7,
    COALESCE((SELECT app_id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 21),
    ''
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_menu WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/ssl'
);

INSERT INTO qixiaozhu_menu
(`name`, `type`, icon, icon_type, router, open_type, parent_id, create_time, update_time,
 create_user_id, update_user_id, deleted, status, component, sort, app_id, microapp_name)
SELECT
    'Zone Settings',
    'menu',
    'fas fa-sliders-h',
    'fontawesome',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/zoneset',
    'iframe',
    COALESCE((SELECT id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 0),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    0,
    'ok',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/zoneset',
    8,
    COALESCE((SELECT app_id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 21),
    ''
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_menu WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/zoneset'
);

INSERT INTO qixiaozhu_menu
(`name`, `type`, icon, icon_type, router, open_type, parent_id, create_time, update_time,
 create_user_id, update_user_id, deleted, status, component, sort, app_id, microapp_name)
SELECT
    'Workers',
    'menu',
    'fas fa-code',
    'fontawesome',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/workers',
    'iframe',
    COALESCE((SELECT id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 0),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    0,
    'ok',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/workers',
    9,
    COALESCE((SELECT app_id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 21),
    ''
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_menu WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/workers'
);

INSERT INTO qixiaozhu_menu
(`name`, `type`, icon, icon_type, router, open_type, parent_id, create_time, update_time,
 create_user_id, update_user_id, deleted, status, component, sort, app_id, microapp_name)
SELECT
    'WAF Rulesets',
    'menu',
    'fas fa-layer-group',
    'fontawesome',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/rulesets',
    'iframe',
    COALESCE((SELECT id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 0),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    0,
    'ok',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/rulesets',
    10,
    COALESCE((SELECT app_id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 21),
    ''
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_menu WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/rulesets'
);

INSERT INTO qixiaozhu_menu
(`name`, `type`, icon, icon_type, router, open_type, parent_id, create_time, update_time,
 create_user_id, update_user_id, deleted, status, component, sort, app_id, microapp_name)
SELECT
    'KV',
    'menu',
    'fas fa-key',
    'fontawesome',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/kv',
    'iframe',
    COALESCE((SELECT id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 0),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    0,
    'ok',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/kv',
    11,
    COALESCE((SELECT app_id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 21),
    ''
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_menu WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/kv'
);

INSERT INTO qixiaozhu_menu
(`name`, `type`, icon, icon_type, router, open_type, parent_id, create_time, update_time,
 create_user_id, update_user_id, deleted, status, component, sort, app_id, microapp_name)
SELECT
    'Analytics',
    'menu',
    'fas fa-chart-bar',
    'fontawesome',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/analytics',
    'iframe',
    COALESCE((SELECT id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 0),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    0,
    'ok',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/analytics',
    12,
    COALESCE((SELECT app_id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 21),
    ''
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_menu WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/analytics'
);

INSERT INTO qixiaozhu_menu
(`name`, `type`, icon, icon_type, router, open_type, parent_id, create_time, update_time,
 create_user_id, update_user_id, deleted, status, component, sort, app_id, microapp_name)
SELECT
    'Page Rules',
    'menu',
    'fas fa-sitemap',
    'fontawesome',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/page-rules',
    'iframe',
    COALESCE((SELECT id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 0),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    0,
    'ok',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/page-rules',
    13,
    COALESCE((SELECT app_id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 21),
    ''
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_menu WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/page-rules'
);

INSERT INTO qixiaozhu_menu
(`name`, `type`, icon, icon_type, router, open_type, parent_id, create_time, update_time,
 create_user_id, update_user_id, deleted, status, component, sort, app_id, microapp_name)
SELECT
    'Audit Logs',
    'menu',
    'fas fa-list',
    'fontawesome',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/logs',
    'iframe',
    COALESCE((SELECT id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 0),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    0,
    'ok',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/logs',
    14,
    COALESCE((SELECT app_id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 21),
    ''
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_menu WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/logs'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'menu', 'Cloudflare', '/plugins/cloudflare', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE router = '/plugins/cloudflare'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'menu', 'Cloudflare Overview', '/plugins/eqadminPluginsCloudflare/console/index.html#/dashboard', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/dashboard'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'menu', 'Cloudflare Accounts', '/plugins/eqadminPluginsCloudflare/console/index.html#/accounts', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/accounts'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'menu', 'Cloudflare DNS', '/plugins/eqadminPluginsCloudflare/console/index.html#/dns', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/dns'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'menu', 'Cloudflare R2', '/plugins/eqadminPluginsCloudflare/console/index.html#/r2', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/r2'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'menu', 'Cloudflare Firewall', '/plugins/eqadminPluginsCloudflare/console/index.html#/firewall', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/firewall'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'menu', 'Cloudflare Cache', '/plugins/eqadminPluginsCloudflare/console/index.html#/cache', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/cache'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'menu', 'Cloudflare SSL/TLS', '/plugins/eqadminPluginsCloudflare/console/index.html#/ssl', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/ssl'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'menu', 'Cloudflare Zone Settings', '/plugins/eqadminPluginsCloudflare/console/index.html#/zoneset', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/zoneset'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'menu', 'Cloudflare Workers', '/plugins/eqadminPluginsCloudflare/console/index.html#/workers', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/workers'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'menu', 'Cloudflare WAF Rulesets', '/plugins/eqadminPluginsCloudflare/console/index.html#/rulesets', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/rulesets'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'menu', 'Cloudflare KV', '/plugins/eqadminPluginsCloudflare/console/index.html#/kv', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/kv'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'menu', 'Cloudflare Analytics', '/plugins/eqadminPluginsCloudflare/console/index.html#/analytics', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/analytics'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'menu', 'Cloudflare Page Rules', '/plugins/eqadminPluginsCloudflare/console/index.html#/page-rules', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/page-rules'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'menu', 'Cloudflare Logs', '/plugins/eqadminPluginsCloudflare/console/index.html#/logs', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/logs'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'api', 'Cloudflare Plugin API', '/plugins/eqadminPluginsCloudflare/cloudflare/**', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'api' AND router = '/plugins/eqadminPluginsCloudflare/cloudflare/**'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'api', 'Cloudflare Plugin Console', '/plugins/eqadminPluginsCloudflare/console/**', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'api' AND router = '/plugins/eqadminPluginsCloudflare/console/**'
);

INSERT INTO qixiaozhu_role_perms (role_id, perm_id)
SELECT r.id, p.id
FROM qixiaozhu_role r
JOIN qixiaozhu_perms p
  ON p.router IN (
      '/plugins/cloudflare',
      '/plugins/eqadminPluginsCloudflare/console/index.html#/dashboard',
      '/plugins/eqadminPluginsCloudflare/console/index.html#/accounts',
      '/plugins/eqadminPluginsCloudflare/console/index.html#/dns',
      '/plugins/eqadminPluginsCloudflare/console/index.html#/r2',
      '/plugins/eqadminPluginsCloudflare/console/index.html#/firewall',
      '/plugins/eqadminPluginsCloudflare/console/index.html#/cache',
      '/plugins/eqadminPluginsCloudflare/console/index.html#/ssl',
      '/plugins/eqadminPluginsCloudflare/console/index.html#/zoneset',
      '/plugins/eqadminPluginsCloudflare/console/index.html#/workers',
      '/plugins/eqadminPluginsCloudflare/console/index.html#/rulesets',
      '/plugins/eqadminPluginsCloudflare/console/index.html#/kv',
      '/plugins/eqadminPluginsCloudflare/console/index.html#/analytics',
      '/plugins/eqadminPluginsCloudflare/console/index.html#/page-rules',
      '/plugins/eqadminPluginsCloudflare/console/index.html#/logs',
      '/plugins/eqadminPluginsCloudflare/cloudflare/**',
      '/plugins/eqadminPluginsCloudflare/console/**'
  )
WHERE r.code = 'sys'
  AND r.status = 'ok'
  AND NOT EXISTS (
      SELECT 1
      FROM qixiaozhu_role_perms rp
      WHERE rp.role_id = r.id
        AND rp.perm_id = p.id
  );
