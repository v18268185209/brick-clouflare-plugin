-- Seed plugin-market menu entries for Cloudflare plugin console.
-- Idempotent and safe to execute repeatedly.

INSERT INTO qixiaozhu_menu
(`name`, `type`, icon, icon_type, router, open_type, parent_id, create_time, update_time,
 create_user_id, update_user_id, deleted, status, component, sort, app_id, microapp_name)
SELECT
    'Plugin Market',
    'menu',
    'fas fa-store',
    'fontawesome',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/plugin-market',
    'iframe',
    COALESCE((SELECT id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 0),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    0,
    'ok',
    '/plugins/eqadminPluginsCloudflare/console/index.html#/plugin-market',
    15,
    COALESCE((SELECT app_id FROM qixiaozhu_menu WHERE router = '/plugins/cloudflare' ORDER BY id DESC LIMIT 1), 21),
    ''
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_menu
    WHERE router = '/plugins/eqadminPluginsCloudflare/console/index.html#/plugin-market'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'menu', 'Cloudflare Plugin Market', '/plugins/eqadminPluginsCloudflare/console/index.html#/plugin-market',
       'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms
    WHERE `type` = 'menu'
      AND router = '/plugins/eqadminPluginsCloudflare/console/index.html#/plugin-market'
);

INSERT INTO qixiaozhu_role_perms (role_id, perm_id)
SELECT r.id, p.id
FROM qixiaozhu_role r
JOIN qixiaozhu_perms p
  ON p.router = '/plugins/eqadminPluginsCloudflare/console/index.html#/plugin-market'
WHERE r.code = 'sys'
  AND r.status = 'ok'
  AND p.`type` = 'menu'
  AND NOT EXISTS (
      SELECT 1
      FROM qixiaozhu_role_perms rp
      WHERE rp.role_id = r.id
        AND rp.perm_id = p.id
  );
