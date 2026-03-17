import axios from "axios";
import Config from "@/config";
import { ElMessage } from "element-plus";
import router from "@/router";

// 创建axios实例
const http = axios.create({
  // 从 env 文件中获取 baseUrl
  baseURL: Config.baseUrl,
  timeout: 10000, // 请求超时时间
  headers: {
    "Content-Type": "application/json",
  },
});

// 请求拦截器
http.interceptors.request.use(
  (config) => {
    // 从localStorage获取token
    const token = localStorage.getItem("token");
    if (token) {
      config.headers["Authorization"] = token;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
http.interceptors.response.use(
  (response) => {
    const res = response.data;
    // 这里可以根据后端的响应结构进行调整
    if (res.code === 200) {
      return res;
    } else {
      const msg = res.description != "" ? res.description : res.msg;

      // 权限错误，跳转登录页面，清除信息
      if (res.code === 520) {
        localStorage.clear();
        window.location.href = "/login";
        return res;
      }
      // 处理其他状态码
      console.error("请求错误：", msg);
      ElMessage.error(msg);
      return res;
    }
  },
  (error) => {
    // 处理 HTTP 错误状态
    let message = "";
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = "未授权，请重新登录";
          // 可以在这里处理登录过期的逻辑
          break;
        case 403:
          message = "拒绝访问";
          break;
        case 404:
          message = "请求错误，未找到该资源";
          break;
        case 500:
          message = "服务器错误";
          break;
        default:
          message = `连接错误${error.response.status}`;
      }
    } else {
      message = error.message;
    }
    console.error(message);
    return Promise.reject(error);
  }
);

// 封装基础请求方法
export const request = {
  get(url, params) {
    return http.get(url, { params });
  },
  post(url, data) {
    return http.post(url, data);
  },
  put(url, data) {
    return http.put(url, data);
  },
  delete(url) {
    return http.delete(url);
  },
};

export default http;
