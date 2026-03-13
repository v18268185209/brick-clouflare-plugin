<template>
  <div class="panel">
    <div class="panel-title">账号管理</div>
    <div class="toolbar">
      <el-button
        v-permission="'btn:plugins_cloudflare_accounts:create'"
        type="primary"
        @click="openCreate"
      >
        新增账号
      </el-button>
      <el-button @click="refreshAll">刷新列表</el-button>
    </div>

    <el-table :data="store.accounts" stripe>
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="displayName" label="名称" min-width="160" />
      <el-table-column prop="authType" label="鉴权方式" width="120">
        <template #default="{ row }">
          {{ authTypeLabel(row.authType) }}
        </template>
      </el-table-column>
      <el-table-column prop="cloudflareAccountId" label="平台账号标识" min-width="220" />
      <el-table-column prop="defaultZoneId" label="默认站点标识" min-width="220" />
      <el-table-column prop="enabled" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.enabled === 1 ? 'success' : 'info'">
            {{ row.enabled === 1 ? "启用" : "停用" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="320">
        <template #default="{ row }">
          <el-button
            v-permission="'btn:plugins_cloudflare_accounts:update'"
            link
            type="primary"
            @click="openEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            v-permission="'btn:plugins_cloudflare_accounts:verify'"
            link
            type="success"
            @click="verify(row)"
          >
            验证凭据
          </el-button>
          <el-button
            v-permission="'btn:plugins_cloudflare_accounts:loadzones'"
            link
            @click="loadZones(row)"
          >
            读取站点
          </el-button>
          <el-button
            v-permission="'btn:plugins_cloudflare_accounts:delete'"
            link
            type="danger"
            @click="remove(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <el-dialog v-model="dialog.visible" :title="dialog.title" width="640px">
    <el-form :model="form" label-width="140px">
      <el-form-item label="显示名称">
        <el-input v-model="form.displayName" placeholder="例如：生产主账号" />
      </el-form-item>
      <el-form-item label="鉴权方式">
        <el-select v-model="form.authType" style="width: 260px">
          <el-option label="访问令牌" value="API_TOKEN" />
          <el-option label="全局密钥" value="GLOBAL_KEY" />
        </el-select>
      </el-form-item>
      <el-form-item label="访问令牌">
        <el-input
          v-model="form.apiToken"
          type="password"
          show-password
          placeholder="访问令牌模式下必填"
        />
      </el-form-item>
      <el-form-item label="全局密钥">
        <el-input
          v-model="form.apiKey"
          type="password"
          show-password
          placeholder="全局密钥模式下必填"
        />
      </el-form-item>
      <el-form-item label="账号邮箱">
        <el-input
          v-model="form.apiEmail"
          placeholder="全局密钥模式下必填"
        />
      </el-form-item>
      <el-form-item label="平台账号标识">
        <el-input v-model="form.cloudflareAccountId" placeholder="例如：023e105f..." />
      </el-form-item>
      <el-form-item label="默认站点标识">
        <el-input v-model="form.defaultZoneId" placeholder="可留空，首次加载站点后再补充" />
      </el-form-item>
      <el-form-item label="启用状态">
        <el-switch v-model="enabledBool" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialog.visible = false">取消</el-button>
      <el-button
        v-permission="dialog.editId ? 'btn:plugins_cloudflare_accounts:update' : 'btn:plugins_cloudflare_accounts:create'"
        type="primary"
        :loading="submitting"
        @click="submit"
      >
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { api } from "../api/cloudflare";
import { useAccountStore } from "../stores/accountStore";

const store = useAccountStore();
const submitting = ref(false);

const dialog = reactive({
  visible: false,
  title: "新增账号",
  editId: null
});

const form = reactive({
  displayName: "",
  authType: "API_TOKEN",
  apiToken: "",
  apiKey: "",
  apiEmail: "",
  cloudflareAccountId: "",
  defaultZoneId: "",
  enabled: 1,
  remark: ""
});

const enabledBool = computed({
  get: () => form.enabled === 1,
  set: (val) => {
    form.enabled = val ? 1 : 0;
  }
});

function authTypeLabel(value) {
  return value === "GLOBAL_KEY" ? "全局密钥" : "访问令牌";
}

function resetForm() {
  Object.assign(form, {
    displayName: "",
    authType: "API_TOKEN",
    apiToken: "",
    apiKey: "",
    apiEmail: "",
    cloudflareAccountId: "",
    defaultZoneId: "",
    enabled: 1,
    remark: ""
  });
}

function openCreate() {
  resetForm();
  dialog.title = "新增账号";
  dialog.editId = null;
  dialog.visible = true;
}

function openEdit(row) {
  resetForm();
  dialog.title = "编辑账号";
  dialog.editId = row.id;
  Object.assign(form, {
    displayName: row.displayName,
    authType: row.authType,
    cloudflareAccountId: row.cloudflareAccountId,
    defaultZoneId: row.defaultZoneId,
    enabled: row.enabled ?? 1,
    remark: row.remark
  });
  dialog.visible = true;
}

async function submit() {
  submitting.value = true;
  try {
    const payload = { ...form };
    if (dialog.editId) {
      await api.updateAccount(dialog.editId, payload);
      ElMessage.success("账号更新成功");
    } else {
      await api.createAccount(payload);
      ElMessage.success("账号创建成功");
    }
    dialog.visible = false;
    await refreshAll();
  } finally {
    submitting.value = false;
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除账号“${row.displayName}”吗？`, "删除确认", {
    type: "warning"
  });
  await api.deleteAccount(row.id);
  ElMessage.success("账号已删除");
  await refreshAll();
}

async function verify(row) {
  const resp = await api.verifyAccount(row.id);
  if (resp.data?.success) {
    ElMessage.success("账号凭据验证通过");
  } else {
    ElMessage.info("验证已完成，请结合返回内容确认配置是否正确");
  }
}

async function loadZones(row) {
  const resp = await api.listZones(row.id, { page: 1, perPage: 200 });
  const total = resp.data?.result_info?.total_count ?? resp.data?.result?.length ?? 0;
  ElMessage.info(`账号“${row.displayName}”可访问的站点数量：${total}`);
}

async function refreshAll() {
  await store.refresh();
}
</script>
