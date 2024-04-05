import {defineStore} from 'pinia';

export const useProfilesStore = defineStore('profiles', {
  state: () => ({
    desktopModeEnabled: false,
    serverModeEnabled: false,
    e2eModeEnabled: false,
  }),

  getters: {
    isDesktopModeEnabled(state) {
      return state.desktopModeEnabled;
    },
    isServerModeEnabled(state) {
      return state.serverModeEnabled;
    },
    isE2eModeEnabled(state) {
      return state.e2eModeEnabled;
    },
  },

  actions: {
    setDesktopModeEnabled(enabled: boolean) {
      this.desktopModeEnabled = enabled;
    },
    setServerModeEnabled(enabled: boolean) {
      this.serverModeEnabled = enabled;
    },
    setE2eModeEnabled(enabled: boolean) {
      this.e2eModeEnabled = enabled;
    },
  }
});
