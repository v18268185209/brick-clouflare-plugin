<template>
  <div class="panel">
    <div class="panel-title">授权中心</div>
    <div class="toolbar">
      <el-tag :type="ready ? 'success' : 'danger'" effect="plain">
        {{ ready ? "已连接" : "未连接" }}
      </el-tag>
      <span class="muted">当前管理员：{{ adminUser || "-" }}</span>
      <el-button @click="refreshAll">刷新数据</el-button>
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

  <div class="panel">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="套餐配置" name="plan">
        <div class="toolbar">
          <el-button
            v-permission="'btn:plugins_cloudflare_worker_admin:save'"
            type="primary"
            @click="savePlanConfig"
          >
            保存套餐配置
          </el-button>
        </div>
        <el-input v-model="planConfigText" type="textarea" :rows="16" />
      </el-tab-pane>

      <el-tab-pane label="支付配置" name="payment">
        <div class="toolbar">
          <el-button
            v-permission="'btn:plugins_cloudflare_worker_admin:save'"
            type="primary"
            @click="savePaymentConfig"
          >
            保存支付配置
          </el-button>
        </div>
        <el-input v-model="paymentConfigText" type="textarea" :rows="16" />
      </el-tab-pane>

      <el-tab-pane label="邮件配置" name="email">
        <div class="toolbar">
          <el-button
            v-permission="'btn:plugins_cloudflare_worker_admin:save'"
            type="primary"
            @click="saveEmailConfig"
          >
            保存邮件配置
          </el-button>
          <el-input v-model="testEmail" placeholder="请输入测试邮箱地址" style="width: 260px" />
          <el-button v-permission="'btn:plugins_cloudflare_worker_admin:emailtest'" @click="sendTestEmail">
            发送测试邮件
          </el-button>
        </div>
        <el-input v-model="emailConfigText" type="textarea" :rows="16" />
      </el-tab-pane>

      <el-tab-pane label="菜单编码" name="menu">
        <div class="toolbar">
          <el-button
            v-permission="'btn:plugins_cloudflare_worker_admin:save'"
            type="primary"
            @click="saveMenuCodes"
          >
            保存菜单编码
          </el-button>
        </div>
        <el-input v-model="menuCodesText" type="textarea" :rows="14" />
      </el-tab-pane>

      <el-tab-pane label="其他配置" name="other">
        <div class="toolbar">
          <el-button
            v-permission="'btn:plugins_cloudflare_worker_admin:save'"
            type="primary"
            @click="saveOtherConfig"
          >
            保存其他配置
          </el-button>
        </div>
        <el-input v-model="otherConfigText" type="textarea" :rows="16" />
      </el-tab-pane>

      <el-tab-pane label="密码安全" name="security">
        <el-form :model="passwordForm" label-width="140px" style="max-width: 560px">
          <el-form-item label="旧密码">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="passwordForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button
              v-permission="'btn:plugins_cloudflare_worker_admin:passwordchange'"
              type="primary"
              @click="changePassword"
            >
              修改管理员密码
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="授权管理" name="licenses">
        <div class="toolbar">
          <el-input
            v-model="licenseQuery.keyword"
            placeholder="按机器码或授权标识搜索"
            clearable
            style="width: 220px"
            @keyup.enter="loadLicenses"
          />
          <el-input
            v-model="licenseQuery.plan"
            placeholder="按套餐筛选"
            clearable
            style="width: 160px"
            @keyup.enter="loadLicenses"
          />
          <el-input
            v-model="licenseQuery.status"
            placeholder="按状态筛选"
            clearable
            style="width: 160px"
            @keyup.enter="loadLicenses"
          />
          <el-button @click="loadLicenses">查询授权</el-button>
          <el-divider direction="vertical" />
          <el-tag effect="plain" type="info">已选 {{ selectedLicenses.length }} 条</el-tag>
          <el-button
            v-permission="'btn:plugins_cloudflare_worker_admin:licenseedit'"
            :disabled="selectedLicenses.length === 0"
            @click="openBatchDurationDialog"
          >
            批量改时长
          </el-button>
          <el-button
            v-permission="'btn:plugins_cloudflare_worker_admin:licenseedit'"
            :disabled="selectedLicenses.length === 0"
            @click="openBatchExpireDialog"
          >
            批量改到期
          </el-button>
          <el-button
            v-permission="'btn:plugins_cloudflare_worker_admin:licenseedit'"
            :disabled="selectedLicenses.length === 0"
            @click="openBatchAdjustDialog"
          >
            批量校正
          </el-button>
          <el-button
            v-permission="'btn:plugins_cloudflare_worker_admin:licenseedit'"
            :disabled="selectedLicenses.length === 0"
            type="danger"
            @click="batchDeleteLicenses"
          >
            批量删除
          </el-button>
        </div>

        <el-table
          :data="licenseRows"
          stripe
          v-loading="licenseLoading"
          @selection-change="onLicenseSelectionChange"
        >
          <el-table-column type="selection" width="42" />
          <el-table-column prop="id" label="编号" width="80" />
          <el-table-column prop="machineCode" label="机器码" min-width="220" />
          <el-table-column prop="plan" label="套餐" width="120" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              {{ licenseStatusLabel(row.status) }}
            </template>
          </el-table-column>
          <el-table-column prop="issueAtText" label="生效时间" min-width="180" />
          <el-table-column prop="expireAtText" label="到期时间" min-width="180" />
          <el-table-column label="操作" width="430">
            <template #default="{ row }">
              <el-button
                v-permission="'btn:plugins_cloudflare_worker_admin:licenseedit'"
                link
                type="primary"
                @click="openDurationDialog(row)"
              >
                改时长
              </el-button>
              <el-button
                v-permission="'btn:plugins_cloudflare_worker_admin:licenseedit'"
                link
                type="warning"
                @click="openExpireDialog(row)"
              >
                改到期
              </el-button>
              <el-button
                v-permission="'btn:plugins_cloudflare_worker_admin:licenseedit'"
                link
                @click="openAdjustDialog(row)"
              >
                校正时间
              </el-button>
              <el-button
                v-permission="'btn:plugins_cloudflare_worker_admin:licenseedit'"
                link
                type="danger"
                @click="deleteLicense(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="配置总览" name="all-config">
        <el-table :data="configRows" stripe>
          <el-table-column prop="key" label="配置键" min-width="200" />
          <el-table-column prop="updatedAt" label="更新时间" width="180" />
          <el-table-column label="配置值" min-width="380">
            <template #default="{ row }">
              <el-input :model-value="toPrettyText(row.value)" type="textarea" :rows="4" readonly />
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>

  <el-dialog v-model="durationDialog.visible" title="调整授权时长" width="480px">
    <el-form :model="durationDialog.form" label-width="120px">
      <el-form-item label="授权编号">
        <el-input v-model="durationDialog.form.id" disabled />
      </el-form-item>
      <el-form-item label="月数">
        <el-input-number v-model="durationDialog.form.months" :min="-1200" :max="1200" />
      </el-form-item>
      <el-form-item label="模式">
        <el-select v-model="durationDialog.form.mode" style="width: 180px">
          <el-option label="追加" value="append" />
          <el-option label="重置" value="reset" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="durationDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="submitDuration">确认保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="expireDialog.visible" title="修改到期时间" width="520px">
    <el-form :model="expireDialog.form" label-width="120px">
      <el-form-item label="授权编号">
        <el-input v-model="expireDialog.form.id" disabled />
      </el-form-item>
      <el-form-item label="到期时间戳">
        <el-input-number v-model="expireDialog.form.expireAt" :min="0" :step="60000" style="width: 240px" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="expireDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="submitExpire">确认保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="adjustDialog.visible" title="校正授权时间" width="560px">
    <el-form :model="adjustDialog.form" label-width="130px">
      <el-form-item label="授权编号">
        <el-input v-model="adjustDialog.form.id" disabled />
      </el-form-item>
      <el-form-item label="调整方式">
        <el-select v-model="adjustDialog.form.op" style="width: 220px">
          <el-option label="延长到" value="extend_to" />
          <el-option label="回退到" value="rollback_to" />
        </el-select>
      </el-form-item>
      <el-form-item label="目标时间戳">
        <el-input-number
          v-model="adjustDialog.form.targetAt"
          :min="0"
          :step="60000"
          style="width: 260px"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="adjustDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="submitAdjust">确认保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="batchDurationDialog.visible" title="批量调整授权时长" width="480px">
    <el-form :model="batchDurationDialog.form" label-width="120px">
      <el-form-item label="目标数量">
        <el-tag type="info" effect="plain">{{ selectedLicenses.length }}</el-tag>
      </el-form-item>
      <el-form-item label="月数">
        <el-input-number v-model="batchDurationDialog.form.months" :min="-1200" :max="1200" />
      </el-form-item>
      <el-form-item label="模式">
        <el-select v-model="batchDurationDialog.form.mode" style="width: 180px">
          <el-option label="追加" value="append" />
          <el-option label="重置" value="reset" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="batchDurationDialog.visible = false">取消</el-button>
      <el-button type="primary" :loading="batchRunning" @click="submitBatchDuration">开始执行</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="batchExpireDialog.visible" title="批量修改到期时间" width="520px">
    <el-form :model="batchExpireDialog.form" label-width="120px">
      <el-form-item label="目标数量">
        <el-tag type="info" effect="plain">{{ selectedLicenses.length }}</el-tag>
      </el-form-item>
      <el-form-item label="到期时间戳">
        <el-input-number
          v-model="batchExpireDialog.form.expireAt"
          :min="0"
          :step="60000"
          style="width: 260px"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="batchExpireDialog.visible = false">取消</el-button>
      <el-button type="primary" :loading="batchRunning" @click="submitBatchExpire">开始执行</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="batchAdjustDialog.visible" title="批量校正授权时间" width="560px">
    <el-form :model="batchAdjustDialog.form" label-width="130px">
      <el-form-item label="目标数量">
        <el-tag type="info" effect="plain">{{ selectedLicenses.length }}</el-tag>
      </el-form-item>
      <el-form-item label="调整方式">
        <el-select v-model="batchAdjustDialog.form.op" style="width: 220px">
          <el-option label="延长到" value="extend_to" />
          <el-option label="回退到" value="rollback_to" />
        </el-select>
      </el-form-item>
      <el-form-item label="目标时间戳">
        <el-input-number
          v-model="batchAdjustDialog.form.targetAt"
          :min="0"
          :step="60000"
          style="width: 260px"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="batchAdjustDialog.visible = false">取消</el-button>
      <el-button type="primary" :loading="batchRunning" @click="submitBatchAdjust">开始执行</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="batchProgressDialog.visible"
    title="批量执行进度"
    width="900px"
    :close-on-click-modal="false"
    :close-on-press-escape="!batchRunning"
  >
    <div class="toolbar">
      <el-tag type="info" effect="plain">{{ batchProgressDialog.title || "-" }}</el-tag>
      <el-progress
        style="width: 320px"
        :percentage="batchProgressPercent()"
        :status="batchProgressStatus()"
      />
      <el-tag effect="plain">已处理 {{ batchProgressDialog.processed }}/{{ batchProgressDialog.total }}</el-tag>
      <el-tag type="success" effect="plain">成功 {{ batchProgressDialog.success }}</el-tag>
      <el-tag type="danger" effect="plain">失败 {{ batchProgressDialog.failed }}</el-tag>
    </div>

    <el-table :data="batchProgressDialog.rows" stripe height="360px">
      <el-table-column type="index" label="#" width="56" />
      <el-table-column prop="id" label="授权编号" width="100" />
      <el-table-column prop="machineCode" label="机器码" min-width="220" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="batchStatusTagType(row.status)" effect="plain">{{ batchStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="message" label="说明" min-width="320" />
    </el-table>

    <template #footer>
      <el-button
        type="warning"
        plain
        :disabled="batchRunning || !canRetryFailedBatch()"
        @click="retryFailedBatch"
      >
        重试失败项
      </el-button>
      <el-button :disabled="batchRunning" @click="batchProgressDialog.visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { workerAdminApi } from "../api/workerAdmin";

const activeTab = ref("plan");
const ready = ref(false);
const adminUser = ref("");
const serviceMessage = ref("");

const planConfigText = ref("{}");
const paymentConfigText = ref("{}");
const emailConfigText = ref("{}");
const menuCodesText = ref("{}");
const otherConfigText = ref("{}");
const testEmail = ref("");
const passwordForm = reactive({
  oldPassword: "",
  newPassword: ""
});

const configRows = ref([]);

const licenseLoading = ref(false);
const batchRunning = ref(false);
const licenseRows = ref([]);
const selectedLicenses = ref([]);
const licenseQuery = reactive({
  keyword: "",
  plan: "",
  status: ""
});

const durationDialog = reactive({
  visible: false,
  form: {
    id: "",
    months: 1,
    mode: "append"
  }
});

const expireDialog = reactive({
  visible: false,
  form: {
    id: "",
    expireAt: Date.now() + 30 * 24 * 60 * 60 * 1000
  }
});

const adjustDialog = reactive({
  visible: false,
  form: {
    id: "",
    op: "extend_to",
    targetAt: Date.now() + 30 * 24 * 60 * 60 * 1000
  }
});

const batchDurationDialog = reactive({
  visible: false,
  form: {
    months: 1,
    mode: "append"
  }
});

const batchExpireDialog = reactive({
  visible: false,
  form: {
    expireAt: Date.now() + 30 * 24 * 60 * 60 * 1000
  }
});

const batchAdjustDialog = reactive({
  visible: false,
  form: {
    op: "extend_to",
    targetAt: Date.now() + 30 * 24 * 60 * 60 * 1000
  }
});

const batchProgressDialog = reactive({
  visible: false,
  title: "",
  total: 0,
  processed: 0,
  success: 0,
  failed: 0,
  rows: []
});

const batchRetryAction = ref(null);

const licenseStatusTextMap = {
  active: "生效中",
  expired: "已过期",
  disabled: "已停用",
  pending: "待生效",
  revoked: "已撤销"
};

function toPrettyText(value) {
  try {
    return JSON.stringify(value ?? {}, null, 2);
  } catch (_error) {
    return String(value ?? "");
  }
}

function parseJsonText(raw, fallback = {}) {
  const text = String(raw || "").trim();
  if (!text) {
    return fallback;
  }
  return JSON.parse(text);
}

function epochToText(epochMs) {
  const value = Number(epochMs || 0);
  if (!value) {
    return "-";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return String(epochMs);
  }
  return date.toLocaleString("zh-CN", { hour12: false });
}

function batchStatusTagType(status) {
  const value = String(status || "").toLowerCase();
  if (value === "success") {
    return "success";
  }
  if (value === "failed") {
    return "danger";
  }
  if (value === "running") {
    return "warning";
  }
  return "info";
}

function batchStatusLabel(status) {
  const value = String(status || "").toLowerCase();
  return {
    pending: "待处理",
    running: "执行中",
    success: "成功",
    failed: "失败"
  }[value] || "未知状态";
}

function licenseStatusLabel(status) {
  const value = String(status || "").toLowerCase();
  return licenseStatusTextMap[value] || String(status || "-");
}

function batchProgressPercent() {
  const total = Number(batchProgressDialog.total || 0);
  if (total <= 0) {
    return 0;
  }
  const processed = Number(batchProgressDialog.processed || 0);
  return Math.min(100, Math.round((processed / total) * 100));
}

function batchProgressStatus() {
  if (batchRunning.value) {
    return "";
  }
  if (Number(batchProgressDialog.failed || 0) > 0) {
    return "exception";
  }
  return "success";
}

function canRetryFailedBatch() {
  if (!batchRetryAction.value || batchRunning.value) {
    return false;
  }
  return batchProgressDialog.rows.some((row) => row.status === "failed");
}

function normalizeBatchErrorMessage(error) {
  if (!error) {
    return "未知错误";
  }
  if (typeof error === "string") {
    return error;
  }
  return String(error?.message || error || "未知错误");
}

function normalizeServiceMessage(error) {
  const raw = String(error?.message || error || "").trim();
  if (!raw) {
    return "授权中心外部服务暂不可用，请检查服务地址和认证参数。";
  }
  if (raw.includes("baseUrl is not configured")) {
    return "授权中心外部服务未配置，请先设置市场服务地址。";
  }
  if (raw.includes("signed api credentials") || raw.includes("admin credentials")) {
    return "授权中心外部服务缺少认证参数，请配置签名密钥或管理员账号密码。";
  }
  return raw;
}

function handleServiceFailure(error) {
  serviceMessage.value = normalizeServiceMessage(error);
  ready.value = false;
  adminUser.value = "";
}

function beginBatchProgress(title, rows) {
  batchProgressDialog.title = title;
  batchProgressDialog.total = rows.length;
  batchProgressDialog.processed = 0;
  batchProgressDialog.success = 0;
  batchProgressDialog.failed = 0;
  batchProgressDialog.rows = rows.map((row) => ({
    id: row.id,
    machineCode: row.machineCode || "",
    status: "pending",
    message: "等待执行"
  }));
  batchProgressDialog.visible = true;
}

async function executeBatchOperation(title, rows, action) {
  const safeRows = Array.isArray(rows) ? rows.slice() : [];
  if (safeRows.length === 0) {
    ElMessage.warning("请先选择授权记录");
    return { total: 0, success: 0, failed: [] };
  }
  beginBatchProgress(title, safeRows);
  batchRunning.value = true;
  const failed = [];
  let success = 0;
  try {
    for (let index = 0; index < safeRows.length; index += 1) {
      const row = safeRows[index];
      batchProgressDialog.rows[index].status = "running";
      batchProgressDialog.rows[index].message = "执行中";
      try {
        await action(row);
        success += 1;
        batchProgressDialog.rows[index].status = "success";
        batchProgressDialog.rows[index].message = "处理成功";
      } catch (error) {
        const message = normalizeBatchErrorMessage(error);
        failed.push({
          id: row.id,
          message
        });
        batchProgressDialog.rows[index].status = "failed";
        batchProgressDialog.rows[index].message = message;
      } finally {
        batchProgressDialog.processed += 1;
        batchProgressDialog.success = success;
        batchProgressDialog.failed = failed.length;
      }
    }
  } finally {
    batchRunning.value = false;
  }
  const total = safeRows.length;
  if (failed.length === 0) {
    ElMessage.success(`${title}已完成，成功 ${success} 条`);
  } else {
    const first = failed[0];
    ElMessage.warning(
      `${title}执行完成，成功 ${success} 条，失败 ${failed.length} 条，首个失败项 #${first.id}：${first.message}`
    );
  }
  await loadLicenses();
  return { total, success, failed };
}

async function loadMe() {
  try {
    const resp = await workerAdminApi.me();
    ready.value = !!resp?.data?.username;
    adminUser.value = resp?.data?.username || "";
    serviceMessage.value = "";
    return ready.value;
  } catch (error) {
    handleServiceFailure(error);
    return false;
  }
}

async function loadPlanConfig() {
  try {
    const resp = await workerAdminApi.getPlanConfig();
    planConfigText.value = toPrettyText(resp?.data || {});
    serviceMessage.value = "";
    return true;
  } catch (error) {
    planConfigText.value = "{}";
    handleServiceFailure(error);
    return false;
  }
}

async function savePlanConfig() {
  try {
    const payload = parseJsonText(planConfigText.value, {});
    await workerAdminApi.savePlanConfig(payload);
    ElMessage.success("套餐配置保存成功");
    await loadPlanConfig();
  } catch (error) {
    ElMessage.error(error?.message || "保存套餐配置失败");
  }
}

async function loadPaymentConfig() {
  try {
    const resp = await workerAdminApi.getPaymentConfig();
    paymentConfigText.value = toPrettyText(resp?.data || {});
    serviceMessage.value = "";
    return true;
  } catch (error) {
    paymentConfigText.value = "{}";
    handleServiceFailure(error);
    return false;
  }
}

async function savePaymentConfig() {
  try {
    const payload = parseJsonText(paymentConfigText.value, {});
    await workerAdminApi.savePaymentConfig(payload);
    ElMessage.success("支付配置保存成功");
    await loadPaymentConfig();
  } catch (error) {
    ElMessage.error(error?.message || "保存支付配置失败");
  }
}

async function loadEmailConfig() {
  try {
    const resp = await workerAdminApi.getEmailConfig();
    emailConfigText.value = toPrettyText(resp?.data || {});
    serviceMessage.value = "";
    return true;
  } catch (error) {
    emailConfigText.value = "{}";
    handleServiceFailure(error);
    return false;
  }
}

async function saveEmailConfig() {
  try {
    const payload = parseJsonText(emailConfigText.value, {});
    await workerAdminApi.saveEmailConfig(payload);
    ElMessage.success("邮件配置保存成功");
    await loadEmailConfig();
  } catch (error) {
    ElMessage.error(error?.message || "保存邮件配置失败");
  }
}

async function sendTestEmail() {
  if (!testEmail.value) {
    ElMessage.warning("请输入测试邮箱地址");
    return;
  }
  try {
    await workerAdminApi.testEmail({ toEmail: testEmail.value });
    ElMessage.success("测试邮件已提交发送");
  } catch (error) {
    ElMessage.error(error?.message || "发送测试邮件失败");
  }
}

async function loadMenuCodes() {
  try {
    const resp = await workerAdminApi.getMenuCodes();
    menuCodesText.value = toPrettyText(resp?.data || {});
    serviceMessage.value = "";
    return true;
  } catch (error) {
    menuCodesText.value = "{}";
    handleServiceFailure(error);
    return false;
  }
}

async function saveMenuCodes() {
  try {
    const payload = parseJsonText(menuCodesText.value, {});
    await workerAdminApi.saveMenuCodes(payload);
    ElMessage.success("菜单编码保存成功");
    await loadMenuCodes();
  } catch (error) {
    ElMessage.error(error?.message || "保存菜单编码失败");
  }
}

async function loadOtherConfig() {
  try {
    const resp = await workerAdminApi.getOtherConfig();
    otherConfigText.value = toPrettyText(resp?.data || {});
    serviceMessage.value = "";
    return true;
  } catch (error) {
    otherConfigText.value = "{}";
    handleServiceFailure(error);
    return false;
  }
}

async function saveOtherConfig() {
  try {
    const payload = parseJsonText(otherConfigText.value, {});
    await workerAdminApi.saveOtherConfig(payload);
    ElMessage.success("其他配置保存成功");
    await loadOtherConfig();
  } catch (error) {
    ElMessage.error(error?.message || "保存其他配置失败");
  }
}

async function changePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning("旧密码和新密码不能为空");
    return;
  }
  if (passwordForm.newPassword.length < 8) {
    ElMessage.warning("新密码长度不能少于 8 位");
    return;
  }
  try {
    await workerAdminApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    });
    ElMessage.success("管理员密码修改成功");
    passwordForm.oldPassword = "";
    passwordForm.newPassword = "";
  } catch (error) {
    ElMessage.error(error?.message || "修改密码失败");
  }
}

