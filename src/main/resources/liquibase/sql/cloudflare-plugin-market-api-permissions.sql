-- Seed API permission for plugin market admin endpoints.

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'api', 'Cloudflare Plugin Market Admin API',
       '/plugins/eqadminPluginsCloudflare/cloudflare/plugin-market-admin/**',
       'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms
    WHERE `type` = 'api'
      AND router = '/plugins/eqadminPluginsCloudflare/cloudflare/plugin-market-admin/**'
);

INSERT INTO qixiaozhu_role_perms (role_id, perm_id)
SELECT r.id, p.id
FROM qixiaozhu_role r
JOIN qixiaozhu_perms p
  ON p.router = '/plugins/eqadminPluginsCloudflare/cloudflare/plugin-market-admin/**'
WHERE r.code = 'sys'
  AND r.status = 'ok'
  AND p.`type` = 'api'
  AND NOT EXISTS (
      SELECT 1
      FROM qixiaozhu_role_perms rp
      WHERE rp.role_id = r.id
        AND rp.perm_id = p.id
  );
