<template>
  <div class="panel">
    <div class="panel-title">分析报表</div>
    <div class="toolbar">
      <el-date-picker
        v-model="range"
        type="daterange"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
      />
      <el-button type="primary" @click="load">查询</el-button>
    </div>

    <el-alert v-if="!store.selectedZoneId" type="warning" :closable="false">
      <template #default>请先选择站点后再查询分析数据。</template>
    </el-alert>

    <el-descriptions v-else :column="2" border>
      <el-descriptions-item label="总请求量">
        {{ totals.requests?.all || "-" }}
      </el-descriptions-item>
      <el-descriptions-item label="总带宽">
        {{ totals.bandwidth?.all || "-" }}
      </el-descriptions-item>
      <el-descriptions-item label="总访问量">
        {{ totals.visits?.all || "-" }}
      </el-descriptions-item>
      <el-descriptions-item label="页面浏览量">
        {{ totals.pageviews?.all || "-" }}
      </el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { api } from "../api/cloudflare";
import { useAccountStore } from "../stores/accountStore";

const store = useAccountStore();
const range = ref([]);
const totals = reactive({
  requests: {},
  bandwidth: {},
  visits: {},
  pageviews: {}
});

async function load() {
  if (!store.selectedAccountId || !store.selectedZoneId) {
    ElMessage.warning("请先选择账号和站点");
    return;
  }
  const resp = await api.analyticsDashboard({
    accountId: store.selectedAccountId,
    zoneId: store.selectedZoneId,
    since: range.value?.[0] || undefined,
    until: range.value?.[1] || undefined
  });
  const result = resp.data?.result || {};
  totals.requests = result.totals?.requests || {};
  totals.bandwidth = result.totals?.bandwidth || {};
  totals.visits = result.totals?.visits || {};
  totals.pageviews = result.totals?.pageviews || {};
}
</script>
