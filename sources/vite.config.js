import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import { ElementPlusResolver } from "unplugin-vue-components/resolvers";
import { resolve } from "node:path";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const proxyTarget = env.VITE_PROXY_TARGET || "http://127.0.0.1:8080";

  return {
    resolve: {
      alias: {
        "@": resolve(__dirname, "src")
      }
    },
    plugins: [
      vue(),
      AutoImport({
        imports: ["vue", "vue-router"],
        resolvers: [ElementPlusResolver()]
      }),
      Components({
        resolvers: [ElementPlusResolver({ importStyle: "css" })]
      })
    ],
    publicDir: "static",
    base: "./",
    build: {
      outDir: "dist",
      emptyOutDir: true,
      rollupOptions: {
        output: {
          manualChunks(id) {
            if (id.includes("node_modules/echarts")) {
              return "vendor-echarts";
            }
            if (
              id.includes("node_modules/vue") ||
              id.includes("node_modules/pinia") ||
              id.includes("node_modules/vue-router")
            ) {
              return "vendor-vue";
            }
            if (id.includes("node_modules/axios")) {
              return "vendor-axios";
            }
          }
        }
      }
    },
    server: {
      host: "0.0.0.0",
      port: 30001,
      strictPort: true,
      proxy: {
        "/cloudflare": {
          target: proxyTarget,
          changeOrigin: true
        },
        "/plugins/eqadminPluginsCloudflare": {
          target: proxyTarget,
          changeOrigin: true
        }
      }
    }
  };
});
