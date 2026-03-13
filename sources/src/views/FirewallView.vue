<template>
  <div class="panel">
    <div class="panel-title">防火墙规则</div>
    <div class="toolbar">
      <el-button type="primary" @click="load">查询规则</el-button>
      <el-button
        v-permission="'btn:plugins_cloudflare_firewall:create'"
        @click="showCreate = true"
      >
        新增规则
      </el-button>
    </div>

    <el-table :data="rules" stripe>
      <el-table-column prop="id" label="规则标识" width="250" />
      <el-table-column prop="action" label="动作" width="120" />
      <el-table-column label="启用" width="110">
        <template #default="{ row }">
          <el-switch
            v-permission="'btn:plugins_cloudflare_firewall:toggle'"
            :model-value="row.paused !== true"
            @change="(enabled) => toggleRuleStatus(row, enabled)"
          />
        </template>
      </el-table-column>
      <el-table-column label="匹配表达式" min-width="320">
        <template #default="{ row }">
          {{ row.filter?.expression }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="规则说明" min-width="220" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button
            v-permission="'btn:plugins_cloudflare_firewall:delete'"
            link
            type="danger"
            @click="remove(row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <el-dialog v-model="showCreate" title="新增防火墙规则" width="640px">
    <el-form :model="form" label-width="120px">
      <el-form-item label="动作">
        <el-select v-model="form.action" style="width: 220px">
          <el-option label="阻断" value="block" />
          <el-option label="质询" value="challenge" />
          <el-option label="放行" value="allow" />
          <el-option label="记录" value="log" />
        </el-select>
      </el-form-item>
      <el-form-item label="匹配表达式">
        <el-input
          v-model="form.expression"
          type="textarea"
          :rows="3"
          placeholder="示例：(ip.src in {1.1.1.1 2.2.2.2})"
        />
      </el-form-item>
      <el-form-item label="规则说明">
        <el-input v-model="form.description" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showCreate = false">取消</el-button>
      <el-button
        v-permission="'btn:plugins_cloudflare_firewall:create'"
        type="primary"
        @click="create"
      >
        创建
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { api } from "../api/cloudflare";
import { useAccountStore } from "../stores/accountStore";

const store = useAccountStore();
const rules = ref([]);
const showCreate = ref(false);
const form = reactive({
  action: "block",
  expression: "",
  description: ""
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
  const resp = await api.listFirewallRules({
    accountId: store.selectedAccountId,
    zoneId: store.selectedZoneId
  });
  rules.value = resp.data?.result || [];
}

async function create() {
  if (!contextReady()) return;
  if (!form.expression.trim()) {
    ElMessage.warning("匹配表达式不能为空");
    return;
  }
  await api.createFirewallRules({
    accountId: store.selectedAccountId,
    payload: {
      zoneId: store.selectedZoneId,
      rules: [
        {
          action: form.action,
          filter: { expression: form.expression.trim() },
          description: form.description
        }
      ]
    }
  });
  ElMessage.success("防火墙规则创建成功");
  showCreate.value = false;
  form.expression = "";
  form.description = "";
  await load();
}

async function remove(ruleId) {
  if (!contextReady()) return;
  await api.deleteFirewallRule(ruleId, {
    accountId: store.selectedAccountId,
    zoneId: store.selectedZoneId
  });
  ElMessage.success("规则删除成功");
  await load();
}

function buildUpdatePayload(row, enabled) {
  const payload = {
    zoneId: store.selectedZoneId,
    action: row.action,
    filter: {
      expression: row.filter?.expression || ""
    },
    paused: !enabled
  };
  if (row.filter?.id) {
    payload.filter.id = row.filter.id;
  }
  if (row.description) {
    payload.description = row.description;
  }
  if (row.priority !== undefined && row.priority !== null) {
    payload.priority = row.priority;
  }
  if (Array.isArray(row.products) && row.products.length > 0) {
    payload.products = row.products;
  }
  return payload;
}

async function toggleRuleStatus(row, enabled) {
  if (!contextReady()) return;
  if (!row?.id) return;
  await api.updateFirewallRule(row.id, {
    accountId: store.selectedAccountId,
    payload: buildUpdatePayload(row, enabled)
  });
  ElMessage.success(enabled ? "规则已启用" : "规则已暂停");
  await load();
}
</script>
