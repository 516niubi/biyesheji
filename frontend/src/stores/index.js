import { createPinia } from "pinia"; //引入pinia
import piniaPluginPersistedstate from "pinia-plugin-persistedstate";

const store = createPinia(); //创建pinia实例
store.use(piniaPluginPersistedstate);

export default store; //导出pinia用于main.js注册
