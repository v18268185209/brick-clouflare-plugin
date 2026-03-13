import http from "./http";

export const workerAdminApi = {
  me: () => http.get("/worker-admin/me"),
  getPlanConfig: () => http.get("/worker-admin/plan-config"),
  savePlanConfig: (data) => http.put("/worker-admin/plan-config", data),

  getPaymentConfig: () => http.get("/worker-admin/payment-config"),
  savePaymentConfig: (data) => http.put("/worker-admin/payment-config", data),

  getEmailConfig: () => http.get("/worker-admin/email-config"),
  saveEmailConfig: (data) => http.put("/worker-admin/email-config", data),
  testEmail: (data) => http.post("/worker-admin/email-config/test", data),

  getMenuCodes: () => http.get("/worker-admin/menu-codes"),
  saveMenuCodes: (data) => http.put("/worker-admin/menu-codes", data),

  getOtherConfig: () => http.get("/worker-admin/other-config"),
  saveOtherConfig: (data) => http.put("/worker-admin/other-config", data),
  changePassword: (data) => http.post("/worker-admin/password/change", data),

  listConfigs: () => http.get("/worker-admin/config/list"),
  listLicenses: (params) => http.get("/worker-admin/licenses", { params }),
  updateLicenseDuration: (data) =>
    http.post("/worker-admin/licenses/update-duration", data),
  updateLicenseExpire: (data) =>
    http.post("/worker-admin/licenses/update-expire", data),
  adjustLicenseExpire: (data) =>
    http.post("/worker-admin/licenses/adjust-expire", data),
  deleteLicense: (data) => http.post("/worker-admin/licenses/delete", data)
};
