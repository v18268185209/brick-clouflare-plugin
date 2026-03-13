-- v2: Seed module-level API permissions for Cloudflare plugin.
-- Idempotent statements, safe for repeated execution.

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'api', 'Cloudflare Accounts API', '/plugins/eqadminPluginsCloudflare/cloudflare/accounts/**', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'api' AND router = '/plugins/eqadminPluginsCloudflare/cloudflare/accounts/**'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'api', 'Cloudflare Overview API', '/plugins/eqadminPluginsCloudflare/cloudflare/overview/**', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'api' AND router = '/plugins/eqadminPluginsCloudflare/cloudflare/overview/**'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'api', 'Cloudflare DNS API', '/plugins/eqadminPluginsCloudflare/cloudflare/dns/**', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'api' AND router = '/plugins/eqadminPluginsCloudflare/cloudflare/dns/**'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'api', 'Cloudflare R2 API', '/plugins/eqadminPluginsCloudflare/cloudflare/r2/**', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'api' AND router = '/plugins/eqadminPluginsCloudflare/cloudflare/r2/**'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'api', 'Cloudflare Firewall API', '/plugins/eqadminPluginsCloudflare/cloudflare/firewall/**', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'api' AND router = '/plugins/eqadminPluginsCloudflare/cloudflare/firewall/**'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'api', 'Cloudflare Cache API', '/plugins/eqadminPluginsCloudflare/cloudflare/cache/**', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'api' AND router = '/plugins/eqadminPluginsCloudflare/cloudflare/cache/**'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'api', 'Cloudflare SSL API', '/plugins/eqadminPluginsCloudflare/cloudflare/ssl/**', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'api' AND router = '/plugins/eqadminPluginsCloudflare/cloudflare/ssl/**'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'api', 'Cloudflare Zone Settings API', '/plugins/eqadminPluginsCloudflare/cloudflare/zone-settings/**', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'api' AND router = '/plugins/eqadminPluginsCloudflare/cloudflare/zone-settings/**'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'api', 'Cloudflare Workers API', '/plugins/eqadminPluginsCloudflare/cloudflare/workers/**', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'api' AND router = '/plugins/eqadminPluginsCloudflare/cloudflare/workers/**'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'api', 'Cloudflare KV API', '/plugins/eqadminPluginsCloudflare/cloudflare/kv/**', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'api' AND router = '/plugins/eqadminPluginsCloudflare/cloudflare/kv/**'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'api', 'Cloudflare Rulesets API', '/plugins/eqadminPluginsCloudflare/cloudflare/rulesets/**', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'api' AND router = '/plugins/eqadminPluginsCloudflare/cloudflare/rulesets/**'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'api', 'Cloudflare Page Rules API', '/plugins/eqadminPluginsCloudflare/cloudflare/page-rules/**', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'api' AND router = '/plugins/eqadminPluginsCloudflare/cloudflare/page-rules/**'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'api', 'Cloudflare Analytics API', '/plugins/eqadminPluginsCloudflare/cloudflare/analytics/**', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'api' AND router = '/plugins/eqadminPluginsCloudflare/cloudflare/analytics/**'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'api', 'Cloudflare Logs API', '/plugins/eqadminPluginsCloudflare/cloudflare/logs/**', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'api' AND router = '/plugins/eqadminPluginsCloudflare/cloudflare/logs/**'
);

INSERT INTO qixiaozhu_role_perms (role_id, perm_id)
SELECT r.id, p.id
FROM qixiaozhu_role r
JOIN qixiaozhu_perms p
  ON p.router IN (
      '/plugins/eqadminPluginsCloudflare/cloudflare/accounts/**',
      '/plugins/eqadminPluginsCloudflare/cloudflare/overview/**',
      '/plugins/eqadminPluginsCloudflare/cloudflare/dns/**',
      '/plugins/eqadminPluginsCloudflare/cloudflare/r2/**',
      '/plugins/eqadminPluginsCloudflare/cloudflare/firewall/**',
      '/plugins/eqadminPluginsCloudflare/cloudflare/cache/**',
      '/plugins/eqadminPluginsCloudflare/cloudflare/ssl/**',
      '/plugins/eqadminPluginsCloudflare/cloudflare/zone-settings/**',
      '/plugins/eqadminPluginsCloudflare/cloudflare/workers/**',
      '/plugins/eqadminPluginsCloudflare/cloudflare/kv/**',
      '/plugins/eqadminPluginsCloudflare/cloudflare/rulesets/**',
      '/plugins/eqadminPluginsCloudflare/cloudflare/page-rules/**',
      '/plugins/eqadminPluginsCloudflare/cloudflare/analytics/**',
      '/plugins/eqadminPluginsCloudflare/cloudflare/logs/**'
  )
WHERE r.code = 'sys'
  AND r.status = 'ok'
  AND p.`type` = 'api'
  AND NOT EXISTS (
      SELECT 1
      FROM qixiaozhu_role_perms rp
      WHERE rp.role_id = r.id
        AND rp.perm_id = p.id
  );
