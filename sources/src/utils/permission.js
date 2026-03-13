const SUPER_TYPES = new Set(["super", "supper"]);
const SUPER_ROLE_CODES = new Set(["sys"]);

const STORAGE_KEYS = {
  USER_INFO: "userInfo",
  USER_INFO_FALLBACK: "user",
  BTN_COMPONENTS_PERMS: "btnConPomentsPerms",
  PERMISSIONS: "permissions"
};

function normalize(value) {
  return `${value || ""}`.trim();
}

function parseJson(raw, fallback) {
  if (!raw) {
    return fallback;
  }
  try {
    const data = JSON.parse(raw);
    return data ?? fallback;
  } catch (_e) {
    return fallback;
  }
}

function readUserInfo() {
  const userInfo = parseJson(localStorage.getItem(STORAGE_KEYS.USER_INFO), null);
  if (userInfo) {
    return userInfo;
  }
  return parseJson(localStorage.getItem(STORAGE_KEYS.USER_INFO_FALLBACK), null);
}

function appendRoleCode(codes, value) {
  const code = normalize(value).toLowerCase();
  if (code) {
    codes.add(code);
  }
}

function collectRoleCodes(source, codes) {
  if (!source) {
    return;
  }
  if (Array.isArray(source)) {
    source.forEach((item) => {
      if (typeof item === "string") {
        appendRoleCode(codes, item);
        return;
      }
      appendRoleCode(codes, item?.code || item?.roleCode || item?.value);
    });
    return;
  }
  if (typeof source === "string") {
    source
      .split(",")
      .map((item) => item.trim())
      .filter(Boolean)
      .forEach((item) => appendRoleCode(codes, item));
  }
}

function resolveRoleCodes(user) {
  const roleCodes = new Set();
  collectRoleCodes(user?.roles, roleCodes);
  collectRoleCodes(user?.roleCodes, roleCodes);
  collectRoleCodes(user?.account?.roles, roleCodes);
  collectRoleCodes(user?.account?.roleCodes, roleCodes);
  collectRoleCodes(user?.user?.roles, roleCodes);
  collectRoleCodes(user?.user?.roleCodes, roleCodes);
  return roleCodes;
}

function toCode(item) {
  if (!item) {
    return "";
  }
  if (typeof item === "string") {
    return normalize(item);
  }
  if (typeof item === "object") {
    return normalize(item.permCode || item.code || item.router || item.permission || "");
  }
  return "";
}

function readCodes(storageKey) {
  const raw = localStorage.getItem(storageKey);
  const list = parseJson(raw, []);
  if (!Array.isArray(list)) {
    return [];
  }
  return list.map(toCode).filter(Boolean);
}

function wildcardMatch(pattern, target) {
  const p = normalize(pattern);
  const t = normalize(target);
  if (!p || !t) {
    return false;
  }
  if (!p.includes("*")) {
    return p.toLowerCase() === t.toLowerCase();
  }
  const escaped = p.replace(/[.+?^${}()|[\]\\]/g, "\\$&");
  const regex = new RegExp(`^${escaped.replace(/\*/g, ".*")}$`, "i");
  return regex.test(t);
}

export function isSuperUser() {
  const user = readUserInfo();
  const accountType = normalize(user?.accountType || user?.account?.accountType).toLowerCase();
  if (SUPER_TYPES.has(accountType)) {
    return true;
  }
  const roleCodes = resolveRoleCodes(user);
  return Array.from(roleCodes).some((code) => SUPER_ROLE_CODES.has(code));
}

export function getPermissionCodes() {
  const codeSet = new Set([
    ...readCodes(STORAGE_KEYS.BTN_COMPONENTS_PERMS),
    ...readCodes(STORAGE_KEYS.PERMISSIONS)
  ]);
  return Array.from(codeSet);
}

export function hasPermission(code) {
  if (isSuperUser()) {
    return true;
  }
  const target = normalize(code);
  if (!target) {
    return true;
  }

  const codes = getPermissionCodes().map((item) => normalize(item));
  // Compatibility fallback: when permission payload has not been synced yet,
  // keep UI visible to avoid blocking normal operations.
  if (codes.length === 0) {
    return true;
  }
  const targetLower = target.toLowerCase();

  if (
    codes.some((item) => {
      const lower = item.toLowerCase();
      return item === "*" || item === "**" || lower === "all" || lower === "api:*";
    })
  ) {
    return true;
  }

  return codes.some((item) => item.toLowerCase() === targetLower || wildcardMatch(item, target));
}

export function hasAnyPermission(codes) {
  if (!Array.isArray(codes)) {
    return hasPermission(codes);
  }
  if (codes.length === 0) {
    return true;
  }
  return codes.some((code) => hasPermission(code));
}

