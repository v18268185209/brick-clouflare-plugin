<template>
  <div class="panel">
    <div class="panel-title">证书与加密</div>
    <div class="toolbar">
      <el-button type="primary" @click="load">读取设置</el-button>
      <el-button
        v-permission="'btn:plugins_cloudflare_ssl:update'"
        type="success"
        @click="save"
      >
        保存设置
      </el-button>
    </div>

    <el-form :model="form" label-width="170px" style="max-width: 560px">
      <el-form-item label="证书模式">
        <el-select v-model="form.ssl">
          <el-option label="关闭" value="off" />
          <el-option label="灵活" value="flexible" />
          <el-option label="完整" value="full" />
          <el-option label="严格" value="strict" />
        </el-select>
      </el-form-item>
      <el-form-item label="始终启用安全访问">
        <el-switch v-model="form.alwaysUseHttps" />
      </el-form-item>
      <el-form-item label="最低加密协议版本">
        <el-select v-model="form.minTlsVersion">
          <el-option label="1.0" value="1.0" />
          <el-option label="1.1" value="1.1" />
          <el-option label="1.2" value="1.2" />
          <el-option label="1.3" value="1.3" />
        </el-select>
      </el-form-item>
      <el-form-item label="启用新版加密协议">
        <el-switch v-model="form.tls13" />
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
  ssl: "full",
  alwaysUseHttps: false,
  minTlsVersion: "1.2",
  tls13: true
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
  const resp = await api.getSslSettings({
    accountId: store.selectedAccountId,
    zoneId: store.selectedZoneId
  });
  form.ssl = resp.data?.ssl?.result?.value || form.ssl;
  form.alwaysUseHttps = resp.data?.alwaysUseHttps?.result?.value === "on";
  form.minTlsVersion = resp.data?.minTlsVersion?.result?.value || form.minTlsVersion;
  form.tls13 = resp.data?.tls13?.result?.value === "on";
}

async function save() {
  if (!contextReady()) return;
  await api.patchSslSettings({
    accountId: store.selectedAccountId,
    payload: {
      zoneId: store.selectedZoneId,
      ssl: form.ssl,
      alwaysUseHttps: form.alwaysUseHttps ? "on" : "off",
      minTlsVersion: form.minTlsVersion,
      tls13: form.tls13 ? "on" : "off"
    }
  });
  ElMessage.success("证书设置已保存");
}
</script>
