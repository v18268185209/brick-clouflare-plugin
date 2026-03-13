<template>
  <div class="app-shell" :class="{ 'is-micro-app': microAppMode }">
    <aside v-if="!microAppMode" class="app-sidebar">
      <div class="brand-wrap">
        <div class="brand-kicker">独立测试模式</div>
        <div class="brand-title">边缘云平台</div>
        <div class="brand-sub">可在无主应用场景下直接联调全部页面</div>
      </div>

      <el-menu
        :default-active="activePath"
        class="menu"
        background-color="transparent"
        text-color="#eef6f0"
        active-text-color="#8ff3c6"
        router
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.path"
          :index="item.path"
        >
          {{ item.title }}
        </el-menu-item>
      </el-menu>
    </aside>

    <main class="app-main">
      <header class="app-topbar">
        <div class="topbar-title">
          <strong>{{ title }}</strong>
          <div class="top-subline">
            <el-tag size="small" effect="plain" type="success">
              账号 {{ store.accounts.length }}
            </el-tag>
            <el-tag size="small" effect="plain" type="info">
              站点 {{ store.zones.length }}
            </el-tag>
            <span class="api-prefix">
              接口前缀 {{ apiPrefix }}
            </span>
          </div>
        </div>

        <div class="workspace-selectors">
          <el-select
            :model-value="store.selectedAccountId"
            style="width: 280px"
            placeholder="请选择账号"
            filterable
            clearable
            @change="onAccountChange"
          >
            <el-option
              v-for="acc in store.accounts"
              :key="acc.id"
              :label="`${acc.displayName}（#${acc.id}）`"
              :value="acc.id"
            />
          </el-select>

          <el-select
            :model-value="store.selectedZoneId"
            style="width: 320px"
            placeholder="请选择站点"
            filterable
            :loading="store.loadingZones"
            clearable
            @change="onZoneChange"
          >
            <el-option
              v-for="zone in store.zones"
              :key="zone.id"
              :label="`${zone.name}（${zone.id.slice(0, 8)}...）`"
              :value="zone.id"
            />
          </el-select>
        </div>
      </header>

      <section class="app-content">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { useRoute } from "vue-router";
import { useAccountStore } from "../stores/accountStore";
import { menuItems } from "../router/menus";
import { isMicroAppMode, resolveApiBase } from "../utils/runtime";

const route = useRoute();
const store = useAccountStore();

const microAppMode = isMicroAppMode();
const title = computed(() => route.meta.title || "边缘云平台");
const activePath = computed(() => route.path);
const apiPrefix = resolveApiBase();

async function onAccountChange(val) {
  try {
    await store.changeAccount(val);
  } catch (_error) {
    // The request layer already presents the failure, keep the shell stable.
  }
}

function onZoneChange(val) {
  store.changeZone(val);
}

onMounted(async () => {
  try {
    await store.refresh();
  } catch (_error) {
    // The request layer already presents the failure, keep the shell stable.
  }
});
</script>

<style scoped>
.menu {
  border-right: none;
}

.brand-wrap {
  margin-bottom: 18px;
}

.brand-kicker {
  font-size: 12px;
  letter-spacing: 0.18em;
  color: rgba(214, 247, 228, 0.7);
  text-transform: uppercase;
}

.brand-title {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.brand-sub {
  margin-top: 8px;
  color: rgba(235, 246, 239, 0.76);
  font-size: 13px;
  line-height: 1.6;
}

.workspace-selectors {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.topbar-title {
  min-width: 0;
}

.top-subline {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.api-prefix {
  color: var(--text-sub);
  font-size: 12px;
}

.is-micro-app .app-main {
  min-height: 100vh;
}
</style>
