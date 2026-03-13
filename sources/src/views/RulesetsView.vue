<template>
  <div class="panel">
    <div class="panel-title">安全规则集</div>
    <div class="toolbar">
      <el-select v-model="phase" style="width: 320px">
        <el-option label="自定义防火墙阶段" value="http_request_firewall_custom" />
        <el-option label="托管防火墙阶段" value="http_request_firewall_managed" />
        <el-option label="缓存设置阶段" value="http_request_cache_settings" />
      </el-select>
      <el-button type="primary" @click="loadEntrypoint">读取入口规则集</el-button>
      <el-button
        v-permission="'btn:plugins_cloudflare_rulesets:create'"
        @click="showCreate = true"
      >
        新增规则
      </el-button>
      <el-button
        v-permission="'btn:plugins_cloudflare_rulesets:toggle'"
        :disabled="!rulesetDetail.id"
        @click="syncEntrypoint"
      >
        同步入口规则集
      </el-button>
      <el-button :disabled="!rulesetId" @click="loadRulesetDetail">刷新详情</el-button>
    </div>

    <el-alert v-if="rulesetId" type="info" :closable="false" style="margin-bottom: 12px">
      <template #default>
        当前规则集标识：<code>{{ rulesetId }}</code>
      </template>
    </el-alert>

    <el-table :data="rules" stripe>
      <el-table-column prop="id" label="规则标识" width="250" />
      <el-table-column prop="action" label="动作" width="140">
        <template #default="{ row }">
          {{ actionLabel(row.action) }}
        </template>
      </el-table-column>
      <el-table-column label="启用" width="110">
        <template #default="{ row }">
          <el-switch
            v-permission="'btn:plugins_cloudflare_rulesets:toggle'"
            :model-value="row.enabled !== false"
            @change="(v) => toggleRule(row, v)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="expression" label="匹配表达式" min-width="280" />
      <el-table-column prop="description" label="说明" min-width="220" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button
            v-permission="'btn:plugins_cloudflare_rulesets:delete'"
            link
            type="danger"
            @click="removeRule(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-divider />

    <el-form :model="rulesetDetail" label-width="130px">
      <el-form-item label="规则集名称">
        <el-input :model-value="rulesetDetail.name || '-'" readonly />
      </el-form-item>
      <el-form-item label="规则集说明">
        <el-input :model-value="rulesetDetail.description || '-'" readonly />
      </el-form-item>
      <el-form-item label="阶段">
        <el-input :model-value="rulesetDetail.phase || phase" readonly />
      </el-form-item>
      <el-form-item label="详情预览">
        <el-input
          :model-value="detailPreview"
          type="textarea"
          :rows="8"
          readonly
        />
      </el-form-item>
    </el-form>
  </div>

  <el-dialog v-model="showCreate" title="新增规则" width="760px">
    <el-form :model="createForm" label-width="120px">
      <el-form-item label="动作">
        <el-select v-model="createForm.action" style="width: 220px">
          <el-option label="阻断" value="block" />
          <el-option label="托管质询" value="managed_challenge" />
          <el-option label="质询" value="challenge" />
          <el-option label="脚本质询" value="js_challenge" />
          <el-option label="跳过" value="skip" />
          <el-option label="记录" value="log" />
        </el-select>
      </el-form-item>
      <el-form-item label="匹配表达式">
        <el-input
          v-model="createForm.expression"
          type="textarea"
          :rows="3"
          placeholder="示例：(ip.src in {1.1.1.1 2.2.2.2})"
        />
      </el-form-item>
      <el-form-item label="规则说明">
        <el-input v-model="createForm.description" />
      </el-form-item>
      <el-form-item label="启用">
        <el-switch v-model="createForm.enabled" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showCreate = false">取消</el-button>
      <el-button
        v-permission="'btn:plugins_cloudflare_rulesets:create'"
        type="primary"
        @click="createRule"
      >
        创建
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { api } from "../api/cloudflare";
import { useAccountStore } from "../stores/accountStore";

const store = useAccountStore();
const phase = ref("http_request_firewall_custom");
const rulesetId = ref("");
const rules = ref([]);
const showCreate = ref(false);
const rulesetDetail = reactive({
  id: "",
  name: "",
  description: "",
  kind: "",
  phase: "",
  rules: []
});
const createForm = reactive({
  action: "block",
  expression: "",
  description: "",
  enabled: true
});

