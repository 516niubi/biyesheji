import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import "normalize.css";
import "./styles/index.scss";
// import "./styles/element-plus/index.scss";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import * as ElementPlusIconsVue from "@element-plus/icons-vue";
import store from "./stores/index";

// import Vue3VideoPlay from "vue3-video-play";
// import "vue3-video-play/dist/style.css"; // 引入css

const app = createApp(App);
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}
// app.use(Vue3VideoPlay);
app.use(router);
app.use(ElementPlus, {
  locale: zhCn,
});
app.use(store);
app.mount("#app");