async function loadConfigList() {
  try {
    const resp = await workerAdminApi.listConfigs();
    configRows.value = resp?.data || [];
    serviceMessage.value = "";
    return true;
  } catch (error) {
    configRows.value = [];
    handleServiceFailure(error);
    return false;
  }
}

async function loadLicenses() {
  licenseLoading.value = true;
  try {
    const resp = await workerAdminApi.listLicenses(licenseQuery);
    const list = resp?.data?.list || [];
    licenseRows.value = list.map((item) => ({
      ...item,
      issueAtText: epochToText(item.issueAt),
      expireAtText: epochToText(item.expireAt)
    }));
    selectedLicenses.value = [];
    serviceMessage.value = "";
    return true;
  } catch (error) {
    licenseRows.value = [];
    selectedLicenses.value = [];
    handleServiceFailure(error);
    return false;
  } finally {
    licenseLoading.value = false;
  }
}

function onLicenseSelectionChange(rows) {
  selectedLicenses.value = Array.isArray(rows) ? rows : [];
}

function openDurationDialog(row) {
  durationDialog.form.id = row.id;
  durationDialog.form.months = 1;
  durationDialog.form.mode = "append";
  durationDialog.visible = true;
}

async function submitDuration() {
  try {
    await workerAdminApi.updateLicenseDuration({
      id: durationDialog.form.id,
      months: durationDialog.form.months,
      mode: durationDialog.form.mode
    });
    durationDialog.visible = false;
    ElMessage.success("授权时长已更新");
    await loadLicenses();
  } catch (error) {
    ElMessage.error(error?.message || "更新授权时长失败");
  }
}

