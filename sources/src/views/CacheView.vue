<template>
  <div class="panel">
    <div class="panel-title">缓存策略</div>
    <div class="toolbar">
      <el-button type="primary" @click="load">读取设置</el-button>
      <el-button
        v-permission="'btn:plugins_cloudflare_cache:update'"
        type="success"
        @click="save"
      >
        保存设置
      </el-button>
      <el-button
        v-permission="'btn:plugins_cloudflare_cache:purge'"
        type="danger"
        @click="purgeAll"
      >
        清空全部缓存
      </el-button>
    </div>

    <el-form :model="form" label-width="170px" style="max-width: 560px">
      <el-form-item label="缓存级别">
        <el-select v-model="form.cacheLevel">
          <el-option label="跳过缓存" value="bypass" />
          <el-option label="基础缓存" value="basic" />
          <el-option label="简化缓存" value="simplified" />
          <el-option label="激进缓存" value="aggressive" />
        </el-select>
      </el-form-item>
      <el-form-item label="浏览器缓存时长（秒）">
        <el-input-number v-model="form.browserCacheTtl" :min="0" />
      </el-form-item>
      <el-form-item label="开发模式">
        <el-switch v-model="form.developmentMode" />
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
  cacheLevel: "basic",
  browserCacheTtl: 14400,
  developmentMode: false
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
  const resp = await api.getCacheSettings({
    accountId: store.selectedAccountId,
    zoneId: store.selectedZoneId
  });
  form.cacheLevel = resp.data?.cacheLevel?.result?.value || form.cacheLevel;
  form.browserCacheTtl = Number(
    resp.data?.browserCacheTtl?.result?.value || form.browserCacheTtl
  );
  form.developmentMode = resp.data?.developmentMode?.result?.value === "on";
}

async function save() {
  if (!contextReady()) return;
  await api.patchCacheSettings({
    accountId: store.selectedAccountId,
    payload: {
      zoneId: store.selectedZoneId,
      cacheLevel: form.cacheLevel,
      browserCacheTtl: form.browserCacheTtl,
      developmentMode: form.developmentMode ? "on" : "off"
    }
  });
  ElMessage.success("缓存策略已保存");
}

async function purgeAll() {
  if (!contextReady()) return;
  await api.purgeCache({
    accountId: store.selectedAccountId,
    payload: {
      zoneId: store.selectedZoneId,
      purge_everything: true
    }
  });
  ElMessage.success("缓存清理已完成");
}
</script>
