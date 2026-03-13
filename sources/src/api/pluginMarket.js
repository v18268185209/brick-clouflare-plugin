import http from "./http";

export const pluginMarketApi = {
  health: () => http.get("/plugin-market-admin/health"),
  listPlugins: (params) => http.get("/plugin-market-admin/plugins", { params }),
  listReleases: (pluginCode) =>
    http.get("/plugin-market-admin/releases", { params: { pluginCode } }),
  savePlugin: (data) => http.post("/plugin-market-admin/plugin/save", data),
  saveRelease: (data) => http.post("/plugin-market-admin/release/save", data),
  publishPlugin: (pluginCode) =>
    http.post("/plugin-market-admin/plugin/publish", { pluginCode }),
  offlinePlugin: (pluginCode) =>
    http.post("/plugin-market-admin/plugin/offline", { pluginCode }),
  draftPlugin: (pluginCode) =>
    http.post("/plugin-market-admin/plugin/draft", { pluginCode }),
  deletePlugin: (pluginCode) =>
    http.post("/plugin-market-admin/plugin/delete", { pluginCode }),
  listOrders: (params) => http.get("/plugin-market-admin/orders", { params }),
  listOperationLogs: (params) =>
    http.get("/plugin-market-admin/operation-logs", { params })
};
