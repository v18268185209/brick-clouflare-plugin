<template>
  <div class="row-2">
    <div class="panel">
      <div class="panel-title">边缘脚本</div>
      <div class="toolbar">
        <el-input
          v-model="cfAccountId"
          placeholder="平台账号标识（默认取当前账号配置）"
          style="width: 340px"
        />
        <el-button type="primary" @click="loadScripts">查询脚本</el-button>
      </div>

      <el-table :data="scripts" stripe>
        <el-table-column prop="id" label="脚本名称" min-width="180" />
        <el-table-column prop="modified_on" label="最后修改时间" min-width="200" />
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button
              v-permission="'btn:plugins_cloudflare_workers:read'"
              link
              @click="loadScriptCode(row.id)"
            >
              读取代码
            </el-button>
            <el-button
              v-permission="'btn:plugins_cloudflare_workers:publish'"
              link
              type="primary"
              @click="useScriptName(row.id)"
            >
              编辑
            </el-button>
            <el-button
              v-permission="'btn:plugins_cloudflare_workers:delete'"
              link
              type="danger"
              @click="removeScript(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 12px">
        <el-input
          v-model="editor.scriptName"
          placeholder="请输入脚本名称"
          style="margin-bottom: 8px"
        />
        <el-input
          v-model="editor.script"
          type="textarea"
          :rows="8"
          placeholder="请输入脚本代码"
          style="margin-bottom: 8px"
        />
        <el-button
          v-permission="'btn:plugins_cloudflare_workers:publish'"
          type="success"
          @click="publishScript"
        >
          发布或更新脚本
        </el-button>
      </div>
    </div>

    <div class="panel">
      <div class="panel-title">脚本路由</div>
      <div class="toolbar">
        <el-button type="primary" @click="loadRoutes">查询路由</el-button>
      </div>
      <el-table :data="routes" stripe height="300">
        <el-table-column prop="id" label="编号" width="220" />
        <el-table-column prop="pattern" label="匹配规则" min-width="180" />
        <el-table-column prop="script" label="脚本名称" min-width="140" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              v-permission="'btn:plugins_cloudflare_workers:routedelete'"
              link
              type="danger"
              @click="removeRoute(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 10px">
        <el-input
          v-model="newRoute.pattern"
          placeholder="例如：example.com/*"
          style="margin-bottom: 8px"
        />
        <el-input
          v-model="newRoute.script"
          placeholder="请输入脚本名称"
          style="margin-bottom: 8px"
        />
        <el-button
          v-permission="'btn:plugins_cloudflare_workers:routecreate'"
          type="success"
          @click="createRoute"
        >
          新增路由
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
const scripts = ref([]);
const routes = ref([]);
const newRoute = reactive({
  pattern: "",
  script: ""
});
const editor = reactive({
  scriptName: "",
  script: ""
});

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

function zoneReady() {
  if (!store.selectedZoneId) {
    ElMessage.warning("请先选择站点");
    return false;
  }
  return true;
}

async function loadScripts() {
  if (!accountReady()) return;
  const resp = await api.listWorkerScripts({
    accountId: store.selectedAccountId,
    cloudflareAccountId: resolveCfAccountId()
  });
  scripts.value = resp.data?.result || [];
}

async function removeScript(name) {
  if (!accountReady()) return;
  await api.deleteWorkerScript(name, {
    accountId: store.selectedAccountId,
    cloudflareAccountId: resolveCfAccountId()
  });
  ElMessage.success("脚本删除成功");
  await loadScripts();
}

function useScriptName(name) {
  editor.scriptName = name || "";
}

async function loadScriptCode(name) {
  if (!accountReady()) return;
  const resp = await api.getWorkerScript(name, {
    accountId: store.selectedAccountId,
    cloudflareAccountId: resolveCfAccountId()
  });
  editor.scriptName = name || "";
  editor.script = resp.data || "";
}

async function publishScript() {
  if (!accountReady()) return;
  if (!editor.scriptName.trim()) {
    ElMessage.warning("脚本名称不能为空");
    return;
  }
  if (!editor.script.trim()) {
    ElMessage.warning("脚本内容不能为空");
    return;
  }
  await api.putWorkerScript(editor.scriptName.trim(), {
    accountId: store.selectedAccountId,
    payload: {
      script: editor.script,
      cloudflareAccountId: resolveCfAccountId()
    }
  });
  ElMessage.success("脚本发布成功");
  await loadScripts();
}

async function loadRoutes() {
  if (!accountReady() || !zoneReady()) return;
  const resp = await api.listWorkerRoutes({
    accountId: store.selectedAccountId,
    zoneId: store.selectedZoneId
  });
  routes.value = resp.data?.result || [];
}

async function createRoute() {
  if (!accountReady() || !zoneReady()) return;
  if (!newRoute.pattern.trim() || !newRoute.script.trim()) {
    ElMessage.warning("匹配规则和脚本名称不能为空");
    return;
  }
  await api.createWorkerRoute({
    accountId: store.selectedAccountId,
    payload: {
      zoneId: store.selectedZoneId,
      pattern: newRoute.pattern.trim(),
      script: newRoute.script.trim()
    }
  });
  ElMessage.success("路由创建成功");
  newRoute.pattern = "";
  newRoute.script = "";
  await loadRoutes();
}

async function removeRoute(routeId) {
  if (!accountReady() || !zoneReady()) return;
  await api.deleteWorkerRoute(routeId, {
    accountId: store.selectedAccountId,
    zoneId: store.selectedZoneId
  });
  ElMessage.success("路由删除成功");
  await loadRoutes();
}
</script>
