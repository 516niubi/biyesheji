import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import * as path from "path";
// Element Plus按需导入
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import { ElementPlusResolver } from "unplugin-vue-components/resolvers";

export default defineConfig({
  server: {
    port: 8088,
    host: "0.0.0.0",
    proxy: {
      "^/api": {
        // 代理地址
        target: "http://localhost:8080/api",
        // 是否改变源
        changeOrigin: true,
        // 重写路径
        rewrite: (path) => path.replace(/^\/api/, ""),
      },
    },
  },
  css: {
    //  css预处理
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@/styles/element-plus/index.scss" as *;`,
      },
    },
  },
  resolve: {
    // 设置路径别名
    alias: {
      "@": path.join(__dirname, "./src"),
    },
  },
  plugins: [
    vue(),
    AutoImport({
      resolvers: [
        ElementPlusResolver({
          importStyle: "sass",
        }),
      ],
    }),
    Components({
      resolvers: [
        // 配置Element Plus采用saas样式配色系统
        ElementPlusResolver({ importStyle: "sass" }),
      ],
    }),
  ],
});
