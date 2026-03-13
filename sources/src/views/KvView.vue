<template>
  <div class="row-2">
    <div class="panel">
      <div class="panel-title">键值命名空间</div>
      <div class="toolbar">
        <el-input
          v-model="cfAccountId"
          placeholder="平台账号标识（默认取当前账号配置）"
          style="width: 340px"
        />
        <el-button type="primary" @click="loadNamespaces">查询命名空间</el-button>
      </div>
      <div class="toolbar">
        <el-input v-model="newNamespace" placeholder="新命名空间名称" style="width: 220px" />
        <el-button
          v-permission="'btn:plugins_cloudflare_kv:nscreate'"
          type="success"
          @click="createNamespace"
        >
          创建
        </el-button>
      </div>

      <el-table :data="namespaces" stripe>
        <el-table-column prop="id" label="命名空间标识" min-width="200" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="selectNamespace(row)">管理键值</el-button>
            <el-button
              v-permission="'btn:plugins_cloudflare_kv:nsdelete'"
              link
              type="danger"
              @click="removeNamespace(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="panel">
      <div class="panel-title">键值管理 - {{ selectedNamespace?.title || "-" }}</div>
      <div class="toolbar">
        <el-input v-model="prefix" placeholder="按前缀过滤" style="width: 220px" />
        <el-button type="primary" @click="loadKeys">查询键值</el-button>
      </div>

      <el-table :data="keys" stripe height="260">
        <el-table-column prop="name" label="键名" min-width="200" />
        <el-table-column prop="expiration" label="过期时间" min-width="140" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button
              v-permission="'btn:plugins_cloudflare_kv:keyread'"
              link
              @click="readValue(row.name)"
            >
              读取
            </el-button>
            <el-button
              v-permission="'btn:plugins_cloudflare_kv:keydelete'"
              link
              type="danger"
              @click="removeValue(row.name)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 10px">
        <el-input v-model="edit.key" placeholder="请输入键名" style="margin-bottom: 8px" />
        <el-input
          v-model="edit.value"
          type="textarea"
          :rows="4"
          placeholder="请输入键值内容"
          style="margin-bottom: 8px"
        />
        <el-button
          v-permission="'btn:plugins_cloudflare_kv:keywrite'"
          type="success"
          @click="putValue"
        >
          写入或更新
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { api } from "../api/cloudflare";
import { useAccountStore } from "../stores/accountStore";

const store = useAccountStore();
const cfAccountId = ref("");
const newNamespace = ref("");
const namespaces = ref([]);
const selectedNamespace = ref(null);
const keys = ref([]);
const prefix = ref("");
const edit = reactive({ key: "", value: "" });

function resolveCfAccountId() {
  return cfAccountId.value || store.selectedCloudflareAccountId || undefined;
}

function accountReady() {
  if (!store.selectedAccountId) {
    ElMessage.warning("请先选择账号");
    return false;
  }
  return true;
}

async function loadNamespaces() {
  if (!accountReady()) return;
  const resp = await api.listKvNamespaces({
    accountId: store.selectedAccountId,
    cloudflareAccountId: resolveCfAccountId()
  });
  namespaces.value = resp.data?.result || [];
}

async function createNamespace() {
  if (!accountReady()) return;
  if (!newNamespace.value.trim()) {
    ElMessage.warning("请输入命名空间名称");
    return;
  }
  await api.createKvNamespace({
    accountId: store.selectedAccountId,
    payload: {
      title: newNamespace.value.trim(),
      cloudflareAccountId: resolveCfAccountId()
    }
  });
  ElMessage.success("命名空间创建成功");
  newNamespace.value = "";
  await loadNamespaces();
}

async function removeNamespace(namespaceId) {
  if (!accountReady()) return;
  await api.deleteKvNamespace(namespaceId, {
    accountId: store.selectedAccountId,
    cloudflareAccountId: resolveCfAccountId()
  });
  ElMessage.success("命名空间删除成功");
  await loadNamespaces();
}

function selectNamespace(ns) {
  selectedNamespace.value = ns;
  keys.value = [];
  edit.key = "";
  edit.value = "";
  loadKeys();
}

async function loadKeys() {
  if (!accountReady()) return;
  if (!selectedNamespace.value) {
    ElMessage.warning("请先选择一个命名空间");
    return;
  }
  const resp = await api.listKvKeys(selectedNamespace.value.id, {
    accountId: store.selectedAccountId,
    cloudflareAccountId: resolveCfAccountId(),
    prefix: prefix.value || undefined
  });
  keys.value = resp.data?.result || [];
}

async function readValue(key) {
  if (!accountReady()) return;
  const resp = await api.getKvValue(selectedNamespace.value.id, key, {
    accountId: store.selectedAccountId,
    cloudflareAccountId: resolveCfAccountId()
  });
  edit.key = key;
  edit.value = resp.data || "";
}

async function putValue() {
  if (!accountReady()) return;
  if (!selectedNamespace.value) {
    ElMessage.warning("请先选择命名空间");
    return;
  }
  if (!edit.key.trim()) {
    ElMessage.warning("键名不能为空");
    return;
  }
  await api.putKvValue(selectedNamespace.value.id, edit.key.trim(), {
    accountId: store.selectedAccountId,
    payload: {
      value: edit.value,
      cloudflareAccountId: resolveCfAccountId()
    }
  });
  ElMessage.success("键值写入成功");
  await loadKeys();
}

async function removeValue(key) {
  if (!accountReady()) return;
  await api.deleteKvValue(selectedNamespace.value.id, key, {
    accountId: store.selectedAccountId,
    cloudflareAccountId: resolveCfAccountId()
  });
  ElMessage.success("键值删除成功");
  await loadKeys();
}
</script>
