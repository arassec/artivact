import { defineStore } from 'pinia';

export const useWizzardStore = defineStore('wizzard', {
  state: () => ({
    wizzardData: {
      scanItem: null as boolean | null,
      scanPeripherals: null as boolean | null,
    },
  }),

  getters: {
    scanItem(state) {
      return state.wizzardData.scanItem;
    },
    scanPeripherals(state) {
      return state.wizzardData.scanPeripherals;
    },
  },

  actions: {
    setScanItem(scanItem: boolean | null) {
      this.wizzardData.scanItem = scanItem;
    },
    setScanPeripherals(scanPeripherals: boolean | null) {
      this.wizzardData.scanPeripherals = scanPeripherals;
    },
  },
});
