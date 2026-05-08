import { defineStore } from "pinia";
import router from "../router";

const useUserStore = defineStore("user", {
  state: () => ({
    token: "",
    userInfo: {},
    roleFlag: "",
  }),
  getters: {
    getUserInfo() {
      return this.userInfo;
    },
    getToken() {
      return this.token;
    },
    getRoleFlag() {
      return this.roleFlag;
    },
  },
  actions: {
    setUserInfo(userInfo) {
      this.userInfo = userInfo;
    },
    setToken(token) {
      this.token = token;
      // 与 el-upload 等直接读 localStorage 的代码兼容
      if (token) {
        try {
          localStorage.setItem("token", token);
        } catch {
          /* ignore */
        }
      }
    },
    setRoleFlag(roleFlag) {
      this.roleFlag = roleFlag;
    },
    logout() {
      this.token = "";
      this.userInfo = {};
      this.roleFlag = "";
      localStorage.removeItem("user");
      localStorage.removeItem("token");
      localStorage.removeItem("userInfo");
      localStorage.removeItem("roleFlag");
      router.push("/login");
    },
  },
  // 持久化存储
  persist: {
    key: "user", // 自定义存储键名
    storage: localStorage,
    paths: ["token", "userInfo", "roleFlag"],
    beforeRestore: (ctx) => {
      console.log("即将恢复存储", ctx);
    },
    afterRestore: (ctx) => {
      console.log("存储恢复完成", ctx);
    },
  },
});
export default useUserStore;
