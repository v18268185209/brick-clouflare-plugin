import { HOST_ROUTE_PREFIX } from "../router/menus";

export function isMicroAppMode() {
  return Boolean(window.__MICRO_APP_ENVIRONMENT__);
}

export function resolveApiBase() {
  if (import.meta.env.VITE_API_BASE) {
    return import.meta.env.VITE_API_BASE;
  }
  return isMicroAppMode()
    ? "/plugins/eqadminPluginsCloudflare/cloudflare"
    : "/cloudflare";
}

export function normalizeIncomingRoute(raw) {
  if (!raw) {
    return "/overview";
  }

  let value = String(raw).trim();
  if (!value) {
    return "/overview";
  }

  const hashIndex = value.indexOf("#");
  if (hashIndex >= 0) {
    value = value.slice(hashIndex + 1);
  }

  if (value.startsWith(HOST_ROUTE_PREFIX)) {
    value = value.slice(HOST_ROUTE_PREFIX.length) || "/overview";
  }

  if (value.startsWith("/childrens/cloudflare")) {
    value = "/overview";
  }

  if (!value.startsWith("/")) {
    value = `/${value}`;
  }

  return value === "/" ? "/overview" : value;
}

export function applyIncomingMicroRoute(router, payload) {
  const target = normalizeIncomingRoute(
    payload?.component || payload?.path || payload?.router || payload
  );
  if (target && router.currentRoute.value.path !== target) {
    router.push(target).catch(() => {});
  }
}
