-- Seed button permissions for plugin market admin UI.

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Plugin Market - Save Plugin', 'btn:cf:pm:plugin:save',
       'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:pm:plugin:save'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Plugin Market - Save Release', 'btn:cf:pm:release:save',
       'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:pm:release:save'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Plugin Market - Publish Plugin', 'btn:cf:pm:plugin:publish',
       'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:pm:plugin:publish'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Plugin Market - Offline Plugin', 'btn:cf:pm:plugin:offline',
       'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:pm:plugin:offline'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Plugin Market - Move Plugin To Draft', 'btn:cf:pm:plugin:draft',
       'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:pm:plugin:draft'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Plugin Market - Delete Plugin', 'btn:cf:pm:plugin:delete',
       'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:pm:plugin:delete'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Plugin Market - View Orders', 'btn:cf:pm:order:view',
       'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:pm:order:view'
);

INSERT INTO qixiaozhu_role_perms (role_id, perm_id)
SELECT r.id, p.id
FROM qixiaozhu_role r
JOIN qixiaozhu_perms p
  ON p.router IN (
      'btn:cf:pm:plugin:save',
      'btn:cf:pm:release:save',
      'btn:cf:pm:plugin:publish',
      'btn:cf:pm:plugin:offline',
      'btn:cf:pm:plugin:draft',
      'btn:cf:pm:plugin:delete',
      'btn:cf:pm:order:view'
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
