import {defineStore} from 'pinia';

export const useDesktopStore = defineStore('desktop', {
  state: () => ({
    desktopModeEnabled: false
  }),

  getters: {
    isDesktopModeEnabled (state) {
      return state.desktopModeEnabled;
    }
  },

  actions: {
    setEnabled (enabled: boolean) {
      this.desktopModeEnabled = enabled;
    }
  }
});
