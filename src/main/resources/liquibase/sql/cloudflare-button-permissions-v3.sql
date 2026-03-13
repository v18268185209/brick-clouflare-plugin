-- v3: Seed button-level permissions for Cloudflare plugin.
-- These codes are used by frontend `v-permission` to control operation buttons.
-- Idempotent statements, safe for repeated execution.

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Account - Create', 'btn:cf:acct:create', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:acct:create'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Account - Update', 'btn:cf:acct:update', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:acct:update'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Account - Delete', 'btn:cf:acct:delete', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:acct:delete'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Account - Verify', 'btn:cf:acct:verify', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:acct:verify'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Account - Load Zones', 'btn:cf:acct:zones', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:acct:zones'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare DNS - Create', 'btn:cf:dns:create', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:dns:create'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare DNS - Delete', 'btn:cf:dns:delete', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:dns:delete'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare R2 - Create', 'btn:cf:r2:create', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:r2:create'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare R2 - Delete', 'btn:cf:r2:delete', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:r2:delete'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Firewall - Create', 'btn:cf:fw:create', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:fw:create'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Firewall - Toggle', 'btn:cf:fw:toggle', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:fw:toggle'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Firewall - Delete', 'btn:cf:fw:delete', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:fw:delete'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Cache - Update', 'btn:cf:cache:update', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:cache:update'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Cache - Purge', 'btn:cf:cache:purge', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:cache:purge'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare SSL - Update', 'btn:cf:ssl:update', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:ssl:update'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Zone Settings - Update', 'btn:cf:zoneset:update', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:zoneset:update'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Worker - Read', 'btn:cf:worker:read', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:worker:read'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Worker - Publish', 'btn:cf:worker:publish', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:worker:publish'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Worker - Delete', 'btn:cf:worker:delete', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:worker:delete'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Route - Create', 'btn:cf:route:create', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:route:create'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Route - Delete', 'btn:cf:route:delete', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:route:delete'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare KV Namespace - Create', 'btn:cf:kv:ns:create', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:kv:ns:create'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare KV Namespace - Delete', 'btn:cf:kv:ns:delete', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:kv:ns:delete'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare KV Key - Read', 'btn:cf:kv:key:read', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:kv:key:read'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare KV Key - Write', 'btn:cf:kv:key:write', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:kv:key:write'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare KV Key - Delete', 'btn:cf:kv:key:delete', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:kv:key:delete'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Ruleset - Create', 'btn:cf:ruleset:create', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:ruleset:create'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Ruleset - Toggle', 'btn:cf:ruleset:toggle', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:ruleset:toggle'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Ruleset - Delete', 'btn:cf:ruleset:delete', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:ruleset:delete'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Page Rule - Create', 'btn:cf:pgr:create', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:pgr:create'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Page Rule - Toggle', 'btn:cf:pgr:toggle', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:pgr:toggle'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Page Rule - Delete', 'btn:cf:pgr:delete', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:pgr:delete'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Cloudflare Logs - Clear', 'btn:cf:logs:clear', 'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:logs:clear'
);

INSERT INTO qixiaozhu_role_perms (role_id, perm_id)
SELECT r.id, p.id
FROM qixiaozhu_role r
JOIN qixiaozhu_perms p
  ON p.router IN (
      'btn:cf:acct:create',
      'btn:cf:acct:update',
      'btn:cf:acct:delete',
      'btn:cf:acct:verify',
      'btn:cf:acct:zones',
      'btn:cf:dns:create',
      'btn:cf:dns:delete',
      'btn:cf:r2:create',
      'btn:cf:r2:delete',
      'btn:cf:fw:create',
      'btn:cf:fw:toggle',
      'btn:cf:fw:delete',
      'btn:cf:cache:update',
      'btn:cf:cache:purge',
      'btn:cf:ssl:update',
      'btn:cf:zoneset:update',
      'btn:cf:worker:read',
      'btn:cf:worker:publish',
      'btn:cf:worker:delete',
      'btn:cf:route:create',
      'btn:cf:route:delete',
      'btn:cf:kv:ns:create',
      'btn:cf:kv:ns:delete',
      'btn:cf:kv:key:read',
      'btn:cf:kv:key:write',
      'btn:cf:kv:key:delete',
      'btn:cf:ruleset:create',
      'btn:cf:ruleset:toggle',
      'btn:cf:ruleset:delete',
      'btn:cf:pgr:create',
      'btn:cf:pgr:toggle',
      'btn:cf:pgr:delete',
      'btn:cf:logs:clear'
  )
WHERE r.code = 'sys'
  AND r.status = 'ok'
  AND p.`type` = 'btn'
  AND NOT EXISTS (
      SELECT 1
      FROM qixiaozhu_role_perms rp
      WHERE rp.role_id = r.id
        AND rp.perm_id = p.id
  );

