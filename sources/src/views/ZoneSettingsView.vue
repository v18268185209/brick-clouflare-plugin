<template>
  <div class="panel">
    <div class="panel-title">站点高级设置</div>
    <div class="toolbar">
      <el-button type="primary" @click="load">读取设置</el-button>
      <el-button
        v-permission="'btn:plugins_cloudflare_zone_settings:update'"
        type="success"
        @click="save"
      >
        保存设置
      </el-button>
    </div>

    <el-form :model="form" label-width="210px" style="max-width: 680px">
      <el-form-item label="高级文本压缩">
        <el-switch v-model="form.brotli" />
      </el-form-item>
      <el-form-item label="启用新一代传输协议">
        <el-switch v-model="form.http3" />
      </el-form-item>
      <el-form-item label="始终在线">
        <el-switch v-model="form.alwaysOnline" />
      </el-form-item>
      <el-form-item label="自动安全访问重写">
        <el-switch v-model="form.automaticHttpsRewrites" />
      </el-form-item>
      <el-form-item label="脚本延迟加载">
        <el-switch v-model="form.rocketLoader" />
      </el-form-item>
      <el-form-item label="安全级别">
        <el-select v-model="form.securityLevel" style="width: 220px">
          <el-option label="关闭" value="off" />
          <el-option label="极低" value="essentially_off" />
          <el-option label="低" value="low" />
          <el-option label="中" value="medium" />
          <el-option label="高" value="high" />
          <el-option label="遭受攻击" value="under_attack" />
        </el-select>
      </el-form-item>
      <el-form-item label="自动压缩">
        <el-space>
          <el-checkbox v-model="form.minify.css">样式</el-checkbox>
          <el-checkbox v-model="form.minify.js">脚本</el-checkbox>
          <el-checkbox v-model="form.minify.html">页面</el-checkbox>
        </el-space>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import { ElMessage } from "element-plus";
import { api } from "../api/cloudflare";
import { useAccountStore } from "../stores/accountStore";

const store = useAccountStore();
const form = reactive({
  brotli: false,
  http3: false,
  alwaysOnline: false,
  automaticHttpsRewrites: false,
  rocketLoader: false,
  securityLevel: "medium",
  minify: {
    css: false,
    js: false,
    html: false
  }
});

function contextReady() {
  if (!store.selectedAccountId || !store.selectedZoneId) {
    ElMessage.warning("请先选择账号和站点");
    return false;
  }
  return true;
}

async function load() {
  if (!contextReady()) return;
  const resp = await api.getZoneAdvancedSettings({
    accountId: store.selectedAccountId,
    zoneId: store.selectedZoneId
  });
  form.brotli = resp.data?.brotli?.result?.value === "on";
  form.http3 = resp.data?.http3?.result?.value === "on";
  form.alwaysOnline = resp.data?.alwaysOnline?.result?.value === "on";
  form.automaticHttpsRewrites = resp.data?.automaticHttpsRewrites?.result?.value === "on";
  form.rocketLoader = resp.data?.rocketLoader?.result?.value === "on";
  form.securityLevel = resp.data?.securityLevel?.result?.value || form.securityLevel;
  form.minify = {
    css: resp.data?.autoMinify?.result?.value?.css === "on",
    js: resp.data?.autoMinify?.result?.value?.js === "on",
    html: resp.data?.autoMinify?.result?.value?.html === "on"
  };
}

async function save() {
  if (!contextReady()) return;
  await api.patchZoneAdvancedSettings({
    accountId: store.selectedAccountId,
    payload: {
      zoneId: store.selectedZoneId,
      brotli: form.brotli ? "on" : "off",
      http3: form.http3 ? "on" : "off",
      alwaysOnline: form.alwaysOnline ? "on" : "off",
      automaticHttpsRewrites: form.automaticHttpsRewrites ? "on" : "off",
      rocketLoader: form.rocketLoader ? "on" : "off",
      securityLevel: form.securityLevel,
      autoMinify: {
        css: form.minify.css ? "on" : "off",
        js: form.minify.js ? "on" : "off",
        html: form.minify.html ? "on" : "off"
      }
    }
  });
  ElMessage.success("高级设置已保存");
}
</script>
