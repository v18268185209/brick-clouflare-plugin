-- Seed button permissions for worker admin migration page.

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Worker Admin - Save Config', 'btn:cf:workeradmin:save',
       'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:workeradmin:save'
);

INSERT INTO qixiaozhu_perms (`type`, `desc`, router, `status`, deleted, create_time, update_time, create_user_id, update_user_id)
SELECT 'btn', 'Worker Admin - Edit License', 'btn:cf:workeradmin:license:edit',
       'ok', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM qixiaozhu_perms WHERE `type` = 'btn' AND router = 'btn:cf:workeradmin:license:edit'
);

INSERT INTO qixiaozhu_role_perms (role_id, perm_id)
SELECT r.id, p.id
FROM qixiaozhu_role r
JOIN qixiaozhu_perms p
  ON p.router IN (
      'btn:cf:workeradmin:save',
      'btn:cf:workeradmin:license:edit'
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
