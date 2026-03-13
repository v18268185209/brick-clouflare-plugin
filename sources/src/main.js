import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import permissionDirective from "./directives/permission";
import { hasPermission } from "./utils/permission";
import { applyIncomingMicroRoute, isMicroAppMode } from "./utils/runtime";
import "./styles.css";

const app = createApp(App);
app.use(createPinia());
app.use(router);
app.directive("permission", permissionDirective);
app.config.globalProperties.$hasPermission = hasPermission;
app.mount("#app");

if (isMicroAppMode()) {
  window.onmount = (data) => {
    applyIncomingMicroRoute(router, data);
  };

  window.onunmount = () => {
    // micro-app 卸载时不需要额外清理状态。
  };

  window.microApp?.addDataListener((data) => {
    applyIncomingMicroRoute(router, data);
  });
}
