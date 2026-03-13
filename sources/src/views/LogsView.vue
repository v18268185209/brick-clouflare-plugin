<template>
  <div class="panel">
    <div class="panel-title">操作日志</div>
    <div class="toolbar">
      <el-input-number v-model="limit" :min="10" :max="500" />
      <el-button type="primary" @click="load">刷新</el-button>
      <el-button
        v-permission="'btn:plugins_cloudflare_logs:clear'"
        type="danger"
        @click="clear"
      >
        清空日志
      </el-button>
    </div>

    <el-table :data="logs" stripe>
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="accountId" label="账号编号" width="100" />
      <el-table-column prop="module" label="模块" width="120" />
      <el-table-column prop="action" label="动作" width="160" />
      <el-table-column prop="responseCode" label="状态码" width="100" />
      <el-table-column prop="message" label="结果摘要" min-width="260" />
      <el-table-column prop="createdAt" label="时间" width="180" />
    </el-table>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { api } from "../api/cloudflare";

const limit = ref(60);
const logs = ref([]);

async function load() {
  const resp = await api.logs(limit.value);
  logs.value = resp.data || [];
}

async function clear() {
  await api.clearLogs();
  ElMessage.success("日志已清空");
  await load();
}

load();
</script>