const detailPreview = computed(() =>
  JSON.stringify(
    {
      id: rulesetDetail.id,
      name: rulesetDetail.name,
      description: rulesetDetail.description,
      kind: rulesetDetail.kind,
      phase: rulesetDetail.phase,
      rules: rulesetDetail.rules || []
    },
    null,
    2
  )
);

function actionLabel(value) {
  const labels = {
    block: "阻断",
    managed_challenge: "托管质询",
    challenge: "质询",
    js_challenge: "脚本质询",
    skip: "跳过",
    log: "记录"
  };
  return labels[value] || value || "-";
}

function contextReady() {
  if (!store.selectedAccountId || !store.selectedZoneId) {
    ElMessage.warning("请先选择账号和站点");
    return false;
  }
  return true;
}

function applyRulesetDetail(detail) {
  rulesetDetail.id = detail?.id || "";
  rulesetDetail.name = detail?.name || "";
  rulesetDetail.description = detail?.description || "";
  rulesetDetail.kind = detail?.kind || "";
  rulesetDetail.phase = detail?.phase || phase.value;
  rulesetDetail.rules = Array.isArray(detail?.rules) ? detail.rules : [];
}

async function loadEntrypoint() {
  if (!contextReady()) return;
  const resp = await api.getRulesetEntrypoint({
    accountId: store.selectedAccountId,
    zoneId: store.selectedZoneId,
    phase: phase.value
  });
  const result = resp.data?.result || {};
  rulesetId.value = result.id || "";
  rules.value = result.rules || [];
  applyRulesetDetail(result);
  if (rulesetId.value) {
    await loadRulesetDetail();
  }
}

async function loadRulesetDetail() {
  if (!contextReady() || !rulesetId.value) return;
  const resp = await api.getRulesetDetail(rulesetId.value, {
    accountId: store.selectedAccountId,
    zoneId: store.selectedZoneId
  });
  const detail = resp.data?.result || {};
  applyRulesetDetail(detail);
  rules.value = detail.rules || rules.value;
}

async function syncEntrypoint() {
  if (!contextReady() || !rulesetDetail.id) {
    ElMessage.warning("请先读取规则集详情");
    return;
  }
  await api.updateRulesetEntrypoint({
    accountId: store.selectedAccountId,
    payload: {
      zoneId: store.selectedZoneId,
      phase: phase.value,
      id: rulesetDetail.id,
      name: rulesetDetail.name,
      description: rulesetDetail.description,
      kind: rulesetDetail.kind,
      rules: rulesetDetail.rules || []
    }
  });
  ElMessage.success("入口规则集同步成功");
  await loadEntrypoint();
}

async function createRule() {
  if (!contextReady()) return;
  if (!rulesetId.value) {
    ElMessage.warning("请先读取入口规则集");
    return;
  }
  if (!createForm.expression.trim()) {
    ElMessage.warning("匹配表达式不能为空");
    return;
  }

  await api.createRulesetRule(rulesetId.value, {
    accountId: store.selectedAccountId,
    payload: {
      zoneId: store.selectedZoneId,
      rule: {
        action: createForm.action,
        expression: createForm.expression.trim(),
        description: createForm.description,
        enabled: createForm.enabled
      }
    }
  });
  ElMessage.success("规则创建成功");
  showCreate.value = false;
  createForm.expression = "";
  createForm.description = "";
  await loadEntrypoint();
}

async function toggleRule(row, enabled) {
  if (!contextReady()) return;
  if (!rulesetId.value || !row?.id) return;

  await api.updateRulesetRule(rulesetId.value, row.id, {
    accountId: store.selectedAccountId,
    payload: {
      zoneId: store.selectedZoneId,
      rule: {
        action: row.action,
        expression: row.expression,
        description: row.description,
        enabled
      }
    }
  });
  ElMessage.success("规则状态更新成功");
  await loadEntrypoint();
}

async function removeRule(row) {
  if (!contextReady()) return;
  if (!rulesetId.value || !row?.id) return;
  await api.deleteRulesetRule(rulesetId.value, row.id, {
    accountId: store.selectedAccountId,
    zoneId: store.selectedZoneId
  });
  ElMessage.success("规则删除成功");
  await loadEntrypoint();
}
</script>