function openExpireDialog(row) {
  expireDialog.form.id = row.id;
  expireDialog.form.expireAt = Number(row.expireAt || Date.now());
  expireDialog.visible = true;
}

async function submitExpire() {
  try {
    await workerAdminApi.updateLicenseExpire({
      id: expireDialog.form.id,
      expireAt: Number(expireDialog.form.expireAt || 0)
    });
    expireDialog.visible = false;
    ElMessage.success("到期时间已更新");
    await loadLicenses();
  } catch (error) {
    ElMessage.error(error?.message || "更新到期时间失败");
  }
}

function openAdjustDialog(row) {
  adjustDialog.form.id = row.id;
  adjustDialog.form.op = "extend_to";
  adjustDialog.form.targetAt = Number(row.expireAt || Date.now());
  adjustDialog.visible = true;
}

async function submitAdjust() {
  try {
    await workerAdminApi.adjustLicenseExpire({
      id: adjustDialog.form.id,
      op: adjustDialog.form.op,
      targetAt: Number(adjustDialog.form.targetAt || 0)
    });
    adjustDialog.visible = false;
    ElMessage.success("授权时间已校正");
    await loadLicenses();
  } catch (error) {
    ElMessage.error(error?.message || "校正授权时间失败");
  }
}

async function deleteLicense(row) {
  try {
    await ElMessageBox.confirm(`确认删除授权记录 ${row.id} 吗？`, "删除确认", { type: "warning" });
    await workerAdminApi.deleteLicense({ id: row.id });
    ElMessage.success("授权记录已删除");
    await loadLicenses();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(error?.message || "删除授权记录失败");
    }
  }
}

