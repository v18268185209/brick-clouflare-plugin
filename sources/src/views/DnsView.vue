<template>
  <div class="panel">
    <div class="panel-title">解析记录</div>
    <div class="toolbar">
      <el-input v-model="filters.name" placeholder="按记录名称过滤" style="width: 220px" />
      <el-select v-model="filters.type" style="width: 140px" clearable placeholder="记录类型">
        <el-option label="地址记录" value="A" />
        <el-option label="扩展地址记录" value="AAAA" />
        <el-option label="别名映射" value="CNAME" />
        <el-option label="文本说明" value="TXT" />
        <el-option label="邮件交换" value="MX" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button
        v-permission="'btn:plugins_cloudflare_dns:create'"
        @click="openCreate"
      >
        新增记录
      </el-button>
    </div>

    <el-alert
      v-if="!store.selectedZoneId"
      type="warning"
      :closable="false"
      style="margin-bottom: 10px"
    >
      <template #default>请先在顶部选择一个站点。</template>
    </el-alert>

    <el-table :data="records" stripe>
      <el-table-column prop="id" label="记录标识" width="240" />
      <el-table-column prop="type" label="类型" width="100" />
      <el-table-column prop="name" label="名称" min-width="220" />
      <el-table-column prop="content" label="内容" min-width="220" />
      <el-table-column prop="ttl" label="缓存时长" width="100" />
      <el-table-column prop="proxied" label="是否代理" width="100">
        <template #default="{ row }">
          <el-tag :type="row.proxied ? 'success' : 'info'">
            {{ row.proxied ? "是" : "否" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button
            v-permission="'btn:plugins_cloudflare_dns:update'"
            link
            type="primary"
            @click="openEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            v-permission="'btn:plugins_cloudflare_dns:delete'"
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

  <el-dialog v-model="dialog.visible" :title="dialog.title" width="580px">
    <el-form :model="form" label-width="120px">
      <el-form-item label="记录类型">
        <el-input v-model="form.type" placeholder="请输入记录类型代码" />
      </el-form-item>
      <el-form-item label="记录名称">
        <el-input v-model="form.name" placeholder="例如：主页或业务子域名" />
      </el-form-item>
      <el-form-item label="记录内容">
        <el-input v-model="form.content" />
      </el-form-item>
      <el-form-item label="缓存时长">
        <el-input-number v-model="form.ttl" :min="1" />
      </el-form-item>
      <el-form-item label="代理开关">
        <el-switch v-model="form.proxied" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialog.visible = false">取消</el-button>
      <el-button
        v-permission="dialog.editId ? 'btn:plugins_cloudflare_dns:update' : 'btn:plugins_cloudflare_dns:create'"
        type="primary"
        @click="submit"
      >
        保存
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
const records = ref([]);

const dialog = reactive({
  visible: false,
  title: "新增解析记录",
  editId: ""
});

const filters = reactive({
  type: "",
  name: ""
});

const form = reactive({
  type: "A",
  name: "",
  content: "",
  ttl: 1,
  proxied: false
});

function resetForm() {
  Object.assign(form, {
    type: "A",
    name: "",
    content: "",
    ttl: 1,
    proxied: false
  });
}

function contextReady() {
  if (!store.selectedAccountId || !store.selectedZoneId) {
    ElMessage.warning("请先选择账号和站点");
    return false;
  }
  return true;
}

function openCreate() {
  resetForm();
  dialog.title = "新增解析记录";
  dialog.editId = "";
  dialog.visible = true;
}

function openEdit(row) {
  resetForm();
  dialog.title = "编辑解析记录";
  dialog.editId = row.id;
  Object.assign(form, {
    type: row.type || "A",
    name: row.name || "",
    content: row.content || "",
    ttl: row.ttl || 1,
    proxied: row.proxied === true
  });
  dialog.visible = true;
}

async function load() {
  if (!contextReady()) return;
  const resp = await api.listDnsRecords({
    accountId: store.selectedAccountId,
    zoneId: store.selectedZoneId,
    type: filters.type || undefined,
    name: filters.name || undefined
  });
  records.value = resp.data?.result || [];
}

async function submit() {
  if (!contextReady()) return;
  const payload = {
    accountId: store.selectedAccountId,
    payload: {
      zoneId: store.selectedZoneId,
      type: form.type,
      name: form.name,
      content: form.content,
      ttl: form.ttl,
      proxied: form.proxied
    }
  };
  if (dialog.editId) {
    await api.updateDnsRecord(dialog.editId, payload);
    ElMessage.success("解析记录更新成功");
  } else {
    await api.createDnsRecord(payload);
    ElMessage.success("解析记录创建成功");
  }
  dialog.visible = false;
  await load();
}

async function remove(row) {
  if (!contextReady()) return;
  await api.deleteDnsRecord(row.id, {
    accountId: store.selectedAccountId,
    zoneId: store.selectedZoneId
  });
  ElMessage.success("解析记录已删除");
  await load();
}
</script>
