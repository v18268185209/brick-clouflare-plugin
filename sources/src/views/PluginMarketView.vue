<template>
  <div class="panel">
    <div class="panel-title">插件市场</div>
    <div class="toolbar">
      <el-tag :type="healthReady ? 'success' : 'danger'" effect="plain">
        {{ healthReady ? "服务已连通" : "服务未就绪" }}
      </el-tag>
      <span class="muted">服务地址：{{ marketBaseUrl || "-" }}</span>
      <el-button @click="refreshAll">刷新全部</el-button>
    </div>
    <el-alert
      v-if="serviceMessage"
      class="status-alert"
      type="warning"
      :closable="false"
      show-icon
      :title="serviceMessage"
    />
  </div>

  <div class="row-2">
    <div class="panel">
      <div class="panel-title">插件列表</div>
      <div class="toolbar">
        <el-input
          v-model="pluginQuery.keyword"
          placeholder="按插件编码、名称或描述搜索"
          clearable
          style="width: 240px"
          @keyup.enter="onPluginSearch"
        />
        <el-select v-model="pluginQuery.status" clearable placeholder="按状态筛选" style="width: 140px">
          <el-option label="草稿" value="draft" />
          <el-option label="已发布" value="published" />
          <el-option label="已下线" value="offline" />
        </el-select>
        <el-button @click="onPluginSearch">查询插件</el-button>
      </div>

      <el-table
        :data="pluginRows"
        stripe
        v-loading="pluginLoading"
        style="width: 100%"
        @row-click="selectPlugin"
      >
        <el-table-column prop="pluginCode" label="插件编码" min-width="150" />
        <el-table-column prop="pluginName" label="插件名称" min-width="180" />
        <el-table-column prop="status" label="发布状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="light">{{ pluginStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isPaid" label="计费模式" width="90">
          <template #default="{ row }">
            <el-tag :type="Number(row.isPaid) === 1 ? 'warning' : 'success'" effect="plain">
              {{ Number(row.isPaid) === 1 ? "付费版" : "免费版" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="latestVersion" label="最新版本" width="100" />
        <el-table-column prop="priceCents" label="售价（分）" width="110" />
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button
              v-permission="'btn:plugins_cloudflare_plugin_market:pluginpublish'"
              link
              type="success"
              @click.stop="changePluginStatus('publish', row.pluginCode)"
            >
              发布
            </el-button>
            <el-button
              v-permission="'btn:plugins_cloudflare_plugin_market:pluginoffline'"
              link
              type="warning"
              @click.stop="changePluginStatus('offline', row.pluginCode)"
            >
              下线
            </el-button>
            <el-button
              v-permission="'btn:plugins_cloudflare_plugin_market:plugindraft'"
              link
              @click.stop="changePluginStatus('draft', row.pluginCode)"
            >
              转草稿
            </el-button>
            <el-button
              v-permission="'btn:plugins_cloudflare_plugin_market:plugindelete'"
              link
              type="danger"
              @click.stop="deletePlugin(row.pluginCode)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="table-pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="pluginTotal"
        :current-page="pluginQuery.page"
        :page-size="pluginQuery.pageSize"
        :page-sizes="[20, 50, 100]"
        @current-change="onPluginPageChange"
        @size-change="onPluginSizeChange"
      />
    </div>

    <div class="panel">
      <div class="panel-title">插件编辑器</div>
      <div class="toolbar">
        <el-button @click="createPlugin">新建插件</el-button>
        <el-button
          v-permission="'btn:plugins_cloudflare_plugin_market:pluginsave'"
          type="primary"
          @click="savePlugin"
          :loading="savingPlugin"
        >
          保存插件
        </el-button>
      </div>

      <el-form :model="pluginForm" label-width="120px">
        <el-form-item label="插件编码">
          <el-input v-model="pluginForm.pluginCode" placeholder="请输入唯一插件编码" />
        </el-form-item>
        <el-form-item label="插件名称">
          <el-input v-model="pluginForm.pluginName" />
        </el-form-item>
        <el-form-item label="发布状态">
          <el-select v-model="pluginForm.status" style="width: 160px">
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
            <el-option label="已下线" value="offline" />
          </el-select>
        </el-form-item>
        <el-form-item label="计费模式">
          <el-space>
            <el-switch v-model="pluginPaidFlag" active-text="付费版" inactive-text="免费版" />
            <el-input-number v-model="pluginForm.priceCents" :min="0" :step="100" />
          </el-space>
        </el-form-item>
        <el-form-item label="分类 / 供应商">
          <el-space style="width: 100%">
            <el-input v-model="pluginForm.category" placeholder="分类" />
            <el-input v-model="pluginForm.vendor" placeholder="供应商" />
          </el-space>
        </el-form-item>
        <el-form-item label="最新版本 / 排序值">
          <el-space style="width: 100%">
            <el-input v-model="pluginForm.latestVersion" placeholder="例如：1.0.0" />
            <el-input-number v-model="pluginForm.sort" :min="0" :step="1" />
          </el-space>
        </el-form-item>
        <el-form-item label="来源信息">
          <el-space style="width: 100%">
            <el-input v-model="pluginForm.sourceType" placeholder="请输入来源类型" />
            <el-input v-model="pluginForm.sourceRef" placeholder="请输入来源标识" />
          </el-space>
        </el-form-item>
        <el-form-item label="图标 / 币种">
          <el-space style="width: 100%">
            <el-input v-model="pluginForm.iconUrl" placeholder="请输入图标地址" />
            <el-input v-model="pluginForm.currency" placeholder="请输入币种编码" />
          </el-space>
        </el-form-item>
        <el-form-item label="插件描述">
          <el-input v-model="pluginForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="标签配置">
          <el-input v-model="pluginForm.tagsJson" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="清单配置">
          <el-input v-model="pluginForm.manifestJson" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="详情内容">
          <el-input v-model="pluginForm.detailHtml" type="textarea" :rows="5" />
        </el-form-item>
      </el-form>
    </div>
  </div>

  <div class="row-2">
    <div class="panel">
      <div class="panel-title">版本维护</div>
      <div class="toolbar">
        <el-tag effect="plain" type="info">当前插件：{{ currentPluginCode || "-" }}</el-tag>
        <el-button @click="loadReleases" :disabled="!currentPluginCode">刷新版本</el-button>
        <el-button
          v-permission="'btn:plugins_cloudflare_plugin_market:releasesave'"
          type="primary"
          @click="saveRelease"
          :disabled="!currentPluginCode"
          :loading="savingRelease"
        >
          保存版本
        </el-button>
      </div>
      <el-form :model="releaseForm" label-width="120px">
        <el-form-item label="插件编码">
          <el-input v-model="releaseForm.pluginCode" :disabled="true" />
        </el-form-item>
        <el-form-item label="版本号">
          <el-input v-model="releaseForm.version" placeholder="1.0.0" />
        </el-form-item>
        <el-form-item label="版本状态">
          <el-select v-model="releaseForm.status" style="width: 160px">
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
            <el-option label="已下线" value="offline" />
          </el-select>
        </el-form-item>
        <el-form-item label="发布时间戳">
          <el-input-number v-model="releaseForm.publishedAt" :min="0" :step="1000" style="width: 220px" />
        </el-form-item>
          <el-form-item label="来源信息">
          <el-space style="width: 100%">
            <el-input v-model="releaseForm.sourceType" placeholder="请输入来源类型" />
            <el-input v-model="releaseForm.sourceRef" placeholder="请输入来源标识" />
          </el-space>
        </el-form-item>
        <el-form-item label="下载地址">
          <el-input v-model="releaseForm.downloadUrl" />
        </el-form-item>
        <el-form-item label="校验摘要">
          <el-input v-model="releaseForm.checksumSha256" />
        </el-form-item>
        <el-form-item label="签名信息">
          <el-input v-model="releaseForm.signature" placeholder="选填" />
        </el-form-item>
        <el-form-item label="清单配置">
          <el-input v-model="releaseForm.manifestJson" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="更新说明">
          <el-input v-model="releaseForm.releaseNotes" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <el-table :data="releaseRows" stripe style="margin-top: 12px" @row-click="selectRelease">
        <el-table-column prop="version" label="版本号" width="120" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            {{ pluginStatusLabel(row.status) }}
          </template>
        </el-table-column>
        <el-table-column prop="publishedAt" label="发布时间" min-width="140" />
        <el-table-column prop="sourceType" label="来源类型" width="120" />
        <el-table-column prop="downloadUrl" label="下载地址" min-width="200" />
      </el-table>
    </div>

    <div class="panel">
      <div class="panel-title">订单查询</div>
      <div class="toolbar">
        <el-input
          v-model="orderQuery.keyword"
          placeholder="按订单号、插件编码或机器码搜索"
          style="width: 240px"
          clearable
          @keyup.enter="onOrderSearch"
        />
        <el-select v-model="orderQuery.status" clearable placeholder="按状态筛选" style="width: 180px">
          <el-option label="已创建" value="CREATED" />
          <el-option label="已生成付款码" value="QR_CREATED" />
          <el-option label="支付成功" value="TRADE_SUCCESS" />
          <el-option label="交易完成" value="TRADE_FINISHED" />
          <el-option label="交易关闭" value="TRADE_CLOSED" />
          <el-option label="预下单失败" value="PRECREATE_FAILED" />
        </el-select>
        <el-button
          v-permission="'btn:plugins_cloudflare_plugin_market:orderview'"
          @click="onOrderSearch"
        >
          查询订单
        </el-button>
      </div>

      <el-table :data="orderRows" stripe v-loading="orderLoading">
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="pluginCode" label="插件编码" min-width="130" />
        <el-table-column prop="version" label="版本" width="90" />
        <el-table-column prop="machineCode" label="机器码" min-width="170" />
        <el-table-column prop="status" label="订单状态" width="130">
          <template #default="{ row }">
            {{ orderStatusLabel(row.status) }}
          </template>
        </el-table-column>
        <el-table-column prop="amountYuan" label="支付金额" width="90" />
      </el-table>
      <el-pagination
        class="table-pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="orderTotal"
        :current-page="orderQuery.page"
        :page-size="orderQuery.pageSize"
        :page-sizes="[20, 50, 100]"
        @current-change="onOrderPageChange"
        @size-change="onOrderSizeChange"
      />
    </div>
  </div>

  <div class="panel">
    <div class="panel-title">操作日志</div>
    <div class="toolbar">
      <el-button @click="loadOperationLogs">刷新日志</el-button>
    </div>
    <el-table :data="operationLogs" stripe>
      <el-table-column prop="createdAt" label="操作时间" width="180" />
      <el-table-column prop="operatorName" label="操作人" width="140" />
      <el-table-column prop="action" label="行为" width="170">
        <template #default="{ row }">
          {{ operationActionLabel(row.action) }}
        </template>
      </el-table-column>
      <el-table-column prop="target" label="目标" min-width="160" />
      <el-table-column prop="success" label="结果" width="90">
        <template #default="{ row }">
          <el-tag :type="Number(row.success) === 1 ? 'success' : 'danger'" effect="plain">
            {{ Number(row.success) === 1 ? "成功" : "失败" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="message" label="说明" min-width="220" />
    </el-table>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { pluginMarketApi } from "../api/pluginMarket";

const pluginLoading = ref(false);
const orderLoading = ref(false);
const savingPlugin = ref(false);
const savingRelease = ref(false);
const healthReady = ref(false);
const marketBaseUrl = ref("");
const serviceMessage = ref("");

const pluginStatusTextMap = {
  draft: "草稿",
  published: "已发布",
  offline: "已下线"
};

const orderStatusTextMap = {
  CREATED: "已创建",
  QR_CREATED: "已生成付款码",
  TRADE_SUCCESS: "支付成功",
  TRADE_FINISHED: "交易完成",
  TRADE_CLOSED: "交易关闭",
  PRECREATE_FAILED: "预下单失败"
};

const operationActionTextMap = {
  "plugin.save": "保存插件",
  "release.save": "保存版本",
  "plugin.publish": "发布插件",
  "plugin.offline": "下线插件",
  "plugin.draft": "转为草稿",
  "plugin.delete": "删除插件"
};

const pluginRows = ref([]);
const releaseRows = ref([]);
const orderRows = ref([]);
const operationLogs = ref([]);
const pluginTotal = ref(0);
const orderTotal = ref(0);

const currentPluginCode = ref("");

const pluginQuery = reactive({
  keyword: "",
  status: "",
  page: 1,
  pageSize: 20
});

const orderQuery = reactive({
  keyword: "",
  status: "",
  page: 1,
  pageSize: 20
});

const pluginForm = reactive({
  pluginCode: "",
  pluginName: "",
  description: "",
  sourceType: "",
  sourceRef: "",
  iconUrl: "",
  vendor: "",
  category: "",
  tagsJson: "[]",
  manifestJson: "{}",
  detailHtml: "",
  detailJson: "{}",
  isPaid: 0,
  priceCents: 0,
  currency: "CNY",
  status: "draft",
  minCoreVersion: "",
  maxCoreVersion: "",
  latestVersion: "",
  sort: 100
});

const releaseForm = reactive({
  pluginCode: "",
  version: "",
  changelog: "",
  releaseNotes: "",
  sourceType: "",
  sourceRef: "",
  downloadUrl: "",
  checksumSha256: "",
  signature: "",
  manifestJson: "{}",
  status: "draft",
  publishedAt: 0
});

const pluginPaidFlag = computed({
  get: () => Number(pluginForm.isPaid) === 1,
  set: (value) => {
    pluginForm.isPaid = value ? 1 : 0;
  }
});

function statusTagType(status) {
  const value = String(status || "").toLowerCase();
  if (value === "published") {
    return "success";
  }
  if (value === "offline") {
    return "warning";
  }
  return "info";
}

function pluginStatusLabel(status) {
  const value = String(status || "").toLowerCase();
  return pluginStatusTextMap[value] || "未知状态";
}

function orderStatusLabel(status) {
  const value = String(status || "").toUpperCase();
  return orderStatusTextMap[value] || "未知状态";
}

function operationActionLabel(action) {
  const value = String(action || "").trim();
  return operationActionTextMap[value] || value || "-";
}

function normalizeServiceMessage(error) {
  const raw = String(error?.message || error || "").trim();
  if (!raw) {
    return "插件市场外部服务暂不可用，请检查服务地址和认证参数。";
  }
  if (raw.includes("baseUrl is not configured")) {
    return "插件市场外部服务未配置，请先设置市场服务地址。";
  }
  if (raw.includes("signed api credentials") || raw.includes("admin credentials")) {
    return "插件市场外部服务缺少认证参数，请配置签名密钥或管理员账号密码。";
  }
  return raw;
}

function handleServiceFailure(error) {
  serviceMessage.value = normalizeServiceMessage(error);
  healthReady.value = false;
}

function ensureJsonText(raw, fallback) {
  const text = String(raw || "").trim();
  if (!text) {
    return fallback;
  }
  try {
    JSON.parse(text);
    return text;
  } catch (_error) {
    throw new Error("检测到不合法的 JSON 内容");
  }
}

function toPrettyJsonText(raw, fallback) {
  if (raw === null || raw === undefined || raw === "") {
    return fallback;
  }
  if (typeof raw === "string") {
    return ensureJsonText(raw, fallback);
  }
  try {
    return JSON.stringify(raw, null, 2);
  } catch (_error) {
    return fallback;
  }
}

function createPlugin() {
  currentPluginCode.value = "";
  releaseRows.value = [];
  Object.assign(pluginForm, {
    pluginCode: "",
    pluginName: "",
    description: "",
    sourceType: "",
    sourceRef: "",
    iconUrl: "",
    vendor: "",
    category: "",
    tagsJson: "[]",
    manifestJson: "{}",
    detailHtml: "",
    detailJson: "{}",
    isPaid: 0,
    priceCents: 0,
    currency: "CNY",
    status: "draft",
    minCoreVersion: "",
    maxCoreVersion: "",
    latestVersion: "",
    sort: 100
  });
  Object.assign(releaseForm, {
    pluginCode: "",
    version: "",
    changelog: "",
    releaseNotes: "",
    sourceType: "",
    sourceRef: "",
    downloadUrl: "",
    checksumSha256: "",
    signature: "",
    manifestJson: "{}",
    status: "draft",
    publishedAt: 0
  });
}

async function selectPlugin(row) {
  currentPluginCode.value = row.pluginCode || "";
  Object.assign(pluginForm, {
    pluginCode: row.pluginCode || "",
    pluginName: row.pluginName || "",
    description: row.description || "",
    sourceType: row.sourceType || "",
    sourceRef: row.sourceRef || "",
    iconUrl: row.iconUrl || "",
    vendor: row.vendor || "",
    category: row.category || "",
    tagsJson: toPrettyJsonText(row.tags ?? row.tagsJson, "[]"),
    manifestJson: toPrettyJsonText(row.manifest ?? row.manifestJson, "{}"),
    detailHtml: row.detailHtml || "",
    detailJson: toPrettyJsonText(row.detailJson, "{}"),
    isPaid: Number(row.isPaid || 0),
    priceCents: Number(row.priceCents || 0),
    currency: row.currency || "CNY",
    status: row.status || "draft",
    minCoreVersion: row.minCoreVersion || "",
    maxCoreVersion: row.maxCoreVersion || "",
    latestVersion: row.latestVersion || "",
    sort: Number(row.sort || 100)
  });
  Object.assign(releaseForm, {
    pluginCode: row.pluginCode || "",
    version: "",
    changelog: "",
    releaseNotes: "",
    sourceType: row.sourceType || "",
    sourceRef: row.sourceRef || "",
    downloadUrl: "",
    checksumSha256: "",
    signature: "",
    manifestJson: "{}",
    status: "draft",
    publishedAt: 0
  });
  await loadReleases();
}

async function loadHealth() {
  try {
    const resp = await pluginMarketApi.health();
    healthReady.value = !!resp?.data;
    marketBaseUrl.value = resp?.data?.baseUrl || "";
    serviceMessage.value = "";
    return healthReady.value;
  } catch (error) {
    marketBaseUrl.value = "";
    handleServiceFailure(error);
    return false;
  }
}

async function loadPlugins() {
  pluginLoading.value = true;
  try {
    const resp = await pluginMarketApi.listPlugins(pluginQuery);
    const data = resp?.data || {};
    pluginRows.value = data.records || [];
    pluginTotal.value = Number(data.total || 0);
    pluginQuery.page = Number(data.page || pluginQuery.page || 1);
    pluginQuery.pageSize = Number(data.pageSize || pluginQuery.pageSize || 20);
    serviceMessage.value = "";
    return true;
  } catch (error) {
    pluginRows.value = [];
    pluginTotal.value = 0;
    handleServiceFailure(error);
    return false;
  } finally {
    pluginLoading.value = false;
  }
}

function onPluginSearch() {
  pluginQuery.page = 1;
  loadPlugins();
}

function onPluginPageChange(page) {
  pluginQuery.page = Number(page || 1);
  loadPlugins();
}

function onPluginSizeChange(size) {
  pluginQuery.pageSize = Number(size || 20);
  pluginQuery.page = 1;
  loadPlugins();
}

async function savePlugin() {
  if (!pluginForm.pluginCode || !pluginForm.pluginName) {
    ElMessage.warning("插件编码和插件名称不能为空");
    return;
  }
  savingPlugin.value = true;
  try {
    const tagsJsonText = ensureJsonText(pluginForm.tagsJson, "[]");
    const manifestJsonText = ensureJsonText(pluginForm.manifestJson, "{}");
    const detailJsonText = ensureJsonText(pluginForm.detailJson, "{}");
    const payload = {
      ...pluginForm,
      tags: JSON.parse(tagsJsonText),
      manifest: JSON.parse(manifestJsonText),
      detailJson: JSON.parse(detailJsonText),
      isPaid: Number(pluginForm.isPaid) === 1 ? 1 : 0,
      priceCents: Number(pluginForm.priceCents || 0),
      sort: Number(pluginForm.sort || 100)
    };
    await pluginMarketApi.savePlugin(payload);
    ElMessage.success("插件保存成功");
    await loadPlugins();
  } catch (error) {
    ElMessage.error(error?.message || "保存插件失败");
  } finally {
    savingPlugin.value = false;
  }
}

async function loadReleases() {
  if (!currentPluginCode.value) {
    releaseRows.value = [];
    return true;
  }
  try {
    const resp = await pluginMarketApi.listReleases(currentPluginCode.value);
    releaseRows.value = resp?.data?.records || [];
    serviceMessage.value = "";
    return true;
  } catch (error) {
    releaseRows.value = [];
    handleServiceFailure(error);
    return false;
  }
}

function selectRelease(row) {
  Object.assign(releaseForm, {
    pluginCode: row.pluginCode || currentPluginCode.value,
    version: row.version || "",
    changelog: row.changelog || "",
    releaseNotes: row.releaseNotes || "",
    sourceType: row.sourceType || "",
    sourceRef: row.sourceRef || "",
    downloadUrl: row.downloadUrl || "",
    checksumSha256: row.checksumSha256 || "",
    signature: row.signature || "",
    manifestJson: toPrettyJsonText(row.manifest ?? row.manifestJson, "{}"),
    status: row.status || "draft",
    publishedAt: Number(row.publishedAt || 0)
  });
}

async function saveRelease() {
  if (!currentPluginCode.value) {
    ElMessage.warning("请先选择一个插件");
    return;
  }
  if (!releaseForm.version) {
    ElMessage.warning("版本号不能为空");
    return;
  }
  savingRelease.value = true;
  try {
    const manifestJsonText = ensureJsonText(releaseForm.manifestJson, "{}");
    const payload = {
      ...releaseForm,
      pluginCode: currentPluginCode.value,
      manifest: JSON.parse(manifestJsonText),
      publishedAt: Number(releaseForm.publishedAt || 0)
    };
    await pluginMarketApi.saveRelease(payload);
    ElMessage.success("版本保存成功");
    await loadReleases();
    await loadPlugins();
  } catch (error) {
    ElMessage.error(error?.message || "保存版本失败");
  } finally {
    savingRelease.value = false;
  }
}

async function changePluginStatus(nextStatus, pluginCode) {
  const code = String(pluginCode || "").trim();
  if (!code) {
    return;
  }
  const actionMap = {
    publish: pluginMarketApi.publishPlugin,
    offline: pluginMarketApi.offlinePlugin,
    draft: pluginMarketApi.draftPlugin
  };
  const action = actionMap[nextStatus];
  if (!action) {
    return;
  }
  await action(code);
  const label = {
    publish: "发布",
    offline: "下线",
    draft: "转为草稿"
  }[nextStatus] || "更新状态";
  ElMessage.success(`插件已${label}`);
  await loadPlugins();
}

async function deletePlugin(pluginCode) {
  const code = String(pluginCode || "").trim();
  if (!code) {
    return;
  }
  await ElMessageBox.confirm(`确认删除插件“${code}”吗？`, "删除确认", {
    type: "warning"
  });
  await pluginMarketApi.deletePlugin(code);
  ElMessage.success("插件已删除");
  if (currentPluginCode.value === code) {
    createPlugin();
  }
  await loadPlugins();
}

async function loadOrders() {
  orderLoading.value = true;
  try {
    const resp = await pluginMarketApi.listOrders(orderQuery);
    const data = resp?.data || {};
    orderRows.value = data.records || [];
    orderTotal.value = Number(data.total || 0);
    orderQuery.page = Number(data.page || orderQuery.page || 1);
    orderQuery.pageSize = Number(data.pageSize || orderQuery.pageSize || 20);
    serviceMessage.value = "";
    return true;
  } catch (error) {
    orderRows.value = [];
    orderTotal.value = 0;
    handleServiceFailure(error);
    return false;
  } finally {
    orderLoading.value = false;
  }
}

function onOrderSearch() {
  orderQuery.page = 1;
  loadOrders();
}

function onOrderPageChange(page) {
  orderQuery.page = Number(page || 1);
  loadOrders();
}

function onOrderSizeChange(size) {
  orderQuery.pageSize = Number(size || 20);
  orderQuery.page = 1;
  loadOrders();
}

async function loadOperationLogs() {
  try {
    const resp = await pluginMarketApi.listOperationLogs({ limit: 80 });
    operationLogs.value = resp?.data || [];
    serviceMessage.value = "";
    return true;
  } catch (error) {
    operationLogs.value = [];
    handleServiceFailure(error);
    return false;
  }
}

async function refreshAll() {
  const ready = await loadHealth();
  if (!ready) {
    pluginRows.value = [];
    releaseRows.value = [];
    orderRows.value = [];
    operationLogs.value = [];
    pluginTotal.value = 0;
    orderTotal.value = 0;
    return;
  }
  await loadPlugins();
  await loadOrders();
  await loadOperationLogs();
  if (currentPluginCode.value) {
    await loadReleases();
  }
}

onMounted(() => {
  refreshAll();
});
</script>

<style scoped>
.row-2 {
  margin-top: 0;
}

.panel :deep(.el-form-item) {
  margin-bottom: 12px;
}

.panel :deep(.el-space) {
  width: 100%;
}

.table-pagination {
  margin-top: 12px;
  justify-content: flex-end;
}

.status-alert {
  margin-top: 12px;
}
</style>
