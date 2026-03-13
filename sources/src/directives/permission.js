import { hasAnyPermission } from "../utils/permission";

function updateVisibility(el, binding) {
  const required = binding?.value;
  const allow = hasAnyPermission(required);
  if (allow) {
    el.style.display = el.__originDisplay || "";
    return;
  }
  if (el.__originDisplay === undefined) {
    el.__originDisplay = el.style.display;
  }
  el.style.display = "none";
}

export default {
  mounted(el, binding) {
    updateVisibility(el, binding);
  },
  updated(el, binding) {
    updateVisibility(el, binding);
  }
};