function openBatchDurationDialog() {
  if (selectedLicenses.value.length === 0) {
    ElMessage.warning("请先选择授权记录");
    return;
  }
  batchDurationDialog.visible = true;
}

function openBatchExpireDialog() {
  if (selectedLicenses.value.length === 0) {
    ElMessage.warning("请先选择授权记录");
    return;
  }
  batchExpireDialog.visible = true;
}

function openBatchAdjustDialog() {
  if (selectedLicenses.value.length === 0) {
    ElMessage.warning("请先选择授权记录");
    return;
  }
  batchAdjustDialog.visible = true;
}

async function runBatchOperation(title, action) {
  const rows = selectedLicenses.value.slice();
  return executeBatchOperation(title, rows, action);
}

async function retryFailedBatch() {
  if (!canRetryFailedBatch()) {
    return;
  }
  const failedRows = batchProgressDialog.rows
    .filter((row) => row.status === "failed")
    .map((row) => ({
      id: row.id,
      machineCode: row.machineCode || ""
    }));
  if (failedRows.length === 0) {
    ElMessage.info("当前没有可重试的失败项");
    return;
  }
  const retry = batchRetryAction.value;
  await executeBatchOperation(`${retry.title}（重试）`, failedRows, retry.action);
}

async function submitBatchDuration() {
  const form = {
    months: Number(batchDurationDialog.form.months || 0),
    mode: batchDurationDialog.form.mode === "reset" ? "reset" : "append"
  };
  const title = "批量调整授权时长";
  const action = (row) =>
    workerAdminApi.updateLicenseDuration({
      id: row.id,
      months: form.months,
      mode: form.mode
    });
  batchRetryAction.value = { title, action };
  await runBatchOperation(title, action);
  batchDurationDialog.visible = false;
}

