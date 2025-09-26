import { defineStore } from 'pinia';

export const useWizzardStore = defineStore('wizzard', {
  state: () => ({
    wizzardData: {
      startScanning: null as boolean | null,
    },
  }),

  getters: {
    startScanning(state) {
      return state.wizzardData.startScanning;
    },
  },

  actions: {
    setStartScanning(startScanning: boolean | null) {
      this.wizzardData.startScanning = startScanning;
    },
  },
});
