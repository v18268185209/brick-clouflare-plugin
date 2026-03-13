INSERT INTO qixiaozhu_role_perms (role_id, perm_id)
SELECT r.id, p.id
FROM qixiaozhu_role r
JOIN qixiaozhu_perms p
  ON p.deleted = 0
 AND p.status = 'ok'
 AND (
     p.menu_code = 'plugins_cloudflare_manager'
     OR p.menu_code LIKE 'plugins_cloudflare_%'
     OR p.router LIKE '/cloudflare/%'
     OR p.router LIKE '/plugins/eqadminPluginsCloudflare/cloudflare/%'
 )
WHERE r.code = 'sys'
  AND (r.status = 'ok' OR r.status IS NULL)
  AND NOT EXISTS (
      SELECT 1
      FROM qixiaozhu_role_perms rp
      WHERE rp.role_id = r.id
        AND rp.perm_id = p.id
  );
