<template>
  <div class="panel">
    <div class="panel-title">对象存储</div>
    <div class="toolbar">
      <el-input
        v-model="cfAccountId"
        placeholder="平台账号标识（默认取当前账号配置）"
        style="width: 360px"
      />
      <el-button type="primary" @click="load">查询</el-button>
      <el-input v-model="newBucket" placeholder="新建存储桶名称" style="width: 220px" />
      <el-button
        v-permission="'btn:plugins_cloudflare_r2:create'"
        type="success"
        @click="create"
      >
        创建存储桶
      </el-button>
    </div>

    <el-table :data="buckets" stripe>
      <el-table-column prop="name" label="存储桶名称" min-width="220" />
      <el-table-column prop="location" label="区域" width="140" />
      <el-table-column prop="creation_date" label="创建时间" min-width="220" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button
            v-permission="'btn:plugins_cloudflare_r2:delete'"
            link
            type="danger"
            @click="remove(row.name)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { api } from "../api/cloudflare";
import { useAccountStore } from "../stores/accountStore";

const store = useAccountStore();
const buckets = ref([]);
const newBucket = ref("");
const cfAccountId = ref("");

function resolveCfAccountId() {
  return cfAccountId.value || store.selectedCloudflareAccountId || undefined;
}

function contextReady() {
  if (!store.selectedAccountId) {
    ElMessage.warning("请先选择账号");
    return false;
  }
  return true;
}

async function load() {
  if (!contextReady()) return;
  const resp = await api.listR2Buckets({
    accountId: store.selectedAccountId,
    cloudflareAccountId: resolveCfAccountId()
  });
  buckets.value = resp.data?.result?.buckets || [];
}

async function create() {
  if (!contextReady()) return;
  if (!newBucket.value.trim()) {
    ElMessage.warning("请输入存储桶名称");
    return;
  }
  await api.createR2Bucket({
    accountId: store.selectedAccountId,
    payload: {
      bucketName: newBucket.value.trim(),
      cloudflareAccountId: resolveCfAccountId()
    }
  });
  ElMessage.success("存储桶创建成功");
  newBucket.value = "";
  await load();
}

async function remove(name) {
  if (!contextReady()) return;
  await api.deleteR2Bucket(name, {
    accountId: store.selectedAccountId,
    cloudflareAccountId: resolveCfAccountId()
  });
  ElMessage.success("存储桶删除成功");
  await load();
}
</script>
