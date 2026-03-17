import { defineStore } from "pinia";
import router from "../router";

const useUserStore = defineStore("user", {
  state: () => ({
    token: localStorage.getItem("token") ?? "",
    userInfo: localStorage.getItem("userInfo") ?? {},
    roleFlag: localStorage.getItem("roleFlag") ?? "",
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
    },
    setRoleFlag(roleFlag) {
      this.roleFlag = roleFlag;
    },
    logout() {
      localStorage.clear();
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
