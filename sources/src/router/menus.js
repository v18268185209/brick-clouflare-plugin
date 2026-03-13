export const HOST_ROUTE_PREFIX = "/dashboard/plugins/cloudflare";

export const menuItems = [
  { path: "/overview", fullPath: `${HOST_ROUTE_PREFIX}/overview`, title: "总览", menuCode: "plugins_cloudflare_overview" },
  { path: "/accounts", fullPath: `${HOST_ROUTE_PREFIX}/accounts`, title: "账号管理", menuCode: "plugins_cloudflare_accounts" },
  { path: "/dns", fullPath: `${HOST_ROUTE_PREFIX}/dns`, title: "解析记录", menuCode: "plugins_cloudflare_dns" },
  { path: "/r2", fullPath: `${HOST_ROUTE_PREFIX}/r2`, title: "对象存储", menuCode: "plugins_cloudflare_r2" },
  { path: "/firewall", fullPath: `${HOST_ROUTE_PREFIX}/firewall`, title: "防火墙规则", menuCode: "plugins_cloudflare_firewall" },
  { path: "/rulesets", fullPath: `${HOST_ROUTE_PREFIX}/rulesets`, title: "安全规则集", menuCode: "plugins_cloudflare_rulesets" },
  { path: "/cache", fullPath: `${HOST_ROUTE_PREFIX}/cache`, title: "缓存策略", menuCode: "plugins_cloudflare_cache" },
  { path: "/ssl", fullPath: `${HOST_ROUTE_PREFIX}/ssl`, title: "证书与加密", menuCode: "plugins_cloudflare_ssl" },
  { path: "/zone-settings", fullPath: `${HOST_ROUTE_PREFIX}/zone-settings`, title: "站点高级设置", menuCode: "plugins_cloudflare_zone_settings" },
  { path: "/workers", fullPath: `${HOST_ROUTE_PREFIX}/workers`, title: "边缘脚本", menuCode: "plugins_cloudflare_workers" },
  { path: "/kv", fullPath: `${HOST_ROUTE_PREFIX}/kv`, title: "键值存储", menuCode: "plugins_cloudflare_kv" },
  { path: "/analytics", fullPath: `${HOST_ROUTE_PREFIX}/analytics`, title: "分析报表", menuCode: "plugins_cloudflare_analytics" },
  { path: "/page-rules", fullPath: `${HOST_ROUTE_PREFIX}/page-rules`, title: "页面规则", menuCode: "plugins_cloudflare_page_rules" },
  { path: "/logs", fullPath: `${HOST_ROUTE_PREFIX}/logs`, title: "操作日志", menuCode: "plugins_cloudflare_logs" },
  { path: "/plugin-market", fullPath: `${HOST_ROUTE_PREFIX}/plugin-market`, title: "插件市场", menuCode: "plugins_cloudflare_plugin_market" },
  { path: "/worker-admin", fullPath: `${HOST_ROUTE_PREFIX}/worker-admin`, title: "授权中心", menuCode: "plugins_cloudflare_worker_admin" }
];