async function submitBatchExpire() {
  const form = {
    expireAt: Number(batchExpireDialog.form.expireAt || 0)
  };
  const title = "批量修改到期时间";
  const action = (row) =>
    workerAdminApi.updateLicenseExpire({
      id: row.id,
      expireAt: Number(form.expireAt || 0)
    });
  batchRetryAction.value = { title, action };
  await runBatchOperation(title, action);
  batchExpireDialog.visible = false;
}

async function submitBatchAdjust() {
  const form = {
    op: batchAdjustDialog.form.op === "rollback_to" ? "rollback_to" : "extend_to",
    targetAt: Number(batchAdjustDialog.form.targetAt || 0)
  };
  const title = "批量校正授权时间";
  const action = (row) =>
    workerAdminApi.adjustLicenseExpire({
      id: row.id,
      op: form.op,
      targetAt: Number(form.targetAt || 0)
    });
  batchRetryAction.value = { title, action };
  await runBatchOperation(title, action);
  batchAdjustDialog.visible = false;
}

async function batchDeleteLicenses() {
  const rows = selectedLicenses.value.slice();
  if (rows.length === 0) {
    ElMessage.warning("请先选择授权记录");
    return;
  }
  try {
    await ElMessageBox.confirm(`确认删除已选择的 ${rows.length} 条授权记录吗？`, "删除确认", { type: "warning" });
  } catch (_cancel) {
    return;
  }
  const title = "批量删除授权记录";
  const action = (row) => workerAdminApi.deleteLicense({ id: row.id });
  batchRetryAction.value = { title, action };
  await runBatchOperation(title, action);
}

async function refreshAll() {
  const readyState = await loadMe();
  if (!readyState) {
    planConfigText.value = "{}";
    paymentConfigText.value = "{}";
    emailConfigText.value = "{}";
    menuCodesText.value = "{}";
    otherConfigText.value = "{}";
    configRows.value = [];
    licenseRows.value = [];
    selectedLicenses.value = [];
    return;
  }
  await loadPlanConfig();
  await loadPaymentConfig();
  await loadEmailConfig();
  await loadMenuCodes();
  await loadOtherConfig();
  await loadConfigList();
  await loadLicenses();
}

onMounted(() => {
  refreshAll();
});
</script>

<style scoped>
.status-alert {
  margin-top: 12px;
}
</style>
