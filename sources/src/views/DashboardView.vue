<template>
  <div>
    <div class="grid-4">
      <div class="kpi">
        <h4>已接入账号</h4>
        <strong>{{ metrics.accountCount }}</strong>
      </div>
      <div class="kpi">
        <h4>当前账号</h4>
        <strong style="font-size: 20px">{{ store.selectedAccount?.displayName || "-" }}</strong>
      </div>
      <div class="kpi">
        <h4>当前站点</h4>
        <strong style="font-size: 20px">{{ store.selectedZone?.name || "-" }}</strong>
      </div>
      <div class="kpi">
        <h4>最近操作数</h4>
        <strong>{{ logs.length }}</strong>
      </div>
    </div>

    <div class="row-2" style="margin-top: 14px">
      <div class="panel">
        <div class="panel-title">操作成功趋势</div>
        <div ref="chartRef" style="height: 320px"></div>
      </div>
      <div class="panel">
        <div class="panel-title">最近操作流</div>
        <el-timeline>
          <el-timeline-item
            v-for="log in logs.slice(0, 10)"
            :key="log.id"
            :timestamp="log.createdAt"
            :type="log.success === 1 ? 'success' : 'danger'"
          >
            <strong>[{{ log.module }}]</strong> {{ log.action }}
            <div class="muted">状态码 {{ log.responseCode }}，{{ log.message || "-" }}</div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { LineChart } from "echarts/charts";
import { GridComponent, LegendComponent, TooltipComponent } from "echarts/components";
import { init, use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { api } from "../api/cloudflare";
import { useAccountStore } from "../stores/accountStore";

use([LineChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer]);

const store = useAccountStore();
const chartRef = ref();
let chart;

const metrics = ref({
  accountCount: 0
});
const logs = ref([]);

async function loadOverview() {
  const resp = await api.overview(store.selectedAccountId || undefined);
  const data = resp.data || {};
  metrics.value.accountCount = data.accountCount || 0;
  logs.value = data.latestLogs || [];
  await nextTick();
  renderChart();
}

function renderChart() {
  if (!chartRef.value) return;
  if (!chart) {
    chart = init(chartRef.value);
  }

  const points = logs.value.slice(0, 20).reverse();
  const xAxis = points.map((item) => item.createdAt?.slice(11, 19) || "-");
  const successSeries = points.map((item) => (item.success === 1 ? 1 : 0));
  const errorSeries = points.map((item) => (item.responseCode >= 400 ? 1 : 0));

  chart.setOption({
    color: ["#0f8a63", "#c76a1a"],
    tooltip: { trigger: "axis" },
    legend: { data: ["成功", "异常"] },
    xAxis: { type: "category", data: xAxis },
    yAxis: { type: "value", min: 0, max: 1 },
    series: [
      {
        name: "成功",
        type: "line",
        smooth: true,
        areaStyle: { opacity: 0.18 },
        data: successSeries
      },
      {
        name: "异常",
        type: "line",
        smooth: true,
        areaStyle: { opacity: 0.12 },
        data: errorSeries
      }
    ]
  });
}

function handleResize() {
  chart?.resize();
}

watch(
  () => store.selectedAccountId,
  async () => {
    await loadOverview();
  }
);

onMounted(async () => {
  window.addEventListener("resize", handleResize);
  await loadOverview();
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize);
  if (chart) {
    chart.dispose();
    chart = null;
  }
});
</script>
