<template>
  <div class="panel">
    <div class="panel-title">页面规则</div>
    <div class="toolbar">
      <el-button type="primary" @click="load">查询规则</el-button>
      <el-button
        v-permission="'btn:plugins_cloudflare_page_rules:create'"
        @click="showCreate = true"
      >
        新增规则
      </el-button>
    </div>

    <el-table :data="rules" stripe>
      <el-table-column prop="id" label="规则标识" width="250" />
      <el-table-column label="匹配地址" min-width="260">
        <template #default="{ row }">
          {{ row.targets?.[0]?.constraint?.value }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          {{ row.status === "disabled" ? "停用" : "启用" }}
        </template>
      </el-table-column>
      <el-table-column label="启用" width="110">
        <template #default="{ row }">
          <el-switch
            v-permission="'btn:plugins_cloudflare_page_rules:toggle'"
            :model-value="row.status !== 'disabled'"
            @change="(enabled) => toggleRuleStatus(row, enabled)"
          />
        </template>
      </el-table-column>
      <el-table-column label="规则动作" min-width="260">
        <template #default="{ row }">
          <el-tag
            v-for="(action, idx) in row.actions || []"
            :key="`${row.id}-${idx}`"
            style="margin-right: 6px"
            size="small"
          >
            {{ actionLabel(action.id) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button
            v-permission="'btn:plugins_cloudflare_page_rules:delete'"
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

  <el-dialog v-model="showCreate" title="新增页面规则" width="680px">
    <el-form :model="createForm" label-width="120px">
      <el-form-item label="匹配地址">
        <el-input v-model="createForm.target" placeholder="例如：example.com/*" />
      </el-form-item>
      <el-form-item label="规则动作">
        <el-select v-model="createForm.action" style="width: 220px">
          <el-option label="转发地址" value="forwarding_url" />
          <el-option label="缓存级别" value="cache_level" />
          <el-option label="强制 HTTPS" value="always_use_https" />
        </el-select>
      </el-form-item>
      <el-form-item label="动作值">
        <el-input
          v-model="createForm.value"
          placeholder='例如：转发地址使用 {"url":"https://a.com","status_code":301}'
        />
      </el-form-item>
      <el-form-item label="启用状态">
        <el-select v-model="createForm.status" style="width: 160px">
          <el-option label="启用" value="active" />
          <el-option label="停用" value="disabled" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showCreate = false">取消</el-button>
      <el-button
        v-permission="'btn:plugins_cloudflare_page_rules:create'"
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
const createForm = reactive({
  target: "",
  action: "cache_level",
  value: "bypass",
  status: "active"
});

function actionLabel(value) {
  const labels = {
    forwarding_url: "转发地址",
    cache_level: "缓存级别",
    always_use_https: "强制 HTTPS"
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

async function load() {
  if (!contextReady()) return;
  const resp = await api.listPageRules({
    accountId: store.selectedAccountId,
    zoneId: store.selectedZoneId
  });
  rules.value = resp.data?.result || [];
}

function buildActionValue() {
  if (createForm.action === "forwarding_url") {
    try {
      return JSON.parse(createForm.value);
    } catch (_error) {
      throw new Error("转发地址动作值必须是合法的 JSON");
    }
  }
  return createForm.value;
}

async function create() {
  if (!contextReady()) return;
  if (!createForm.target.trim()) {
    ElMessage.warning("匹配地址不能为空");
    return;
  }
  let actionValue;
  try {
    actionValue = buildActionValue();
  } catch (error) {
    ElMessage.warning(error.message);
    return;
  }

  await api.createPageRule({
    accountId: store.selectedAccountId,
    payload: {
      zoneId: store.selectedZoneId,
      targets: [
        {
          target: "url",
          constraint: {
            operator: "matches",
            value: createForm.target.trim()
          }
        }
      ],
      actions: [
        {
          id: createForm.action,
          value: actionValue
        }
      ],
      status: createForm.status
    }
  });
  ElMessage.success("页面规则创建成功");
  showCreate.value = false;
  createForm.target = "";
  await load();
}

async function remove(ruleId) {
  if (!contextReady()) return;
  await api.deletePageRule(ruleId, {
    accountId: store.selectedAccountId,
    zoneId: store.selectedZoneId
  });
  ElMessage.success("规则删除成功");
  await load();
}

function buildUpdatePayload(row, enabled) {
  const payload = {
    zoneId: store.selectedZoneId,
    status: enabled ? "active" : "disabled",
    targets: Array.isArray(row.targets) ? row.targets : [],
    actions: Array.isArray(row.actions) ? row.actions : []
  };
  if (row.priority !== undefined && row.priority !== null) {
    payload.priority = row.priority;
  }
  return payload;
}

async function toggleRuleStatus(row, enabled) {
  if (!contextReady()) return;
  if (!row?.id) return;
  await api.updatePageRule(row.id, {
    accountId: store.selectedAccountId,
    payload: buildUpdatePayload(row, enabled)
  });
  ElMessage.success(enabled ? "规则已启用" : "规则已停用");
  await load();
}
</script>
