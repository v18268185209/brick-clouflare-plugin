import { createRouter, createWebHashHistory } from "vue-router";
import MainLayout from "../views/MainLayout.vue";
import { menuItems } from "./menus";

const DashboardView = () => import("../views/DashboardView.vue");
const AccountsView = () => import("../views/AccountsView.vue");
const DnsView = () => import("../views/DnsView.vue");
const R2View = () => import("../views/R2View.vue");
const FirewallView = () => import("../views/FirewallView.vue");
const CacheView = () => import("../views/CacheView.vue");
const SslView = () => import("../views/SslView.vue");
const WorkersView = () => import("../views/WorkersView.vue");
const KvView = () => import("../views/KvView.vue");
const AnalyticsView = () => import("../views/AnalyticsView.vue");
const PageRulesView = () => import("../views/PageRulesView.vue");
const LogsView = () => import("../views/LogsView.vue");
const ZoneSettingsView = () => import("../views/ZoneSettingsView.vue");
const RulesetsView = () => import("../views/RulesetsView.vue");
const PluginMarketView = () => import("../views/PluginMarketView.vue");
const WorkerAdminView = () => import("../views/WorkerAdminView.vue");

const routeComponents = {
  "/overview": DashboardView,
  "/accounts": AccountsView,
  "/dns": DnsView,
  "/r2": R2View,
  "/firewall": FirewallView,
  "/rulesets": RulesetsView,
  "/cache": CacheView,
  "/ssl": SslView,
  "/zone-settings": ZoneSettingsView,
  "/workers": WorkersView,
  "/kv": KvView,
  "/analytics": AnalyticsView,
  "/page-rules": PageRulesView,
  "/logs": LogsView,
  "/plugin-market": PluginMarketView,
  "/worker-admin": WorkerAdminView
};

const childRoutes = menuItems.map((item) => ({
  path: item.path.slice(1),
  component: routeComponents[item.path],
  meta: {
    title: item.title,
    menuCode: item.menuCode,
    hostRoute: item.fullPath
  }
}));

const routes = [
  {
    path: "/",
    component: MainLayout,
    children: [
      { path: "", redirect: "/overview" },
      ...childRoutes
    ]
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes
});

export default router;
