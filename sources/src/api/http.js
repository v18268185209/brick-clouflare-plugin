import axios from "axios";
import { ElMessage } from "element-plus";
import { resolveApiBase } from "../utils/runtime";

const http = axios.create({
  baseURL: resolveApiBase(),
  timeout: 20000
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem("token") || localStorage.getItem("adminToken");
  if (token && !config.headers?.headerToken) {
    config.headers = config.headers || {};
    config.headers.headerToken = token;
  }
  return config;
});

http.interceptors.response.use(
  (resp) => {
    const payload = resp.data;
    if (payload && payload.success === false) {
      const message = payload.message || "请求失败";
      ElMessage.error(message);
      return Promise.reject(new Error(message));
    }
    return payload;
  },
  (err) => {
    const message =
      err?.response?.data?.message ||
      err?.response?.data?.error ||
      err?.message ||
      "请求失败";
    ElMessage.error(message);
    return Promise.reject(err);
  }
);

export default http;
