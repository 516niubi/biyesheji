/**
 * 读取登录 token：兼容 Pinia 持久化键 `user`（JSON）与历史写法独立键 `token`
 */
export function getStoredToken() {
  const direct = localStorage.getItem("token");
  if (direct) return direct;
  try {
    const raw = localStorage.getItem("user");
    if (!raw) return "";
    const o = JSON.parse(raw);
    return typeof o?.token === "string" ? o.token : "";
  } catch {
    return "";
  }
}
