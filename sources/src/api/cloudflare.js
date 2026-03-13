import http from "./http";

export const api = {
  overview: (accountId) =>
    http.get("/overview", { params: accountId ? { accountId } : {} }),

  logs: (limit = 50) => http.get("/logs", { params: { limit } }),
  clearLogs: () => http.delete("/logs"),

  listAccounts: () => http.get("/accounts"),
  createAccount: (data) => http.post("/accounts", data),
  updateAccount: (id, data) => http.put(`/accounts/${id}`, data),
  deleteAccount: (id) => http.delete(`/accounts/${id}`),
  verifyAccount: (id) => http.post(`/accounts/${id}/verify`),
  listZones: (id, params = {}) => http.get(`/accounts/${id}/zones`, { params }),

  listDnsRecords: (params) => http.get("/dns/records", { params }),
  createDnsRecord: (data) => http.post("/dns/records", data),
  updateDnsRecord: (recordId, data) => http.put(`/dns/records/${recordId}`, data),
  deleteDnsRecord: (recordId, params) =>
    http.delete(`/dns/records/${recordId}`, { params }),

  listR2Buckets: (params) => http.get("/r2/buckets", { params }),
  createR2Bucket: (data) => http.post("/r2/buckets", data),
  deleteR2Bucket: (bucketName, params) =>
    http.delete(`/r2/buckets/${bucketName}`, { params }),

  listFirewallRules: (params) => http.get("/firewall/rules", { params }),
  createFirewallRules: (data) => http.post("/firewall/rules", data),
  updateFirewallRule: (ruleId, data) => http.put(`/firewall/rules/${ruleId}`, data),
  deleteFirewallRule: (ruleId, params) =>
    http.delete(`/firewall/rules/${ruleId}`, { params }),

  getRulesetEntrypoint: (params) => http.get("/rulesets/entrypoint", { params }),
  updateRulesetEntrypoint: (data) => http.put("/rulesets/entrypoint", data),
  getRulesetDetail: (rulesetId, params) =>
    http.get(`/rulesets/${rulesetId}`, { params }),
  createRulesetRule: (rulesetId, data) =>
    http.post(`/rulesets/${rulesetId}/rules`, data),
  updateRulesetRule: (rulesetId, ruleId, data) =>
    http.put(`/rulesets/${rulesetId}/rules/${ruleId}`, data),
  deleteRulesetRule: (rulesetId, ruleId, params) =>
    http.delete(`/rulesets/${rulesetId}/rules/${ruleId}`, { params }),

  getCacheSettings: (params) => http.get("/cache/settings", { params }),
  patchCacheSettings: (data) => http.patch("/cache/settings", data),
  purgeCache: (data) => http.post("/cache/purge", data),

  getSslSettings: (params) => http.get("/ssl/settings", { params }),
  patchSslSettings: (data) => http.patch("/ssl/settings", data),

  getZoneAdvancedSettings: (params) =>
    http.get("/zone-settings/settings", { params }),
  patchZoneAdvancedSettings: (data) =>
    http.patch("/zone-settings/settings", data),

  listWorkerScripts: (params) => http.get("/workers/scripts", { params }),
  getWorkerScript: (scriptName, params) =>
    http.get(`/workers/scripts/${scriptName}`, { params }),
  putWorkerScript: (scriptName, data) =>
    http.put(`/workers/scripts/${scriptName}`, data),
  deleteWorkerScript: (scriptName, params) =>
    http.delete(`/workers/scripts/${scriptName}`, { params }),
  listWorkerRoutes: (params) => http.get("/workers/routes", { params }),
  createWorkerRoute: (data) => http.post("/workers/routes", data),
  deleteWorkerRoute: (routeId, params) =>
    http.delete(`/workers/routes/${routeId}`, { params }),

  listKvNamespaces: (params) => http.get("/kv/namespaces", { params }),
  createKvNamespace: (data) => http.post("/kv/namespaces", data),
  deleteKvNamespace: (namespaceId, params) =>
    http.delete(`/kv/namespaces/${namespaceId}`, { params }),
  listKvKeys: (namespaceId, params) =>
    http.get(`/kv/namespaces/${namespaceId}/keys`, { params }),
  getKvValue: (namespaceId, key, params) =>
    http.get(`/kv/namespaces/${namespaceId}/values/${encodeURIComponent(key)}`, {
      params
    }),
  putKvValue: (namespaceId, key, data) =>
    http.put(`/kv/namespaces/${namespaceId}/values/${encodeURIComponent(key)}`, data),
  deleteKvValue: (namespaceId, key, params) =>
    http.delete(`/kv/namespaces/${namespaceId}/values/${encodeURIComponent(key)}`, {
      params
    }),

  listPageRules: (params) => http.get("/page-rules", { params }),
  createPageRule: (data) => http.post("/page-rules", data),
  updatePageRule: (ruleId, data) => http.put(`/page-rules/${ruleId}`, data),
  deletePageRule: (ruleId, params) =>
    http.delete(`/page-rules/${ruleId}`, { params }),

  analyticsDashboard: (params) => http.get("/analytics/dashboard", { params })
};
