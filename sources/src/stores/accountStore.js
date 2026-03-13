import { defineStore } from "pinia";
import { api } from "../api/cloudflare";

const KEY_ACCOUNT_ID = "cf_selected_account_id";
const KEY_ZONE_ID = "cf_selected_zone_id";

function readNumber(key) {
  const raw = window.localStorage.getItem(key);
  if (!raw) return null;
  const value = Number(raw);
  return Number.isFinite(value) ? value : null;
}

function readString(key) {
  return window.localStorage.getItem(key) || null;
}

function normalizeZones(payload) {
  const result = payload?.result || [];
  return result.map((item) => ({
    id: item.id,
    name: item.name,
    status: item.status,
    type: item.type,
    planName: item.plan?.name
  }));
}

export const useAccountStore = defineStore("account", {
  state: () => ({
    accounts: [],
    selectedAccountId: readNumber(KEY_ACCOUNT_ID),
    zones: [],
    selectedZoneId: readString(KEY_ZONE_ID),
    loadingZones: false
  }),
  getters: {
    selectedAccount(state) {
      return state.accounts.find((a) => a.id === state.selectedAccountId) || null;
    },
    selectedZone(state) {
      return state.zones.find((z) => z.id === state.selectedZoneId) || null;
    },
    selectedCloudflareAccountId(state) {
      return (
        state.accounts.find((a) => a.id === state.selectedAccountId)?.cloudflareAccountId ||
        null
      );
    }
  },
  actions: {
    persistSelection() {
      if (this.selectedAccountId) {
        window.localStorage.setItem(KEY_ACCOUNT_ID, String(this.selectedAccountId));
      } else {
        window.localStorage.removeItem(KEY_ACCOUNT_ID);
      }
      if (this.selectedZoneId) {
        window.localStorage.setItem(KEY_ZONE_ID, this.selectedZoneId);
      } else {
        window.localStorage.removeItem(KEY_ZONE_ID);
      }
    },

    async refresh() {
      const resp = await api.listAccounts();
      this.accounts = resp.data || [];

      if (!this.selectedAccountId && this.accounts.length > 0) {
        this.selectedAccountId = this.accounts[0].id;
      }
      if (
        this.selectedAccountId &&
        !this.accounts.find((a) => a.id === this.selectedAccountId)
      ) {
        this.selectedAccountId = this.accounts[0]?.id || null;
      }
      this.persistSelection();
      await this.refreshZones();
    },

    async changeAccount(accountId) {
      this.selectedAccountId = accountId || null;
      this.selectedZoneId = null;
      this.zones = [];
      this.persistSelection();
      await this.refreshZones();
    },

    changeZone(zoneId) {
      this.selectedZoneId = zoneId || null;
      this.persistSelection();
    },

    async refreshZones() {
      if (!this.selectedAccountId) {
        this.zones = [];
        this.selectedZoneId = null;
        this.persistSelection();
        return;
      }

      this.loadingZones = true;
      try {
        const resp = await api.listZones(this.selectedAccountId, {
          page: 1,
          perPage: 200
        });
        this.zones = normalizeZones(resp.data);

        const account = this.selectedAccount;
        const defaultZone = account?.defaultZoneId;
        const defaultExists = this.zones.some((z) => z.id === defaultZone);
        if (!this.selectedZoneId) {
          this.selectedZoneId = defaultExists
            ? defaultZone
            : this.zones[0]?.id || null;
        } else if (!this.zones.some((z) => z.id === this.selectedZoneId)) {
          this.selectedZoneId = defaultExists
            ? defaultZone
            : this.zones[0]?.id || null;
        }
        this.persistSelection();
      } catch (_error) {
        this.zones = [];
        this.selectedZoneId = null;
        this.persistSelection();
      } finally {
        this.loadingZones = false;
      }
    }
  }
});
